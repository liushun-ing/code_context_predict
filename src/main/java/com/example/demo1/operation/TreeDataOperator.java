package com.example.demo1.operation;

import com.example.demo1.data.ContextTaskData;
import com.example.demo1.data.DataCenter;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Date;

public class TreeDataOperator {

  /**
   * 获取树模型的根节点
   *
   * @return 根节点
   */
  public static DefaultMutableTreeNode getTreeModelRoot() {
    return (DefaultMutableTreeNode) DataCenter.TREE_MODEL.getRoot();
  }

  /**
   * 获取一个新的树节点
   *
   * @param o 用户数据对象
   * @param allowsChildren 是否允许孩子节点存在
   * @return 新的树节点对象
   */
  public static DefaultMutableTreeNode getNewNode(Object o, boolean allowsChildren) {
    return new DefaultMutableTreeNode(new ContextTaskData(o), allowsChildren);
  }

  /**
   * 过滤TimeInterval内的节点
   */
  public static void filterNodeInTimeInterval() {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) DataCenter.TREE_MODEL.getRoot();
    for (int i = 0; i < root.getChildCount(); i++) {
      DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) root.getChildAt(i);
      ContextTaskData userObject = (ContextTaskData) childAt.getUserObject();
      long time = userObject.getCaptureTime().getTime();
      long nowTime = new Date().getTime();
      if (time < (nowTime - DataCenter.TIME_INTERVAL * 1000L)) {
        DataCenter.TREE_MODEL.removeNodeFromParent(childAt);
        i--;
      } else {
        for (int j = 0; j < childAt.getChildCount(); j++) {
          DefaultMutableTreeNode childAt1 = (DefaultMutableTreeNode) childAt.getChildAt(j);
          ContextTaskData userObject1 = (ContextTaskData) childAt1.getUserObject();
          long time1 = userObject1.getCaptureTime().getTime();
          long nowTime1 = new Date().getTime();
          if (time1 < (nowTime1 - DataCenter.TIME_INTERVAL * 1000L)) {
            DataCenter.TREE_MODEL.removeNodeFromParent(childAt1);
            j--;
          }
        }
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
    DataCenter.TREE_MODEL.insertNodeInto(newNode, getTreeModelRoot(), getTreeModelRoot().getChildCount());
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
      DataCenter.TREE_MODEL.insertNodeInto(newNode, getTreeModelRoot(), getTreeModelRoot().getChildCount());
    }
    DefaultMutableTreeNode findNode = findExist(containingClass);
    if (findNode == null) {
      DefaultMutableTreeNode newClass = getNewNode(containingClass, true);
      DefaultMutableTreeNode newField = getNewNode(psiField, false);
      newClass.add(newField);
      DataCenter.TREE_MODEL.insertNodeInto(newClass, getTreeModelRoot(), getTreeModelRoot().getChildCount());
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
      DataCenter.TREE_MODEL.insertNodeInto(newNode, getTreeModelRoot(), getTreeModelRoot().getChildCount());
    }
    DefaultMutableTreeNode findNode = findExist(containingClass);
    if (findNode == null) {
      DefaultMutableTreeNode newClass = getNewNode(containingClass, true);
      DefaultMutableTreeNode newMethod = getNewNode(psiMethod, false);
      newClass.add(newMethod);
      DataCenter.TREE_MODEL.insertNodeInto(newClass, getTreeModelRoot(), getTreeModelRoot().getChildCount());
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
      DataCenter.TREE_MODEL.insertNodeInto(newNode, getTreeModelRoot(), getTreeModelRoot().getChildCount());
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
    DefaultMutableTreeNode root = getTreeModelRoot();
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
    DefaultMutableTreeNode root = getTreeModelRoot();
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
