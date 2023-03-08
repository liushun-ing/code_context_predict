package com.example.demo1.myjstereocode.stereotype;

/**
 * 类原型
 */
public enum TypeStereotype implements CodeStereotype {
  ENTITY,
  MINIMAL_ENTITY,
  DATA_PROVIDER,
  COMMANDER,
  BOUNDARY,
  FACTORY,
  CONTROLLER,
  PURE_CONTROLLER,
  LARGE_CLASS,
  LAZY_CLASS,
  DEGENERATE,
  DATA_CLASS,
  POOL,
  INTERFACE;

  private TypeStereotype() {
  }
}
