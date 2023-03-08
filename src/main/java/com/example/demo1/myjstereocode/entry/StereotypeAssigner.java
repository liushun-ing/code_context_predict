package com.example.demo1.myjstereocode.entry;

import com.example.demo1.myjstereocode.stereotypedresult.StereotypedElement;
import com.example.demo1.myjstereocode.stereotypedresult.StereotypedField;
import com.example.demo1.myjstereocode.stereotypedresult.StereotypedMethod;
import com.example.demo1.myjstereocode.stereotypedresult.StereotypedType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

/**
 * 原型分配器
 */
public class StereotypeAssigner {
  /**
   * 每个类方法个数平均值，分布平均值
   */
  double methodsMean;
  /**
   * 每个类方法个数分布的方差
   */
  double methodsStdDev;

  public StereotypeAssigner() {
  }

  /**
   * 设置项目的方法方法参数
   *
   * @param methodsMean   方法个数平均值
   * @param methodsStdDev 方法分布的方差
   */
  public void setParameters(double methodsMean, double methodsStdDev) {
    this.methodsMean = methodsMean;
    this.methodsStdDev = methodsStdDev;
  }

  /**
   * 为节点分配原型
   *
   * @param psiElement 需要分配原型的节点
   * @return 原型字符串
   */
  public String assignStereotypes(PsiElement psiElement) {
    try {
      StereotypedElement stereoElement = null;
      if (psiElement instanceof PsiField) {
        stereoElement = new StereotypedField((PsiField) psiElement);
      }
      if (psiElement instanceof PsiClass) {
        stereoElement = new StereotypedType((PsiClass) psiElement, this.methodsMean, this.methodsStdDev);
      } else if (psiElement instanceof PsiMethod) {
        stereoElement = new StereotypedMethod((PsiMethod) psiElement);
      }
      if (stereoElement != null) {
        stereoElement.findStereotypes();
        return stereoElement.getStereotypesName();
      }
    } catch (NullPointerException var4) {
      System.out.println("出错啦！" + var4.getMessage());
    }
    return "NULL";
  }
}
