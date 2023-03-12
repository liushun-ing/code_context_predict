package com.example.contextcodepredict.myjstereocode.analyzer;

import com.example.contextcodepredict.myjstereocode.stereotypedresult.StereotypedMethod;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.LinkedList;
import java.util.List;

/**
 * 类的分析器
 */
public class TypeAnalyzer {
  private List<StereotypedMethod> stereotypedMethods = new LinkedList<>();

  public TypeAnalyzer(PsiClass type) {
    TypeVisitor visitor = new TypeVisitor();
    type.accept(visitor);
  }

  public List<StereotypedMethod> getStereotypedMethods() {
    return this.stereotypedMethods;
  }


  private class TypeVisitor extends JavaRecursiveElementVisitor {
    private boolean isRoot;

    private TypeVisitor() {
      this.isRoot = true;
    }

    @Override
    public void visitMethod(PsiMethod method) {
      StereotypedMethod stereotypedMethod = new StereotypedMethod(method);
      stereotypedMethod.findStereotypes();
      TypeAnalyzer.this.stereotypedMethods.add(stereotypedMethod);
      super.visitMethod(method);
    }

    @Override
    public void visitClass(PsiClass aClass) {
      if (this.isRoot) {
        this.isRoot = false;
        super.visitClass(aClass);
      }
    }
  }
}
