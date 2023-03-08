package com.example.demo1.myjstereocode.stereotypedresult;

import com.example.demo1.myjstereocode.analyzer.TypeAnalyzer;
import com.example.demo1.myjstereocode.info.TypeInfo;
import com.example.demo1.myjstereocode.rules.TypeStereotypeRules;
import com.example.demo1.myjstereocode.stereotype.CodeStereotype;
import com.example.demo1.myjstereocode.stereotype.TypeStereotype;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiUtil;

import java.util.*;

public class StereotypedType extends TypeStereotypeRules implements StereotypedElement {
  private Set<PsiField> fields = new HashSet<>();
  private TypeStereotype primaryStereotype;
  private TypeStereotype secondaryStereotype;
  private List<StereotypedMethod> stereotypedMethods = new ArrayList<>();
  private List<StereotypedType> stereotypedSubTypes = new ArrayList<>();
  private List<StereotypedField> stereotypedSubField = new ArrayList<>();
  private List<TypeInfo> relatedTypes = new LinkedList<>();

  public StereotypedType(PsiClass type, double methodsMean, double methodsStdDev) throws NullPointerException {
    super(type, methodsMean, methodsStdDev);
    try {
      type.getQualifiedName();
    } catch (NullPointerException var7) {
      throw new NullPointerException("No type name found");
    }
  }

  public PsiClass getElement() {
    return this.type;
  }

  public List<StereotypedElement> getStereoSubElements() {
    List<StereotypedElement> elements = new ArrayList<>(this.stereotypedMethods);
    elements.addAll(this.stereotypedSubTypes);
    return elements;
  }

  public List<StereotypedElement> getStereoSubFields() {
    return new ArrayList<>(this.stereotypedSubField);
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

  public void findStereotypes() {
    PsiField[] var4;
    int var3 = (var4 = this.type.getFields()).length;
    this.fields.addAll(Arrays.asList(var4).subList(0, var3));
    this.findMethodsStereotypes();
    this.findTypeStereotypes();
    this.findSubtypesStereotypes();
  }

  private void findSubtypesStereotypes() {
    PsiClass[] var4;
    int var3 = (var4 = this.type.getInnerClasses()).length;

    for (int var2 = 0; var2 < var3; ++var2) {
      PsiClass subtype = var4[var2];
      if (!subtype.isInterface() && !subtype.isEnum()) {
        StereotypedType subStereotypedType = new StereotypedType(subtype, this.methodsMean, this.methodsStdDev);
        subStereotypedType.findStereotypes();
        this.stereotypedSubTypes.add(subStereotypedType);
      }
    }

  }

  private void setStereotype(TypeStereotype stereotype) {
    if (this.primaryStereotype == null) {
      this.primaryStereotype = stereotype;
    } else {
      this.secondaryStereotype = stereotype;
    }

  }

  private void findTypeStereotypes() {
    if (this.checkForInterface()) {
      this.setStereotype(TypeStereotype.INTERFACE);
    } else if (this.checkForPool()) {
      this.setStereotype(TypeStereotype.POOL);
    } else if (this.checkForDegenerate()) {
      this.setStereotype(TypeStereotype.DEGENERATE);
    } else if (this.checkForLazyClass()) {
      this.setStereotype(TypeStereotype.LAZY_CLASS);
    } else if (this.checkForLargeClass()) {
      this.setStereotype(TypeStereotype.LARGE_CLASS);
    } else if (this.checkForDataClass()) {
      this.setStereotype(TypeStereotype.DATA_CLASS);
    } else if (this.checkForMinimalEntity()) {
      this.setStereotype(TypeStereotype.MINIMAL_ENTITY);
    } else if (this.checkForEntity()) {
      this.setStereotype(TypeStereotype.ENTITY);
    } else if (this.checkForFactory()) {
      this.setStereotype(TypeStereotype.FACTORY);
    } else if (this.checkForController()) {
      this.setStereotype(TypeStereotype.CONTROLLER);
    } else if (this.checkForPureController()) {
      this.setStereotype(TypeStereotype.PURE_CONTROLLER);
    } else {
      if (this.checkForBoundary()) {
        this.setStereotype(TypeStereotype.BOUNDARY);
      }

      if (this.checkForDataProvider()) {
        this.setStereotype(TypeStereotype.DATA_PROVIDER);
      } else if (this.checkForCommander()) {
        this.setStereotype(TypeStereotype.COMMANDER);
      }
    }
  }

  private void findMethodsStereotypes() {
    TypeAnalyzer analyzer = new TypeAnalyzer(this.type);
    this.stereotypedMethods = analyzer.getStereotypedMethods();
    for (StereotypedMethod stereotypedMethod : this.stereotypedMethods) {
      if (!stereotypedMethod.overridesObjectMethod() && !stereotypedMethod.isConstructor() && !stereotypedMethod.isAnonymous()) {
        this.addRelatedTypes(stereotypedMethod.getUsedTypes());
        this.addMethodToSet(stereotypedMethod);
        this.totalMethods.add(stereotypedMethod);
      }
    }
  }

  private void addRelatedTypes(List<TypeInfo> types) {
    for (TypeInfo type : types) {
      if (this.relatedTypes.contains(type)) {
        this.relatedTypes.get(this.relatedTypes.indexOf(type)).incrementFrequencyBy(type.getFrequency());
      } else if (PsiUtil.getPackageName(type.getTypeBinding()) != null && !"java.lang".equals(PsiUtil.getPackageName(type.getTypeBinding()))) {
        this.relatedTypes.add(type);
      }
    }

  }

  private void addMethodToSet(StereotypedMethod stereotypedMethod) {
    if (stereotypedMethod.isAccessor()) {
      this.accessorMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isMutator()) {
      this.mutatorMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isCollaborational()) {
      this.collaborationalMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isDegenerate()) {
      this.degenerateMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isGet()) {
      this.getMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isPredicate()) {
      this.predicateMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isProperty()) {
      this.propertyMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isVoidAccessor()) {
      this.voidAccessorMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isSet()) {
      this.setMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isCommand()) {
      this.commandMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isNonVoidCommand()) {
      this.nonVoidCommandMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isFactory()) {
      this.factoryMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isCollaborator()) {
      this.collaboratorMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isLocalController()) {
      this.localControllerMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isController()) {
      this.controllerMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isIncidental()) {
      this.incidentalMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isEmpty()) {
      this.emptyMethods.add(stereotypedMethod);
    }
    if (stereotypedMethod.isAbstract()) {
      this.abstractMethods.add(stereotypedMethod);
    }
  }

  public List<StereotypedMethod> getStereotypedMethods() {
    return this.stereotypedMethods;
  }

  public List<StereotypedType> getStereotypedSubTypes() {
    return this.stereotypedSubTypes;
  }

  private boolean typeIs(TypeStereotype stereotype) {
    boolean result = false;
    if (this.primaryStereotype != null) {
      result = this.primaryStereotype.equals(stereotype);
    }
    if (this.secondaryStereotype != null) {
      result = result || this.secondaryStereotype.equals(stereotype);
    }
    return result;
  }

  public boolean isInterface() {
    return this.typeIs(TypeStereotype.INTERFACE);
  }

  public boolean isEntity() {
    return this.typeIs(TypeStereotype.ENTITY);
  }

  public boolean isMinimalEntity() {
    return this.typeIs(TypeStereotype.MINIMAL_ENTITY);
  }

  public boolean isDataProvider() {
    return this.typeIs(TypeStereotype.DATA_PROVIDER);
  }

  public boolean isCommander() {
    return this.typeIs(TypeStereotype.COMMANDER);
  }

  public boolean isBoundary() {
    return this.typeIs(TypeStereotype.BOUNDARY);
  }

  public boolean isFactory() {
    return this.typeIs(TypeStereotype.FACTORY);
  }

  public boolean isController() {
    return this.typeIs(TypeStereotype.CONTROLLER);
  }

  public boolean isPureController() {
    return this.typeIs(TypeStereotype.PURE_CONTROLLER);
  }

  public boolean isLargeClass() {
    return this.typeIs(TypeStereotype.LARGE_CLASS);
  }

  public boolean isLazyClass() {
    return this.typeIs(TypeStereotype.LAZY_CLASS);
  }

  public boolean isDegenerate() {
    return this.typeIs(TypeStereotype.DEGENERATE);
  }

  public boolean isDataClass() {
    return this.typeIs(TypeStereotype.DATA_CLASS);
  }

  public boolean isPool() {
    return this.typeIs(TypeStereotype.POOL);
  }

  public List<TypeInfo> getRelatedTypes() {
    return this.relatedTypes;
  }

  public Set<PsiField> getFields() {
    return this.fields;
  }

  public String getName() {
    return this.type != null ? this.type.getName() : "";
  }

}
