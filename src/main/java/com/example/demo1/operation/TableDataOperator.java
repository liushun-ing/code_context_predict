package com.example.demo1.operation;

import com.example.demo1.data.DataCenter;
import com.example.demo1.data.SuggestionData;
import com.example.demo1.vf3.algorithm.MainEntry;
import com.example.demo1.vf3.algorithm.MatchCouple;
import com.example.demo1.vf3.algorithm.Solution;
import com.example.demo1.vf3.graph.Graph;
import com.example.demo1.vf3.graph.Vertex;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 表格数据操作类
 */
public class TableDataOperator {
  /**
   * 根据suggestionData转换为table展示的数据类型
   *
   * @param suggestionData 需要转换的对象
   * @return 转换后的table行对象
   */
  public static Object[] convertSuggestionData(SuggestionData suggestionData) {
    Object[] raw = new Object[4];
    raw[0] = suggestionData.getElement();
    raw[1] = suggestionData.getPackagePath();
    raw[2] = suggestionData.getStereotype();
    raw[3] = suggestionData.getConfidence();
    return raw;
  }

  /**
   * 过滤TimeInterval内的建议数据项
   */
  public static void filterSuggestionDataInTimeInterval() {
    for (int i = 0; i < DataCenter.SUGGESTION_LIST.size(); i++) {
      SuggestionData suggestionData = DataCenter.SUGGESTION_LIST.get(i);
      long time = suggestionData.getGenerateTime().getTime();
      long nowTime = new Date().getTime();
      if (time < (nowTime - DataCenter.TIME_INTERVAL * 1000L)) {
        DataCenter.SUGGESTION_LIST.remove(i);
        i--;
      }
    }
  }

  /**
   * 判断是否建议列表中已经存在该元素，如果存在，则更新置信值，取最大值
   *
   * @param psiElement    目标元素
   * @param newConfidence 新的置信值
   * @return 是否存在
   */
  public static boolean existInSuggestionList(PsiElement psiElement, String newConfidence) {
    for (SuggestionData s : DataCenter.SUGGESTION_LIST) {
      if (s.getElement().equals(psiElement)) {
        if (Double.parseDouble(newConfidence) > Double.parseDouble(s.getConfidence())) {
          s.setConfidence(newConfidence);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * 更新SUGGESTION_LIST，在末尾追加
   *
   * @param list 新的列表项
   */
  public static void updateSuggestionList(List<SuggestionData> list) {
    DataCenter.SUGGESTION_LIST.addAll(list);
  }

  /**
   * 更新表格模型，将SUGGESTION_LIST的数据转换为table数据，并更新表格
   */
  public static void updateTableModel() {
    for (int i = DataCenter.TABLE_MODEL.getRowCount() - 1; i >= 0; i--) {
      DataCenter.TABLE_MODEL.removeRow(i);
    }
    for (int i = 0; i < DataCenter.SUGGESTION_LIST.size(); i++) {
      DataCenter.TABLE_MODEL.addRow(TableDataOperator.convertSuggestionData(DataCenter.SUGGESTION_LIST.get(i)));
    }
  }

  /**
   * 执行field元素的代码预测，包括构建目标图，分配原型，VF3子图匹配，计算置信值，更新table列表
   *
   * @param psiField 目标感兴趣PsiField
   */
  public static void executePrediction(PsiField psiField) {
    System.out.println("field prediction start");
    if (psiField == null) {
      return;
    }
    commonExecute(TargetGraphOperator.buildTargetGraph(psiField), psiField);
    System.out.println("field prediction complete");
  }

  /**
   * 执行method元素的代码预测，包括构建目标图，VF3子图匹配，计算置信值，更新table列表
   *
   * @param psiMethod 感兴趣method元素
   */
  public static void executePrediction(PsiMethod psiMethod) {
    System.out.println("method prediction start");
    if (psiMethod == null) {
      return;
    }
    commonExecute(TargetGraphOperator.buildTargetGraph(psiMethod), psiMethod);
    System.out.println("method prediction complete");
  }

  /**
   * 执行class元素的代码预测，包括构建目标图，VF3子图匹配，计算置信值，更新table列表
   *
   * @param psiClass 感兴趣Class元素
   */
  public static void executePrediction(PsiClass psiClass) {
    System.out.println("class prediction start");
    if (psiClass == null) {
      return;
    }
    commonExecute(TargetGraphOperator.buildTargetGraph(psiClass), psiClass);
    System.out.println("class prediction complete");
  }

  /**
   * 相同的执行逻辑，根据目标图执行一系列预测操作
   *
   * @param targetGraph 目标图
   * @param psiElement  目标元素
   */
  public static void commonExecute(Graph targetGraph, PsiElement psiElement) {
    if (targetGraph == null) {
      return;
    }
    // 分配原型
    TargetGraphOperator.assignStereotypeRole(targetGraph);
    // 分配原型之后需要重新统计图的节点类型
    targetGraph.countLabelQuantity();
    // 执行VF3子图匹配算法
    MainEntry mainEntry = new MainEntry();
    ArrayList<ArrayList<Solution>> executeResult = mainEntry.execute(targetGraph);
    // 计算置信度
    HashMap<Integer, Double> confidenceMap = calculateConfidence(executeResult, targetGraph);
    // 更新SUGGESTION_LIST,需要过滤掉时间过期的
    filterSuggestionDataInTimeInterval();
    // 数据转换
    ArrayList<SuggestionData> newSuggestionDataList = convertSuggestionList(confidenceMap, targetGraph, psiElement);
    // 将新得到的节点加入到SUGGESTION_LIST中
    updateSuggestionList(newSuggestionDataList);
    // 更新tableModel
    updateTableModel();
  }


  /**
   * 计算置信度
   *
   * @param executeResult VF3子图匹配结果
   * @param targetGraph   目标图
   * @return 返回节点和对应的置信度的map
   */
  public static HashMap<Integer, Double> calculateConfidence(ArrayList<ArrayList<Solution>> executeResult, Graph targetGraph) {
    ArrayList<Vertex> vertices = targetGraph.getVertices();
    HashMap<Integer, Double> confidenceMap = new HashMap<>();
    // 先初始化
    for (Vertex v : vertices) {
      confidenceMap.put(v.getId(), 0.0);
    }
    // 统计所有匹配子图数目
    Double occurrenceSum = 0.0;
    // 不同的模式图
    for (ArrayList<Solution> solutions : executeResult) {
      // 不同的解决方案
      for (Solution solution : solutions) {
        occurrenceSum++;
        // 映射节点对
        for (MatchCouple mc : solution.getSolution()) {
          Vertex targetV = mc.getV(); // 拿到目标图映射的节点
          if (confidenceMap.containsKey(targetV.getId())) {
            confidenceMap.replace(targetV.getId(), confidenceMap.get(targetV.getId()) + 1);
          }
        }
      }
    }
    if (occurrenceSum != 0) {
      // 计算比例
      for (Integer i : confidenceMap.keySet()) {
        confidenceMap.replace(i, confidenceMap.get(i) / occurrenceSum);
      }
    }
    return confidenceMap;
  }

  /**
   * 根据置信值和目标图，将匹配到的节点，转换为table数据
   *
   * @param confidenceMap 置信度映射map
   * @param targetGraph   目标图
   * @return table数据列表
   */
  public static ArrayList<SuggestionData> convertSuggestionList(HashMap<Integer, Double> confidenceMap, Graph targetGraph, PsiElement psiElement) {
    ArrayList<SuggestionData> suggestionList = new ArrayList<>();
    for (Integer i : confidenceMap.keySet()) {
      if (confidenceMap.get(i) > 0) {
        Vertex vertexById = targetGraph.getVertexById(i);
        if (!vertexById.getPsiElement().equals(psiElement)) {
          String s = confidenceMap.get(i).toString();
          s = s.length() > 6 ? s.substring(0, 6) : s;
          if (!existInSuggestionList(vertexById.getPsiElement(), s)) {
            suggestionList.add(new SuggestionData(vertexById.getPsiElement(), vertexById.getLabel(), s));
          }
        }
      }
    }
    return suggestionList;
  }


}