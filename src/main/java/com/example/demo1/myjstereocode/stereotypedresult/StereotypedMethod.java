package com.example.demo1.myjstereocode.stereotypedresult;

import com.example.demo1.myjstereocode.analyzer.MethodAnalyzer;
import com.example.demo1.myjstereocode.info.TypeInfo;
import com.example.demo1.myjstereocode.rules.MethodStereotypeRules;
import com.example.demo1.myjstereocode.stereotype.CodeStereotype;
import com.example.demo1.myjstereocode.stereotype.MethodStereotype;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 分配好原型的METHOD
 */
public class StereotypedMethod extends MethodStereotypeRules implements StereotypedElement {
  private PsiMethod method;
  private MethodStereotype primaryStereotype;
  private MethodStereotype secondaryStereotype;

  public StereotypedMethod(PsiMethod method) {
    this.method = method;
  }

  public List<CodeStereotype> getStereotypes() {
    ArrayList<CodeStereotype> stereotypes = new ArrayList<>();
    if (this.primaryStereotype != null) {
      stereotypes.add(this.primaryStereotype);
    }
    if (this.secondaryStereotype != null) {
      stereotypes.add(this.secondaryStereotype);
    }
    return stereotypes;
  }

  public String getStereotypesName() {
    if (this.secondaryStereotype != null) {
      return this.primaryStereotype.toString() + "-" + this.secondaryStereotype.toString();
    }
    if (this.primaryStereotype != null)
      return primaryStereotype.toString();
    else return "NULL";
  }

  public List<StereotypedElement> getStereoSubElements() {
    return new ArrayList<>();
  }

  public PsiMethod getElement() {
    return this.method;
  }

  public Set<PsiField> getGetFields() {
    return this.methodAnalyzer.getGetFields();
  }

  public Set<PsiField> getPropertyFields() {
    return this.methodAnalyzer.getPropertyFields();
  }

  public Set<PsiField> getVoidAccessorFields() {
    return this.methodAnalyzer.getVoidAccessorFields();
  }

  public boolean isAnonymous() {
    return this.methodAnalyzer.isAnonymous();
  }

  public List<TypeInfo> getUsedTypes() {
    return this.methodAnalyzer.getUsedTypes();
  }

  public boolean overridesObjectMethod() {
    return this.methodAnalyzer.overridesClone() || this.methodAnalyzer.overridesFinalize() || this.methodAnalyzer.overridesToString() || this.methodAnalyzer.overridesEquals() || this.methodAnalyzer.overridesHashCode();
  }

  public void findStereotypes() {

    try {
      this.methodAnalyzer = new MethodAnalyzer(this.method);
      this.primaryStereotype = this.findPrimaryStereotype();
      if (!this.primaryStereotype.getCategory().equals(MethodStereotype.Category.COLLABORATIONAL) && !this.primaryStereotype.getCategory().equals(MethodStereotype.Category.DEGENERATE)) {
        this.secondaryStereotype = this.findSecondaryStereotype();
      }
    } catch (NullPointerException var2) {
      this.primaryStereotype = MethodStereotype.INCIDENTAL;
    }

  }

  private MethodStereotype findPrimaryStereotype() {
    MethodStereotype stereotype = this.checkForAbstract();
    if (stereotype != null) {
      return stereotype;
    } else {
      stereotype = this.checkForCreationalStereotype();
      if (stereotype != null) {
        return stereotype;
      } else {
        stereotype = this.checkForEmpty();
        if (stereotype != null) {
          return stereotype;
        } else {
          stereotype = this.checkForMutatorStereotype();
          if (stereotype != null) {
            return stereotype;
          } else {
            stereotype = this.checkForAccessorStereotype();
            if (stereotype != null) {
              return stereotype;
            } else {
              stereotype = this.checkForCollaborationalStereotype(true);
              return stereotype != null ? stereotype : MethodStereotype.INCIDENTAL;
            }
          }
        }
      }
    }
  }

  private MethodStereotype findSecondaryStereotype() {
    return this.checkForCollaborationalStereotype(false);
  }

  public boolean isGet() {
    return this.isInSubcategory(MethodStereotype.GET);
  }

  public boolean isPredicate() {
    return this.isInSubcategory(MethodStereotype.PREDICATE);
  }

  public boolean isProperty() {
    return this.isInSubcategory(MethodStereotype.PROPERTY);
  }

  public boolean isVoidAccessor() {
    return this.isInSubcategory(MethodStereotype.VOID_ACCESSOR);
  }

  public boolean isSet() {
    return this.isInSubcategory(MethodStereotype.SET);
  }

  public boolean isCommand() {
    return this.isInSubcategory(MethodStereotype.COMMAND);
  }

  public boolean isNonVoidCommand() {
    return this.isInSubcategory(MethodStereotype.NON_VOID_COMMAND);
  }

  public boolean isConstructor() {
    return this.isInSubcategory(MethodStereotype.CONSTRUCTOR);
  }

  public boolean isCopyConstructor() {
    return this.isInSubcategory(MethodStereotype.COPY_CONSTRUCTOR);
  }

  public boolean isDestructor() {
    return this.isInSubcategory(MethodStereotype.DESTRUCTOR);
  }

  public boolean isFactory() {
    return this.isInSubcategory(MethodStereotype.FACTORY);
  }

  public boolean isCollaborator() {
    return this.isInSubcategory(MethodStereotype.COLLABORATOR);
  }

  public boolean isLocalController() {
    return this.isInSubcategory(MethodStereotype.LOCAL_CONTROLLER);
  }

  public boolean isController() {
    return this.isInSubcategory(MethodStereotype.CONTROLLER);
  }

  public boolean isIncidental() {
    return this.isInSubcategory(MethodStereotype.INCIDENTAL);
  }

  public boolean isEmpty() {
    return this.isInSubcategory(MethodStereotype.EMPTY);
  }

  public boolean isAbstract() {
    return this.isInSubcategory(MethodStereotype.ABSTRACT);
  }

  private boolean isInSubcategory(MethodStereotype stereotype) {
    boolean result = false;
    if (this.primaryStereotype != null) {
      result = this.primaryStereotype.equals(stereotype);
    }
    if (this.secondaryStereotype != null) {
      result = result || this.secondaryStereotype.equals(stereotype);
    }
    return result;
  }

  private boolean isInCategory(MethodStereotype.Category stereotypeCategory) {
    boolean result = false;
    if (this.primaryStereotype != null) {
      result = this.primaryStereotype.getCategory().equals(stereotypeCategory);
    }
    if (this.secondaryStereotype != null) {
      result = result || this.secondaryStereotype.getCategory().equals(stereotypeCategory);
    }
    return result;
  }

  public boolean isAccessor() {
    return this.isInCategory(MethodStereotype.Category.ACCESSOR);
  }

  public boolean isMutator() {
    return this.isInCategory(MethodStereotype.Category.MUTATOR);
  }

  public boolean isCreational() {
    return this.isInCategory(MethodStereotype.Category.CREATIONAL);
  }

  public boolean isCollaborational() {
    return this.isInCategory(MethodStereotype.Category.COLLABORATIONAL);
  }

  public boolean isDegenerate() {
    return this.isInCategory(MethodStereotype.Category.DEGENERATE);
  }

  public int hashCode() {
    boolean prime = true;
    int result = 1;
    result = 31 * result + (this.primaryStereotype == null ? 0 : this.primaryStereotype.hashCode());
    result = 31 * result + (this.secondaryStereotype == null ? 0 : this.secondaryStereotype.hashCode());
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
      StereotypedMethod other = (StereotypedMethod) obj;
      if (this.method == null) {
        if (other.method != null) {
          return false;
        }
      } else if (!this.method.equals(other.method)) {
        return false;
      }
      if (this.primaryStereotype != other.primaryStereotype) {
        return false;
      } else {
        return this.secondaryStereotype == other.secondaryStereotype;
      }
    }
  }

  public String getName() {
    return this.method != null ? this.method.getName() : "";
  }

  @Override
  public List<StereotypedElement> getStereoSubFields() {
    return null;
  }

}
