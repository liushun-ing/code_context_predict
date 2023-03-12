package com.example.contextcodepredict.myjstereocode.info;

import com.intellij.psi.PsiVariable;

import java.util.HashSet;
import java.util.Set;

/**
 * 变量的信息
 */
public class VariableInfo {
  private PsiVariable variableBinding;
  private Set<PsiVariable> assignedFields;
  private boolean isInstantiated;
  private boolean isReturned;
  private boolean isModified;

  public VariableInfo(PsiVariable name) {
    this.variableBinding = name;
    this.isInstantiated = false;
    this.isReturned = false;
    this.isModified = false;
    this.assignedFields = new HashSet<>();
  }

  public VariableInfo(PsiVariable name, boolean isInstantiated) {
    this.variableBinding = name;
    this.isInstantiated = isInstantiated;
    this.isReturned = false;
    this.isModified = false;
    this.assignedFields = new HashSet<>();
  }

  public PsiVariable getVariableBinding() {
    return this.variableBinding;
  }

  public Set<PsiVariable> getAssignedFields() {
    return this.assignedFields;
  }

  public void addAssignedField(PsiVariable field) {
    this.assignedFields.add(field);
  }

  public void setInstantiated(boolean isInstantiated) {
    this.isInstantiated = isInstantiated;
  }

  public void setReturned(boolean isReturned) {
    this.isReturned = isReturned;
  }

  public void setModified(boolean isModified) {
    this.isModified = isModified;
  }

  public boolean isInstantiated() {
    return this.isInstantiated;
  }

  public boolean isReturned() {
    return this.isReturned;
  }

  public boolean isModified() {
    return this.isModified;
  }

  public int hashCode() {
    int result = 1;
    result = 31 * result + (this.variableBinding == null ? 0 : this.variableBinding.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (this.getClass() != obj.getClass()) {
      return false;
    } else {
      VariableInfo other = (VariableInfo) obj;
      if (this.variableBinding == null) {
        return other.variableBinding == null;
      } else return this.variableBinding.equals(other.variableBinding);
    }
  }
}

