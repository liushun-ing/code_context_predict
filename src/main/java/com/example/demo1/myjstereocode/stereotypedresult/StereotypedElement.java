package com.example.demo1.myjstereocode.stereotypedresult;

import com.example.demo1.myjstereocode.stereotype.CodeStereotype;
import com.intellij.psi.PsiElement;

import java.util.List;

/**
 * 分配好的原型的元素的基类
 */
public interface StereotypedElement {
  PsiElement getElement();

  List<CodeStereotype> getStereotypes();

  List<StereotypedElement> getStereoSubElements();

  List<StereotypedElement> getStereoSubFields();

  String getStereotypesName();

  void findStereotypes();

  String getName();

}