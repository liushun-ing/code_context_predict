package com.example.demo1.data;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaFile;

/**
 * 建议结果数据的对象
 */
public class SuggestionData {
  private PsiElement element;
  private String packagePath;
  private String stereotype;
  private String confidence;

  public SuggestionData() {
  }

  public SuggestionData(PsiElement element, String stereotype, String confidence) {
    PsiJavaFile javaFile = (PsiJavaFile) element.getContainingFile();
    this.packagePath = javaFile.getPackageName();
    this.element = element;
    this.stereotype = stereotype;
    this.confidence = confidence;
  }

  public String getPackagePath() {
    return packagePath;
  }

  public void setPackagePath(String packagePath) {
    this.packagePath = packagePath;
  }

  public PsiElement getElement() {
    return element;
  }

  public void setElement(PsiElement element) {
    this.element = element;
  }

  public String getStereotype() {
    return stereotype;
  }

  public void setStereotype(String stereotype) {
    this.stereotype = stereotype;
  }

  public String getConfidence() {
    return confidence;
  }

  public void setConfidence(String confidence) {
    this.confidence = confidence;
  }

  @Override
  public String toString() {
    return "SuggestionData{" +
        "element=" + element +
        ", packagePath='" + packagePath + '\'' +
        ", stereotype='" + stereotype + '\'' +
        ", confidence='" + confidence + '\'' +
        '}';
  }
}
