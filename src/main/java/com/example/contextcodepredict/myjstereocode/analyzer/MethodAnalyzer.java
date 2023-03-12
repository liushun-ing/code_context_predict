package com.example.contextcodepredict.myjstereocode.analyzer;

import com.example.contextcodepredict.myjstereocode.info.TypeInfo;
import com.example.contextcodepredict.myjstereocode.info.VariableInfo;
import com.intellij.psi.*;
import com.siyeh.ig.psiutils.ClassUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 方法分析器
 */
public class MethodAnalyzer {
  private boolean isConstructor = false;
  private boolean isAnonymous = false;
  private boolean hasBody = false;
  private boolean hasStatements = false;
  private boolean usesNonStaticFinalFields = false;
  private boolean overridesClone = false;
  private boolean overridesFinalize = false;
  private boolean overridesToString = false;
  private boolean overridesHashCode = false;
  private boolean overridesEquals = false;
  private PsiType returnType;
  private PsiClass declaringClass;
  private Set<PsiField> getFields = new HashSet<>();
  private Set<PsiField> propertyFields = new HashSet<>();
  private Set<PsiField> voidAccessorFields = new HashSet<>();
  private Set<PsiField> setFields = new HashSet<>();
  private List<VariableInfo> parameters = new LinkedList<>();
  private List<VariableInfo> variables = new LinkedList<>();
  private boolean instantiatedReturn = false;
  private Set<PsiMethod> invokedLocalMethods = new HashSet<>();
  private Set<PsiMethod> invokedExternalMethods = new HashSet<>();
  private List<TypeInfo> usedTypes = new LinkedList<>();

  public MethodAnalyzer(PsiMethod method) {
    method.accept(new MethodVisitor(method));
  }

  public boolean isConstructor() {
    return this.isConstructor;
  }

  public boolean isAnonymous() {
    return this.isAnonymous;
  }

  public boolean hasBody() {
    return this.hasBody;
  }

  public boolean hasStatements() {
    return this.hasStatements;
  }

  public boolean usesFields() {
    return this.usesNonStaticFinalFields;
  }

  public boolean overridesClone() {
    return this.overridesClone;
  }

  public boolean overridesFinalize() {
    return this.overridesFinalize;
  }

  public boolean overridesToString() {
    return this.overridesToString;
  }

  public boolean overridesHashCode() {
    return this.overridesHashCode;
  }

  public boolean overridesEquals() {
    return this.overridesEquals;
  }

  public PsiType getReturnType() {
    return this.returnType;
  }

  public PsiClass getDeclaringClass() {
    return this.declaringClass;
  }

  public Set<PsiField> getGetFields() {
    return this.getFields;
  }

  public Set<PsiField> getPropertyFields() {
    return this.propertyFields;
  }

  public Set<PsiField> getVoidAccessorFields() {
    return this.voidAccessorFields;
  }

  public Set<PsiField> getSetFields() {
    return this.setFields;
  }

  public List<VariableInfo> getParameters() {
    return this.parameters;
  }

  public List<VariableInfo> getVariables() {
    return this.variables;
  }

  public boolean isInstantiatedReturn() {
    return this.instantiatedReturn;
  }

  public Set<PsiMethod> getInvokedLocalMethods() {
    return this.invokedLocalMethods;
  }

  public Set<PsiMethod> getInvokedExternalMethods() {
    return this.invokedExternalMethods;
  }

  public List<TypeInfo> getUsedTypes() {
    return this.usedTypes;
  }

  private void checkNonFinalStaticFieldUse(PsiField field) {
    if (field != null
        && field.getModifierList() != null
        && (!field.getModifierList().hasExplicitModifier(PsiModifier.FINAL)
        || !field.getModifierList().hasExplicitModifier(PsiModifier.STATIC))) {
      this.usesNonStaticFinalFields = true;
    }
  }

  private class MethodVisitor extends JavaRecursiveElementVisitor {
    private int inReturn = 0;
    private int inAssignmentRightSide = 0;
    private int inCondition = 0;
    private int inMethodArguments = 0;
    private int assignedVariableIndex = -1;
    private int assignedParameterIndex = -1;

    public MethodVisitor(PsiMethod node) {
      MethodAnalyzer.this.declaringClass = node.getContainingClass();
      MethodAnalyzer.this.isAnonymous = MethodAnalyzer.this.declaringClass instanceof PsiAnonymousClass;
      if (node.getBody() != null) {
        MethodAnalyzer.this.hasBody = true;
        if (node.getBody().getStatementCount() > 0) {
          MethodAnalyzer.this.hasStatements = true;
        }
      }
      MethodAnalyzer.this.isConstructor = node.isConstructor();
      MethodAnalyzer.this.returnType = node.getReturnType();
      MethodAnalyzer.this.overridesClone = node.getName().contains("clone");
      MethodAnalyzer.this.overridesFinalize = node.getName().contains("finalize");
      MethodAnalyzer.this.overridesToString = node.getName().contains("toString");
      MethodAnalyzer.this.overridesEquals = node.getName().contains("equals");
      MethodAnalyzer.this.overridesHashCode = node.getName().contains("hashCode");
      PsiParameterList parameterList = node.getParameterList();
      for (PsiParameter p : parameterList.getParameters()) {
        MethodAnalyzer.this.parameters.add(new VariableInfo(p));
      }
    }

    @Override
    public void visitVariable(PsiVariable variable) {
      boolean isInstantiated = false;
      if (variable.hasInitializer()) {
        isInstantiated = variable.getInitializer() instanceof PsiClassInitializer;
      }
      MethodAnalyzer.this.variables.add(new VariableInfo(variable, isInstantiated));
      super.visitVariable(variable);
    }

    @Override
    public void visitConditionalExpression(PsiConditionalExpression expression) {
      ++this.inCondition;
      expression.getCondition().accept(this);
      --this.inCondition;
      if (expression.getThenExpression() != null) {
        expression.getThenExpression().accept(this);
      }
      if (expression.getElseExpression() != null) {
        expression.getElseExpression().accept(this);
      }
      super.visitConditionalExpression(expression);
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
      super.visitNewExpression(expression);
      if (this.inReturn > 0
          && this.inMethodArguments == 0
          && this.inCondition == 0
          && MethodAnalyzer.this.returnType != null
          && !ClassUtils.isPrimitive(MethodAnalyzer.this.returnType)) {
        MethodAnalyzer.this.instantiatedReturn = true;
      }
    }

    @Override
    public void visitReturnStatement(PsiReturnStatement statement) {
      ++this.inReturn;
      PsiField field = this.getLocalField(statement.getReturnValue());
      if (field != null) {
        MethodAnalyzer.this.getFields.add(field);
      } else {
        int index = this.getVariableIndex(statement.getReturnValue());
        if (index != -1
            && MethodAnalyzer.this.variables.get(index).isInstantiated()
            && !ClassUtils.isPrimitive(MethodAnalyzer.this.returnType)) {
          MethodAnalyzer.this.instantiatedReturn = true;
          MethodAnalyzer.this.variables.get(index).setReturned(true);
        } else {
          PsiField var;
          if (index != -1) {
            if (!MethodAnalyzer.this.variables.get(index).getAssignedFields().isEmpty()) {
              for (PsiVariable psiVariable : MethodAnalyzer.this.variables.get(index).getAssignedFields()) {
                var = (PsiField) psiVariable;
                MethodAnalyzer.this.propertyFields.add(var);
              }
            }
            MethodAnalyzer.this.variables.get(index).setReturned(true);
          } else {
            index = this.getParameterIndex(statement.getReturnValue());
            if (index != -1) {
              for (PsiVariable psiVariable : MethodAnalyzer.this.parameters.get(index).getAssignedFields()) {
                var = (PsiField) psiVariable;
                MethodAnalyzer.this.propertyFields.add(var);
              }
              MethodAnalyzer.this.parameters.get(index).setReturned(true);
            }
          }
        }
      }
      super.visitReturnStatement(statement);
      --this.inReturn;
    }

    @Override
    public void visitAssignmentExpression(PsiAssignmentExpression expression) {
      PsiField field = this.getLocalField(expression.getLExpression());
      if (field != null) {
        MethodAnalyzer.this.setFields.add(field);
      }
      this.assignedVariableIndex = this.getVariableIndex(expression.getLExpression());
      if (this.assignedVariableIndex != -1) {
        MethodAnalyzer.this.variables.get(this.assignedVariableIndex)
            .setModified(true);
      }
      this.assignedParameterIndex = this.getParameterIndex(expression.getLExpression());
      if (this.assignedParameterIndex != -1) {
        MethodAnalyzer.this.parameters.get(this.assignedParameterIndex)
            .setModified(true);
      }

      expression.getLExpression().accept(this);
      ++this.inAssignmentRightSide;
      if (expression.getRExpression() != null) {
        expression.getRExpression().accept(this);
      }
      --this.inAssignmentRightSide;

      super.visitAssignmentExpression(expression);
      this.assignedVariableIndex = -1;
      this.assignedParameterIndex = -1;
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
      super.visitMethodCallExpression(expression);
      if (this.inReturn > 0) {
        PsiField field = this.getLocalField(expression.getMethodExpression());
        if (field != null) {
          MethodAnalyzer.this.propertyFields.add(field);
        }
      }

      if (expression.getReference() != null && this.isLocalMethod(expression)) {
        MethodAnalyzer.this.invokedLocalMethods.add(expression.resolveMethod());
      }

      if (expression.getMethodExpression() instanceof PsiIdentifier) {
        PsiIdentifier name = (PsiIdentifier) expression.getMethodExpression();
        if (name.getReference() != null && name.getReference().resolve() instanceof PsiClass) {
          MethodAnalyzer.this.invokedExternalMethods.add(expression.resolveMethod());
        }
      }

      expression.getMethodExpression().accept(this);
      ++this.inMethodArguments;
      PsiExpression[] var3 = expression.getArgumentList().getExpressions();
      for (PsiExpression pe : var3) {
        pe.accept(this);
      }

      --this.inMethodArguments;
    }

    @Override
    public void visitExpressionStatement(PsiExpressionStatement statement) {
      super.visitExpressionStatement(statement);
      if (statement.getExpression() instanceof PsiMethodCallExpression) {
        PsiMethodCallExpression method = (PsiMethodCallExpression) statement.getExpression();

        PsiExpression expression;
        for (expression = method.getMethodExpression(); expression instanceof PsiMethodCallExpression; expression = ((PsiMethodCallExpression) expression).getMethodExpression()) {}

        PsiField field = this.getLocalField(expression);
        if (field != null) {
          MethodAnalyzer.this.setFields.add(field);
        }

        int index = this.getVariableIndex(method.getMethodExpression());
        if (index != -1) {
          MethodAnalyzer.this.variables.get(index).setModified(true);
        } else {
          index = this.getParameterIndex(method.getMethodExpression());
          if (index != -1) {
            MethodAnalyzer.this.parameters.get(index).setModified(true);
          }
        }

        if (this.isLocalMethod(method)) {
          MethodAnalyzer.this.invokedLocalMethods.add(method.resolveMethod());
        }
      }
    }

    private void addUsedType(PsiClass typeBinding) {
      TypeInfo type = new TypeInfo(typeBinding);
      if (MethodAnalyzer.this.usedTypes.contains(type)) {
        MethodAnalyzer.this.usedTypes.get(MethodAnalyzer.this.usedTypes.indexOf(type)).incrementFrequency();
      } else {
        MethodAnalyzer.this.usedTypes.add(type);
      }
    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression expression) {
      super.visitReferenceExpression(expression);
      PsiField field = this.getLocalField(expression);
      MethodAnalyzer.this.checkNonFinalStaticFieldUse(field);
      if (field != null) {
        if (this.inAssignmentRightSide > 0 && this.assignedVariableIndex != -1) {
          ((VariableInfo) MethodAnalyzer.this.variables.get(this.assignedVariableIndex)).addAssignedField(field);
          super.visitReferenceExpression(expression);
        }

        if (this.inAssignmentRightSide > 0 && this.assignedParameterIndex != -1 && !ClassUtils.isPrimitive(((VariableInfo) MethodAnalyzer.this.parameters.get(this.assignedParameterIndex)).getVariableBinding().getType())) {
          ((VariableInfo) MethodAnalyzer.this.parameters.get(this.assignedParameterIndex)).addAssignedField(field);
          MethodAnalyzer.this.voidAccessorFields.add(field);
          super.visitReferenceExpression(expression);
        }

        if (this.inReturn > 0 && !(expression.getParent() instanceof PsiReturnStatement) && this.inMethodArguments == 0) {
          if (expression.getReference() != null) {
            MethodAnalyzer.this.propertyFields.add((PsiField) expression.getReference().resolve());
            super.visitReferenceExpression(expression);
          }
        }
      }

      if (expression.getReference() != null && expression.getReference().resolve() instanceof PsiClass) {
        PsiClass typeBinding = (PsiClass) expression.getReference().resolve();
        this.addUsedType(typeBinding);
      }
    }

    @Override
    public void visitThisExpression(PsiThisExpression expression) {
      super.visitThisExpression(expression);
      MethodAnalyzer.this.checkNonFinalStaticFieldUse(this.getLocalField(expression));
    }

    @Override
    public void visitSuperExpression(PsiSuperExpression expression) {
      super.visitSuperExpression(expression);
      MethodAnalyzer.this.checkNonFinalStaticFieldUse(this.getLocalField(expression));
    }

    @Override
    public void visitArrayAccessExpression(PsiArrayAccessExpression expression) {
      super.visitArrayAccessExpression(expression);
      MethodAnalyzer.this.checkNonFinalStaticFieldUse(this.getLocalField(expression));
    }

    private PsiField getLocalField(PsiExpression expression) {
      if (expression == null) {
        return null;
      }
      PsiElement variableBinding = null;
      if (expression.getReference() != null && expression.getReference().resolve() != null) {
        variableBinding = expression.getReference().resolve();
      }

      if (variableBinding instanceof PsiField && this.isLocalField((PsiField) variableBinding, MethodAnalyzer.this.declaringClass)) {
        return (PsiField) variableBinding;
      } else {
        if (expression instanceof PsiArrayAccessExpression) {
          PsiArrayAccessExpression arrayAccess = (PsiArrayAccessExpression) expression;
          return this.getLocalField(arrayAccess.getArrayExpression());
        } else if (expression instanceof PsiReferenceExpression) {
          PsiReferenceExpression qualifiedName = (PsiReferenceExpression) expression;
          if (qualifiedName.getQualifierExpression() != null) {
            return this.getLocalField(qualifiedName.getQualifierExpression());
          } else {
            return null;
          }
        } else {
          return null;
        }
      }
    }

    private boolean isLocalField(PsiField field, PsiClass relatedType) {
      if (field.getContainingClass() != null) {
        if (field.getContainingClass().equals(relatedType)) {
          return true;
        }

        if (relatedType != null && relatedType.getContainingClass() != null && relatedType.getContainingClass().findInnerClassByName(relatedType.getName(), false) != null
            && relatedType.getModifierList() != null && !relatedType.getModifierList().hasExplicitModifier(PsiModifier.STATIC) && this.isLocalField(field, relatedType.getContainingClass())) {
          return true;
        }

        if (relatedType.getSuperClass() != null && this.isLocalField(field, relatedType.getSuperClass())) {
          return true;
        }

        PsiClass[] var6;
        int var5 = (var6 = relatedType.getInterfaces()).length;

        for (int var4 = 0; var4 < var5; ++var4) {
          PsiClass interfaceType = var6[var4];
          if (this.isLocalField(field, interfaceType)) {
            return true;
          }
        }
      }
      return false;
    }

    private int getVariableIndex(PsiExpression expression) {
      if (expression instanceof PsiIdentifier) {
        PsiIdentifier simpleName = (PsiIdentifier) expression;
        if (simpleName.getReference() != null && simpleName.getReference().resolve() instanceof PsiVariable) {
          VariableInfo var = new VariableInfo((PsiVariable) simpleName.getReference().resolve());
          if (MethodAnalyzer.this.variables.contains(var)) {
            return MethodAnalyzer.this.variables.indexOf(var);
          }
        }
      }

      if (expression instanceof PsiQualifiedExpression) {
        PsiQualifiedExpression simpleName = (PsiQualifiedExpression) expression;
        if (simpleName.getReference() != null && simpleName.getReference().resolve() instanceof PsiVariable) {
          VariableInfo var = new VariableInfo((PsiVariable) simpleName.getReference().resolve());
          if (MethodAnalyzer.this.variables.contains(var)) {
            return MethodAnalyzer.this.variables.indexOf(var);
          }
        }
      }

      if (expression instanceof PsiReferenceExpression) {
        PsiReferenceExpression qualifiedName = (PsiReferenceExpression) expression;
        return this.getVariableIndex(qualifiedName.getQualifierExpression());
      } else {
        return -1;
      }
    }

    private int getParameterIndex(PsiExpression expression) {
      if (expression instanceof PsiIdentifier) {
        PsiIdentifier simpleName = (PsiIdentifier) expression;
        if (simpleName.getReference() != null && simpleName.getReference().resolve() instanceof PsiVariable) {
          VariableInfo var = new VariableInfo((PsiVariable) simpleName.getReference().resolve());
          if (MethodAnalyzer.this.parameters.contains(var)) {
            return MethodAnalyzer.this.parameters.indexOf(var);
          }
        }
      }
      if (expression instanceof PsiQualifiedExpression) {
        PsiQualifiedExpression simpleName = (PsiQualifiedExpression) expression;
        if (simpleName.getReference() != null && simpleName.getReference().resolve() instanceof PsiVariable) {
          VariableInfo var = new VariableInfo((PsiVariable) simpleName.getReference().resolve());
          if (MethodAnalyzer.this.parameters.contains(var)) {
            return MethodAnalyzer.this.parameters.indexOf(var);
          }
        }
      }

      if (expression instanceof PsiReferenceExpression) {
        PsiReferenceExpression qualifiedName = (PsiReferenceExpression) expression;
        return this.getParameterIndex(qualifiedName.getQualifierExpression());
      } else {
        return -1;
      }
    }

    /**
     * 判断是否是本地方法
     *
     * @param method 方法调用表达式
     * @return 是否为本地方法
     */
    private boolean isLocalMethod(PsiMethodCallExpression method) {
      if (method.getReference() == null) {
        return false;
      }
      PsiMethod methodBinding = (PsiMethod) method.getReference().resolve();
      if (methodBinding != null && methodBinding.getContainingClass() != null) {
        if (methodBinding.getContainingClass().equals(MethodAnalyzer.this.declaringClass)) {
          return true;
        }

        PsiClass relatedClass = MethodAnalyzer.this.declaringClass;
        while (relatedClass.getContainingClass() != null && relatedClass.getContainingClass().findInnerClassByName(relatedClass.getName(), false) != null
            && relatedClass.getModifierList() != null && !relatedClass.getModifierList().hasExplicitModifier(PsiModifier.STATIC)) {
          relatedClass = relatedClass.getContainingClass();
          if (relatedClass == null) {
            break;
          }

          if (methodBinding.getContainingClass().equals(relatedClass)) {
            return true;
          }
        }

        for (relatedClass = MethodAnalyzer.this.declaringClass.getSuperClass(); relatedClass != null; relatedClass = relatedClass.getSuperClass()) {
          if (methodBinding.getContainingClass().equals(relatedClass)) {
            return true;
          }
        }
      }

      return false;
    }

    @Override
    public void visitLambdaExpression(PsiLambdaExpression expression) {
    }

    @Override
    public void visitClass(PsiClass aClass) {
    }
  }
}
