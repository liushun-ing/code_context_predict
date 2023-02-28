package com.example.demo1.data;

import com.example.demo1.operation.TableDataOperator;
import com.example.demo1.operation.TargetGraphOperator;
import com.example.demo1.plugin.model.MyTableModel;
import com.example.demo1.vf3.graph.Graph;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataCenter {
  public static Project PROJECT;
  // 时间间隙
  public static int TIME_INTERVAL = 5;
  // 预测步长
  public static int PREDICTION_STEP = 1;

  // 用于代码使用
  public static List<SuggestionData> SUGGESTION_LIST = new ArrayList<>();

  // 用于toolWindow展示
  public static MyTableModel TABLE_MODEL = new MyTableModel(null, Constants.HEAD);
  // 既要用于展示，也要用于代码分析
  public static DefaultTreeModel TREE_MODEL = new DefaultTreeModel(new DefaultMutableTreeNode(new ContextTaskData(), true));

  public static void reset(Project project) {
    PROJECT = project;
    TIME_INTERVAL = 5;
    PREDICTION_STEP = 1;
    SUGGESTION_LIST.clear();
    TABLE_MODEL.setDataVector(null, Constants.HEAD);
    TREE_MODEL.setRoot(new DefaultMutableTreeNode(new ContextTaskData(), true));
    TREE_MODEL.reload();
  }

  public static DefaultMutableTreeNode getTreeModelRoot() {
    return (DefaultMutableTreeNode) TREE_MODEL.getRoot();
  }

  public static DefaultMutableTreeNode getNewNode(Object o, boolean allowsChildren) {
    return new DefaultMutableTreeNode(new ContextTaskData(o), allowsChildren);
  }

  public static void updateTableModel() {
    Object[][] rows = new Object[SUGGESTION_LIST.size()][];
    for (int i = 0; i < SUGGESTION_LIST.size(); i++) {
      rows[i] = TableDataOperator.convertSuggestionData(SUGGESTION_LIST.get(i));
    }
    TABLE_MODEL.setDataVector(rows, Constants.HEAD);
  }

  /**
   * 过滤TimeInterval内的节点
   */
  public static void filterNodeInTimeInterval() {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) TREE_MODEL.getRoot();
    for (int i = 0; i < root.getChildCount(); i++) {
      DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) root.getChildAt(i);
      ContextTaskData userObject = (ContextTaskData) childAt.getUserObject();
      long time = userObject.getCaptureTime().getTime();
      long nowTime = new Date().getTime();
      if (time < (nowTime - TIME_INTERVAL * 1000L)) {
        TREE_MODEL.removeNodeFromParent(childAt);
        i--;
      } else {
        for (int j = 0; j < childAt.getChildCount(); j++) {
          DefaultMutableTreeNode childAt1 = (DefaultMutableTreeNode) childAt.getChildAt(j);
          ContextTaskData userObject1 = (ContextTaskData) childAt1.getUserObject();
          long time1 = userObject1.getCaptureTime().getTime();
          long nowTime1 = new Date().getTime();
          if (time1 < (nowTime1 - TIME_INTERVAL * 1000L)) {
            TREE_MODEL.removeNodeFromParent(childAt1);
            j--;
          }
        }
      }
    }
  }

  public static void filterSuggestionDataInTimeInterval() {
    for (int i = 0; i < SUGGESTION_LIST.size(); i++) {
      SuggestionData suggestionData = SUGGESTION_LIST.get(i);
      long time = suggestionData.getGenerateTime().getTime();
      long nowTime = new Date().getTime();
      if (time < (nowTime - TIME_INTERVAL * 1000L)) {
        SUGGESTION_LIST.remove(i);
        i--;
      }
    }
  }

  /**
   * 添加其他节点
   * @param o PsiElement
   */
  public static void addNewOtherData(PsiElement o) {
    if (o == null) {
      return;
    }
    DefaultMutableTreeNode newNode = getNewNode(o, false);
    DataCenter.TREE_MODEL.insertNodeInto(newNode, DataCenter.getTreeModelRoot(), DataCenter.getTreeModelRoot().getChildCount());
    filterNodeInTimeInterval();
  }


  /**
   * 添加字段节点，存在则更新时间
   * @param psiField 捕捉的字段
   */
  public static void addNewData(PsiField psiField) {
    if (psiField == null) {
      return;
    }
    PsiClass containingClass = psiField.getContainingClass();
    if (containingClass == null) {
      DefaultMutableTreeNode newNode = getNewNode(psiField, false);
      DataCenter.TREE_MODEL.insertNodeInto(newNode, DataCenter.getTreeModelRoot(), DataCenter.getTreeModelRoot().getChildCount());
    }
    DefaultMutableTreeNode findNode = findExist(containingClass);
    if (findNode == null) {
      DefaultMutableTreeNode newClass = getNewNode(containingClass, true);
      DefaultMutableTreeNode newField = getNewNode(psiField, false);
      newClass.add(newField);
      DataCenter.TREE_MODEL.insertNodeInto(newClass, DataCenter.getTreeModelRoot(), DataCenter.getTreeModelRoot().getChildCount());
      DataCenter.TREE_MODEL.reload();
    } else {
      for (int i = 0; i < findNode.getChildCount(); i++) {
        DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) findNode.getChildAt(i);
        ContextTaskData userObject = (ContextTaskData) childAt.getUserObject();
        if (userObject.getCaptureElement().equals(psiField)) {
          userObject.setCaptureTime(new Date());
          return;
        }
      }
      ContextTaskData userObject = (ContextTaskData) findNode.getUserObject();
      userObject.setCaptureTime(new Date());
      DefaultMutableTreeNode newField = getNewNode(psiField, false);
      DataCenter.TREE_MODEL.insertNodeInto(newField, findNode, findNode.getChildCount());
    }
    filterNodeInTimeInterval();
    TableDataOperator.executePrediction(psiField);
  }

  /**
   * 添加方法节点,存在则更新时间
   * @param psiMethod 捕捉的方法
   */
  public static void addNewData(PsiMethod psiMethod) {
    if (psiMethod == null) {
      return;
    }
    // 拿到当前元素所属的类，也就是文件
    PsiClass containingClass = psiMethod.getContainingClass();
    if (containingClass == null) {
      DefaultMutableTreeNode newNode = getNewNode(psiMethod, false);
      DataCenter.TREE_MODEL.insertNodeInto(newNode, DataCenter.getTreeModelRoot(), DataCenter.getTreeModelRoot().getChildCount());
    }
    DefaultMutableTreeNode findNode = findExist(containingClass);
    if (findNode == null) {
      DefaultMutableTreeNode newClass = getNewNode(containingClass, true);
      DefaultMutableTreeNode newMethod = getNewNode(psiMethod, false);
      newClass.add(newMethod);
      DataCenter.TREE_MODEL.insertNodeInto(newClass, DataCenter.getTreeModelRoot(), DataCenter.getTreeModelRoot().getChildCount());
      DataCenter.TREE_MODEL.reload();
    } else {
      for (int i = 0; i < findNode.getChildCount(); i++) {
        DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) findNode.getChildAt(i);
        ContextTaskData userObject = (ContextTaskData) childAt.getUserObject();
        if (userObject.getCaptureElement().equals(psiMethod)) {
          userObject.setCaptureTime(new Date());
          return;
        }
      }
      ContextTaskData userObject = (ContextTaskData) findNode.getUserObject();
      userObject.setCaptureTime(new Date()); // 父节点的时间也要更新
      DefaultMutableTreeNode newMethod = getNewNode(psiMethod, false);
      DataCenter.TREE_MODEL.insertNodeInto(newMethod, findNode, findNode.getChildCount());
    }
    filterNodeInTimeInterval();
    TableDataOperator.executePrediction(psiMethod);
  }

  /**
   * 添加类节点,存在则更新时间
   * @param psiClass 捕捉的类
   */
  public static void addNewData(PsiClass psiClass) {
    if (psiClass == null) {
      return;
    }
    DefaultMutableTreeNode exist = findExist(psiClass);
    if (exist == null) {
      DefaultMutableTreeNode newNode = getNewNode(psiClass, true);
      DataCenter.TREE_MODEL.insertNodeInto(newNode, DataCenter.getTreeModelRoot(), DataCenter.getTreeModelRoot().getChildCount());
    } else {
      ContextTaskData userObject = (ContextTaskData) exist.getUserObject();
      userObject.setCaptureTime(new Date());
    }
    filterNodeInTimeInterval();
    TableDataOperator.executePrediction(psiClass);
  }

  /**
   * 判断节点中是否存在改类
   * @param psiClass 需要判断的类节点
   * @return 是否存在
   */
  public static boolean whetherExist(PsiClass psiClass) {
    DefaultMutableTreeNode root = DataCenter.getTreeModelRoot();
    return traversal(root, psiClass);
  }

  /**
   * 辅助判断的递归函数
   * @param node 当前节点
   * @param psiClass 目标类节点
   * @return 是否存在
   */
  public static boolean traversal(DefaultMutableTreeNode node, PsiClass psiClass) {
    ContextTaskData userObject = (ContextTaskData) node.getUserObject();
    if (userObject.getCaptureElement() instanceof PsiClass) {
      PsiClass p = (PsiClass) userObject.getCaptureElement();
      if (p.equals(psiClass)) {
        return true;
      }
    }
    if (node.getChildCount() > 0) {
      for (int i = 0; i < node.getChildCount(); i++) {
        if (traversal((DefaultMutableTreeNode) node.getChildAt(i), psiClass)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 查找某个类节点是否存在，并返回
   * @param psiClass 目标类节点
   * @return 匹配成功的节点,无则null
   */
  public static DefaultMutableTreeNode findExist(PsiClass psiClass) {
    DefaultMutableTreeNode root = DataCenter.getTreeModelRoot();
    return findTraversal(root, psiClass);
  }

  /**
   * 辅助查找的递归函数
   * @param node 当前节点
   * @param psiClass 目标类节点
   * @return 匹配的节点，无则null
   */
  public static DefaultMutableTreeNode findTraversal(DefaultMutableTreeNode node, PsiClass psiClass) {
    ContextTaskData userObject = (ContextTaskData) node.getUserObject();
    if (userObject.getCaptureElement() instanceof PsiClass) {
      PsiClass p = (PsiClass) userObject.getCaptureElement();
      if (p.equals(psiClass)) {
        return node;
      }
    }
    if (node.getChildCount() > 0) {
      for (int i = 0; i < node.getChildCount(); i++) {
        if (findTraversal((DefaultMutableTreeNode) node.getChildAt(i), psiClass) != null) {
          return (DefaultMutableTreeNode) node.getChildAt(i);
        }
      }
    }
    return null;
  }
}