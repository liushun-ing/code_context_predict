package com.example.demo1.operation;

import com.example.demo1.data.DataCenter;
import com.example.demo1.data.SuggestionData;
import com.example.demo1.vf3.algorithm.MainEntry;
import com.example.demo1.vf3.algorithm.Solution;
import com.example.demo1.vf3.graph.Graph;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;

public class TableDataOperator {
  /**
   * 根据suggestionData转换为table展示的数据类型
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
   * 执行field元素的代码预测，包括构建目标图，分配原型，VF3子图匹配，计算置信值，更新table列表
   * @param psiField 目标感兴趣PsiField
   */
  public static void executePrediction(PsiField psiField) {
    if (psiField == null) {
      return;
    }
    // 构建目标图
    Graph targetGraph = TargetGraphOperator.buildTargetGraph(psiField);
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

    // 更新table,需要过滤掉时间过期的
    DataCenter.filterSuggestionDataInTimeInterval();
    DataCenter.updateTableModel();
    System.out.println("field executeResult: " + executeResult);
  }

  /**
   * 执行method元素的代码预测，包括构建目标图，VF3子图匹配，计算置信值，更新table列表
   * @param psiMethod 感兴趣method元素
   */
  public static void executePrediction(PsiMethod psiMethod) {
    if (psiMethod == null) {
      return;
    }
    Graph targetGraph = TargetGraphOperator.buildTargetGraph(psiMethod);
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
    System.out.println("method executeResult: " + executeResult);
  }

  /**
   * 执行class元素的代码预测，包括构建目标图，VF3子图匹配，计算置信值，更新table列表
   * @param psiClass
   */
  public static void executePrediction(PsiClass psiClass) {
    if (psiClass == null) {
      return;
    }
    Graph targetGraph = TargetGraphOperator.buildTargetGraph(psiClass);
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
    System.out.println("class executeResult: " + executeResult);
  }


}