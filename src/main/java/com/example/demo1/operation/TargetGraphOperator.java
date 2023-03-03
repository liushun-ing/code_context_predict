package com.example.demo1.operation;

import com.example.demo1.data.DataCenter;
import com.example.demo1.vf3.graph.Edge;
import com.example.demo1.vf3.graph.Graph;
import com.example.demo1.vf3.graph.Vertex;
import com.example.demo1.vf3.utils.EdgeLabel;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;

import java.util.*;

/**
 * 目标图操作类
 */
public class TargetGraphOperator {

  /**
   * 根据psiField构建目标图
   *
   * @param psiField 目标psiField节点
   * @return 目标图
   */
  public static Graph buildTargetGraph(PsiField psiField) {
    if (DataCenter.PROJECT == null || psiField == null) {
      return null;
    }
    // 查找用法
    Query<PsiReference> usagesSearch = ReferencesSearch.search(psiField, GlobalSearchScope.projectScope(DataCenter.PROJECT));
    Collection<PsiReference> allUsages = usagesSearch.findAll();
    Iterator<PsiReference> iterator = allUsages.iterator();
    // 去重
    HashSet<PsiMethod> psiMethods = new HashSet<>();
    while (iterator.hasNext()) {
      PsiElement element = iterator.next().getElement();
      PsiMethod parentOfType = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
      if (parentOfType != null) {
        psiMethods.add(parentOfType);
      }
    }
    // 构建图
    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();
    int id = 1;
    Vertex fieldVertex = new Vertex(id, "FIELD", psiField);
    vertices.add(fieldVertex);
    for (PsiMethod psiMethod : psiMethods) {
      id++;
      Vertex methodVertex = new Vertex(id, "METHOD", psiMethod);
      vertices.add(methodVertex);
      edges.add(new Edge(methodVertex, fieldVertex, EdgeLabel.CALL));
    }
    // 查找谁申明了这个字段
    PsiClass containingClass = psiField.getContainingClass();
    if (elementIsInProject(containingClass)) {
      id++;
      Vertex vertex = new Vertex(id, "CLASS", containingClass);
      vertices.add(vertex);
      edges.add(new Edge(vertex, fieldVertex, EdgeLabel.DECLARE));
    }
    return new Graph(vertices, edges);
  }

  /**
   * 根据psiMethod构建目标图
   *
   * @param psiMethod 目标psiMethod节点
   * @return 目标图
   */
  public static Graph buildTargetGraph(PsiMethod psiMethod) {
    if (DataCenter.PROJECT == null || psiMethod == null) {
      return null;
    }
    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();
    final int[] id = {1};
    Vertex originVertex = new Vertex(id[0], "METHOD", psiMethod);
    vertices.add(originVertex);
    // 查找谁声明了这个方法
    PsiClass containingClass = psiMethod.getContainingClass();
    id[0]++;
    Vertex containClassVertex = new Vertex(id[0], "CLASS", containingClass);
    vertices.add(containClassVertex);
    edges.add(new Edge(containClassVertex, originVertex, EdgeLabel.DECLARE));
    // 查找那些方法调用了这个方法
    Query<PsiReference> usagesSearch = ReferencesSearch.search(psiMethod, GlobalSearchScope.projectScope(DataCenter.PROJECT));
    Collection<PsiReference> allUsages = usagesSearch.findAll();
    HashSet<PsiMethod> useMethods = new HashSet<>();
    // 去重
    for (PsiReference p : allUsages) {
      PsiMethod parentOfType = PsiTreeUtil.getParentOfType(p.getElement(), PsiMethod.class);
      if (parentOfType != null) {
        useMethods.add(parentOfType);
      }
    }
    for (PsiMethod p : useMethods) {
      id[0]++;
      Vertex vertex = new Vertex(id[0], "METHOD", p);
      vertices.add(vertex);
      edges.add(new Edge(vertex, originVertex, EdgeLabel.CALL));
      if (DataCenter.PREDICTION_STEP == 2) {
        // 需要进行进一层的搜索
      }
    }
    // 查找这个方法实现了谁
    PsiMethod[] superMethods = psiMethod.findSuperMethods();
    for (PsiMethod pm : superMethods) {
      id[0]++;
      Vertex vertex = new Vertex(id[0], "METHOD", pm);
      vertices.add(vertex);
      edges.add(new Edge(originVertex, vertex, EdgeLabel.IMPLEMENT));
    }
    // 查找这个方法调用了谁
    psiMethod.accept(new JavaRecursiveElementVisitor() {
      @Override
      public void visitCallExpression(PsiCallExpression expression) {
        super.visitCallExpression(expression);
        // 调用构造方法
        if (expression instanceof PsiNewExpression) {
          PsiNewExpression constructorCall = (PsiNewExpression) expression;
          if (constructorCall.getClassReference() != null) {
            PsiClass callClass = (PsiClass) constructorCall.getClassReference().resolve();
            // 需要判断这个类是否在这个项目中
            if (elementIsInProject(callClass)) {
              id[0]++;
              // 直接与这个类关联
              Vertex callVertex = new Vertex(id[0], "CLASS", callClass);
              vertices.add(callVertex);
              edges.add(new Edge(originVertex, callVertex, EdgeLabel.CALL));
            }
          }
        }
      }

      @Override
      public void visitReferenceExpression(PsiReferenceExpression expression) {
        super.visitReferenceExpression(expression);
        PsiReference reference = expression.getReference();
        if (reference != null) {
          PsiElement referenceElement = reference.resolve();
          if (referenceElement instanceof PsiField) {
            // 引用了某个类的字段
            PsiField callField = (PsiField) referenceElement;
            id[0]++;
            Vertex callVertex = new Vertex(id[0], "FIELD", callField);
            vertices.add(callVertex);
            edges.add(new Edge(originVertex, callVertex, EdgeLabel.CALL));
          } else if (referenceElement instanceof PsiMethod) {
            // 调用了某个方法
            PsiMethod callMethod = (PsiMethod) referenceElement;
            // 只有当这个方法是项目中的某个类声明的才能用作扩展
            if (elementIsInProject(callMethod.getContainingClass())) {
              id[0]++;
              Vertex callVertex = new Vertex(id[0], "METHOD", callMethod);
              vertices.add(callVertex);
              edges.add(new Edge(originVertex, callVertex, EdgeLabel.CALL));
            }
          }
        }
      }
    });

    // 查找谁实现了这个方法
    Collection<PsiMethod> allOverridingMethod = OverridingMethodsSearch.search(psiMethod).findAll();
    for (PsiMethod pm : allOverridingMethod) {
      id[0]++;
      Vertex overridingV = new Vertex(id[0], "METHOD", pm);
      vertices.add(overridingV);
      edges.add(new Edge(overridingV, originVertex, EdgeLabel.IMPLEMENT));
    }
    return new Graph(vertices, edges);
  }

  /**
   * 根据psiClass构建目标图
   *
   * @param psiClass 目标psiClass节点
   * @return 目标图
   */
  public static Graph buildTargetGraph(PsiClass psiClass) {
    if (DataCenter.PROJECT == null || psiClass == null) {
      return null;
    }
    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();
    int id = 1;
    Vertex originVertex = new Vertex(id, "CLASS", psiClass);
    vertices.add(originVertex);
    // 查找这个类的用法
    Query<PsiReference> usagesSearch = ReferencesSearch.search(psiClass, GlobalSearchScope.projectScope(DataCenter.PROJECT));
    Collection<PsiReference> allUsages = usagesSearch.findAll();
    HashSet<PsiMethod> useMethods = new HashSet<>();
    // 去重
    for (PsiReference p : allUsages) {
      PsiMethod parentOfType = PsiTreeUtil.getParentOfType(p.getElement(), PsiMethod.class);
      if (parentOfType != null) {
        useMethods.add(parentOfType);
      }
    }
    for (PsiMethod p : useMethods) {
      id++;
      Vertex vertex = new Vertex(id, "METHOD", p);
      vertices.add(vertex);
      edges.add(new Edge(vertex, originVertex, EdgeLabel.CALL));
      if (DataCenter.PREDICTION_STEP == 2) {
        // 需要进行进一层的搜索
      }
    }
    // 查找这个类有没有继承别的类，也就是有没有super class
    PsiClass superClass = psiClass.getSuperClass();
    if (superClass != null) {
      // 需要判断超类是不是属于这个项目
      if (elementIsInProject(superClass)) {
        id++;
        Vertex superVertex = new Vertex(id, "CLASS", superClass);
        vertices.add(superVertex);
        edges.add(new Edge(originVertex, superVertex, EdgeLabel.INHERIT));
      }
    }
    // 查找这个类有没有实现接口
    PsiReferenceList implementsList = psiClass.getImplementsList();
    if (implementsList != null) {
      PsiJavaCodeReferenceElement[] referenceElements = implementsList.getReferenceElements();
      for (PsiJavaCodeReferenceElement pfe : referenceElements) {
        PsiClass implementInterface = (PsiClass) pfe.resolve();
        if (implementInterface != null && implementInterface.isInterface()) {
          // 需要判断超类是不是属于这个项目
          if (elementIsInProject(implementInterface)) {
            id++;
            Vertex impInterfaceVertex = new Vertex(id, "CLASS", superClass);
            vertices.add(impInterfaceVertex);
            edges.add(new Edge(originVertex, impInterfaceVertex, EdgeLabel.IMPLEMENT));
          }
        }
      }
    }
    // 查找这个类有没有继承者，也就是child class
    // 只查询这个项目类的继承者
    Query<PsiClass> search = ClassInheritorsSearch.search(psiClass, GlobalSearchScope.projectScope(DataCenter.PROJECT), false);
    Collection<PsiClass> allInheritor = search.findAll();
    for (PsiClass inheritor : allInheritor) {
      id++;
      Vertex inheritorVertex = new Vertex(id, "CLASS", inheritor);
      vertices.add(inheritorVertex);
      edges.add(new Edge(inheritorVertex, originVertex, EdgeLabel.INHERIT));
      if (DataCenter.PREDICTION_STEP == 2) {
        // 需要进行进一层的搜索
      }
    }
    // 遍历这个类的所有声明字段
    PsiField[] allFields = psiClass.getAllFields();
    for (PsiField pf : allFields) {
      id++;
      Vertex vertex = new Vertex(id, "FIELD", pf);
      vertices.add(vertex);
      edges.add(new Edge(originVertex, vertex, EdgeLabel.DECLARE));
    }
    // 遍历这个类的所有声明方法
    PsiMethod[] allMethod = psiClass.getMethods();
    for (PsiMethod pm : allMethod) {
      id++;
      Vertex vertex = new Vertex(id, "METHOD", pm);
      vertices.add(vertex);
      edges.add(new Edge(originVertex, vertex, EdgeLabel.DECLARE));
    }
    return new Graph(vertices, edges);
  }

  /**
   * 判断一个PsiClass是不是在该项目中
   * @param psiClass 待检查的PsiClass元素
   * @return 是否在该项目中
   */
  public static boolean elementIsInProject(PsiClass psiClass) {
    if (psiClass == null) {
      return false;
    }
    PsiClass aClass = null;
    if (psiClass.getQualifiedName() != null) {
      aClass = JavaPsiFacade.getInstance(DataCenter.PROJECT).findClass(psiClass.getQualifiedName(), GlobalSearchScope.projectScope(DataCenter.PROJECT));
    } else if (psiClass.getName() != null) {
      PsiClass[] classesByName = PsiShortNamesCache.getInstance(DataCenter.PROJECT).getClassesByName(psiClass.getName(), GlobalSearchScope.projectScope(DataCenter.PROJECT));
      if (classesByName.length > 0) {
        aClass = classesByName[0];
      }
    }
    return aClass != null;
  }

  /**
   * 为目标图的每个节点分配原型
   *
   * @param targetGraph 目标图
   */
  public static void assignStereotypeRole(Graph targetGraph) {
    if (targetGraph == null) {
      return;
    }
    // 遍历所有节点，为节点分配原型
    ArrayList<Vertex> vertices = targetGraph.getVertices();
    for (Vertex v : vertices) {
      // do stereotype assign
      String stereotype = "";
      if (v.getPsiElement() instanceof PsiField) {
        stereotype = "FIELD";
      } else if (v.getPsiElement() instanceof PsiMethod) {
        stereotype = "COMMAND-COLLABORATOR";
      } else if (v.getPsiElement() instanceof PsiClass) {
        stereotype = "ENTITY";
      }
      v.setLabel(stereotype);
    }
  }
}


