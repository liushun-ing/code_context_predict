package com.example.demo1.myjstereocode.stereotype;

/**
 * 方法原型
 */
public enum MethodStereotype implements CodeStereotype {
  GET(Category.ACCESSOR, Subcategory.GET),
  PREDICATE(Category.ACCESSOR, Subcategory.PREDICATE),
  PROPERTY(Category.ACCESSOR, Subcategory.PROPERTY),
  VOID_ACCESSOR(Category.ACCESSOR, Subcategory.VOID_ACCESSOR),
  SET(Category.MUTATOR, Subcategory.SET),
  COMMAND(Category.MUTATOR, Subcategory.COMMAND),
  NON_VOID_COMMAND(Category.MUTATOR, Subcategory.NON_VOID_COMMAND),
  CONSTRUCTOR(Category.CREATIONAL, Subcategory.CONSTRUCTOR),
  COPY_CONSTRUCTOR(Category.CREATIONAL, Subcategory.COPY_CONSTRUCTOR),
  DESTRUCTOR(Category.CREATIONAL, Subcategory.DESTRUCTOR),
  FACTORY(Category.CREATIONAL, Subcategory.FACTORY),
  COLLABORATOR(Category.COLLABORATIONAL, Subcategory.COLLABORATOR),
  CONTROLLER(Category.COLLABORATIONAL, Subcategory.CONTROLLER),
  LOCAL_CONTROLLER(Category.COLLABORATIONAL, Subcategory.LOCAL_CONTROLLER),
  INCIDENTAL(Category.DEGENERATE, Subcategory.INCIDENTAL),
  EMPTY(Category.DEGENERATE, Subcategory.EMPTY),
  ABSTRACT(Category.DEGENERATE, Subcategory.ABSTRACT);

  private final Category category;
  private final Subcategory subcategory;

  private MethodStereotype(Category category, Subcategory subcategory) {
    this.category = category;
    this.subcategory = subcategory;
  }

  public Category getCategory() {
    return this.category;
  }

  public Subcategory getSubcategory() {
    return this.subcategory;
  }

  // 五个大类
  public static enum Category {
    ACCESSOR,
    MUTATOR,
    CREATIONAL,
    COLLABORATIONAL,
    DEGENERATE;

    private Category() {
    }
  }

  // 十七的小类
  public static enum Subcategory {
    GET,
    PREDICATE,
    PROPERTY,
    VOID_ACCESSOR,
    SET,
    COMMAND,
    NON_VOID_COMMAND,
    CONSTRUCTOR,
    COPY_CONSTRUCTOR,
    DESTRUCTOR,
    FACTORY,
    COLLABORATOR,
    CONTROLLER,
    LOCAL_CONTROLLER,
    INCIDENTAL,
    EMPTY,
    ABSTRACT;

    private Subcategory() {
    }
  }
}

