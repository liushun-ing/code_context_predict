package com.example.contextcodepredict.myjstereocode.info;

import com.intellij.psi.PsiClass;

/**
 * PsiClass的信息，包括PsiClass和使用频率
 */
public class TypeInfo {
  private PsiClass typeBinding;
  private int frequency;

  public TypeInfo(PsiClass type) {
    this.typeBinding = type;
    this.frequency = 1;
  }

  public PsiClass getTypeBinding() {
    return this.typeBinding;
  }

  public int getFrequency() {
    return this.frequency;
  }

  public void incrementFrequency() {
    ++this.frequency;
  }

  public void incrementFrequencyBy(int x) {
    this.frequency += x;
  }

  public int hashCode() {
    int result = 1;
    result = 31 * result + (this.typeBinding == null ? 0 : this.typeBinding.hashCode());
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
      TypeInfo other = (TypeInfo) obj;
      if (this.typeBinding == null) {
        return other.typeBinding == null;
      } else return this.typeBinding.equals(other.typeBinding);
    }
  }

//  public static class TypeInformationComparator implements Comparator<TypeInfo> {
//    public TypeInformationComparator() {
//    }
//
//    public int compare(TypeInfo o1, TypeInfo o2) {
//      Integer freq1 = o1.getFrequency();
//      Integer freq2 = o2.getFrequency();
//      return freq2.compareTo(freq1);
//    }
//  }
}
