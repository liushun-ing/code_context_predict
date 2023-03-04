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

  public static int ID = 0;

  /**
   * 判断一个元素是不是已经添加到节点集合中过
   *
   * @param vertices 已经创建的节点集合
   * @param psiElement 目标元素
   * @return 是否存在
   */
  public static boolean vertexNotExist(ArrayList<Vertex> vertices, PsiElement psiElement) {
    if (psiElement == null) {
      return false;
    }
    for (Vertex v : vertices) {
      if (v.getPsiElement().equals(psiElement)) {
        return false;
      }
    }
    return true;
  }

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
    // 构建图
    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();
    ID = 1;
    Vertex fieldVertex = new Vertex(ID, "FIELD", psiField);
    vertices.add(fieldVertex);
    extendFieldGraph(fieldVertex, vertices, edges);
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
    ID = 1;
    Vertex originVertex = new Vertex(ID, "METHOD", psiMethod);
    vertices.add(originVertex);
    extendMethodGraph(originVertex, vertices, edges);
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
    Vertex originVertex = new Vertex(ID, "CLASS", psiClass);
    vertices.add(originVertex);
    extendClassGraph(originVertex, vertices, edges);
    return new Graph(vertices, edges);
  }

  /**
   * 扩展一个FIELD节点子图
   *
   * @param originVertex 目标FIELD节点
   * @param vertices 节点集合
   * @param edges 边集合
   */
  public static void extendFieldGraph(Vertex originVertex, ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
    PsiField psiField = (PsiField) originVertex.getPsiElement();
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
    for (PsiMethod psiMethod : psiMethods) {
      // 直接简单处理，不存在才把他加进去
      if (vertexNotExist(vertices, psiMethod)) {
        ID++;
        Vertex methodVertex = new Vertex(ID, "METHOD", psiMethod);
        vertices.add(methodVertex);
        edges.add(new Edge(methodVertex, originVertex, EdgeLabel.CALL));
      }
    }
    // 查找谁申明了这个字段
    PsiClass containingClass = psiField.getContainingClass();
    if (elementIsInProject(containingClass)) {
      if (vertexNotExist(vertices, containingClass)) {
        ID++;
        Vertex vertex = new Vertex(ID, "CLASS", containingClass);
        vertices.add(vertex);
        edges.add(new Edge(vertex, originVertex, EdgeLabel.DECLARE));
      }
    }
  }

  /**
   * 扩展一个METHOD节点子图
   * @param originVertex 目标METHOD节点
   * @param vertices 节点集合
   * @param edges 边集合
   */
  public static void extendMethodGraph(Vertex originVertex, ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
    PsiMethod psiMethod = (PsiMethod) originVertex.getPsiElement();
    // 查找谁声明了这个方法
    PsiClass containingClass = psiMethod.getContainingClass();
    ID++;
    Vertex containClassVertex = new Vertex(ID, "CLASS", containingClass);
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
      if (vertexNotExist(vertices, p)) {
        ID++;
        Vertex vertex = new Vertex(ID, "METHOD", p);
        vertices.add(vertex);
        edges.add(new Edge(vertex, originVertex, EdgeLabel.CALL));
      }
      if (DataCenter.PREDICTION_STEP == 2) {
        // 需要进行进一层的搜索
      }
    }
    // 查找这个方法实现了谁
    PsiMethod[] superMethods = psiMethod.findSuperMethods();
    for (PsiMethod pm : superMethods) {
      if (vertexNotExist(vertices, pm)) {
        ID++;
        Vertex vertex = new Vertex(ID, "METHOD", pm);
        vertices.add(vertex);
        edges.add(new Edge(originVertex, vertex, EdgeLabel.IMPLEMENT));
      }

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
              if (vertexNotExist(vertices, callClass)) {
                ID++;
                // 直接与这个类关联
                Vertex callVertex = new Vertex(ID, "CLASS", callClass);
                vertices.add(callVertex);
                edges.add(new Edge(originVertex, callVertex, EdgeLabel.CALL));
              }
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
            if (vertexNotExist(vertices, callField)) {
              ID++;
              Vertex callVertex = new Vertex(ID, "FIELD", callField);
              vertices.add(callVertex);
              edges.add(new Edge(originVertex, callVertex, EdgeLabel.CALL));
            }
          } else if (referenceElement instanceof PsiMethod) {
            // 调用了某个方法
            PsiMethod callMethod = (PsiMethod) referenceElement;
            // 只有当这个方法是项目中的某个类声明的才能用作扩展
            if (elementIsInProject(callMethod.getContainingClass())) {
              if (vertexNotExist(vertices, callMethod)) {
                ID++;
                Vertex callVertex = new Vertex(ID, "METHOD", callMethod);
                vertices.add(callVertex);
                edges.add(new Edge(originVertex, callVertex, EdgeLabel.CALL));
              }
            }
          }
        }
      }
    });

    // 查找谁实现了这个方法
    Collection<PsiMethod> allOverridingMethod = OverridingMethodsSearch.search(psiMethod).findAll();
    for (PsiMethod pm : allOverridingMethod) {
      if (vertexNotExist(vertices, pm)) {
        ID++;
        Vertex overridingV = new Vertex(ID, "METHOD", pm);
        vertices.add(overridingV);
        edges.add(new Edge(overridingV, originVertex, EdgeLabel.IMPLEMENT));
      }
    }
  }

  /**
   * 扩展一个CLASS节点子图
   * @param originVertex 目标CLASS节点
   * @param vertices 节点集合
   * @param edges 边集合
   */
  public static void extendClassGraph(Vertex originVertex, ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
    PsiClass psiClass = (PsiClass) originVertex.getPsiElement();
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
      if (vertexNotExist(vertices, p)) {
        ID++;
        Vertex vertex = new Vertex(ID, "METHOD", p);
        vertices.add(vertex);
        edges.add(new Edge(vertex, originVertex, EdgeLabel.CALL));
      }
      if (DataCenter.PREDICTION_STEP == 2) {
        // 需要进行进一层的搜索
      }
    }
    // 查找这个类有没有继承别的类，也就是有没有super class
    PsiClass superClass = psiClass.getSuperClass();
    if (superClass != null) {
      // 需要判断超类是不是属于这个项目
      if (elementIsInProject(superClass)) {
        if (vertexNotExist(vertices, superClass)) {
          ID++;
          Vertex superVertex = new Vertex(ID, "CLASS", superClass);
          vertices.add(superVertex);
          edges.add(new Edge(originVertex, superVertex, EdgeLabel.INHERIT));
        }
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
            if (vertexNotExist(vertices, implementInterface)) {
              ID++;
              Vertex impInterfaceVertex = new Vertex(ID, "CLASS", superClass);
              vertices.add(impInterfaceVertex);
              edges.add(new Edge(originVertex, impInterfaceVertex, EdgeLabel.IMPLEMENT));
            }
          }
        }
      }
    }
    // 查找这个类有没有继承者，也就是child class
    // 只查询这个项目类的继承者
    Query<PsiClass> search = ClassInheritorsSearch.search(psiClass, GlobalSearchScope.projectScope(DataCenter.PROJECT), false);
    Collection<PsiClass> allInheritor = search.findAll();
    for (PsiClass inheritor : allInheritor) {
      if (vertexNotExist(vertices, inheritor)) {
        ID++;
        Vertex inheritorVertex = new Vertex(ID, "CLASS", inheritor);
        vertices.add(inheritorVertex);
        edges.add(new Edge(inheritorVertex, originVertex, EdgeLabel.INHERIT));
      }
      if (DataCenter.PREDICTION_STEP == 2) {
        // 需要进行进一层的搜索
      }
    }
    // 遍历这个类的所有声明字段
    PsiField[] allFields = psiClass.getAllFields();
    for (PsiField pf : allFields) {
      if (vertexNotExist(vertices, pf)) {
        ID++;
        Vertex vertex = new Vertex(ID, "FIELD", pf);
        vertices.add(vertex);
        edges.add(new Edge(originVertex, vertex, EdgeLabel.DECLARE));
      }
    }
    // 遍历这个类的所有声明方法
    PsiMethod[] allMethod = psiClass.getMethods();
    for (PsiMethod pm : allMethod) {
      if (vertexNotExist(vertices, pm)) {
        ID++;
        Vertex vertex = new Vertex(ID, "METHOD", pm);
        vertices.add(vertex);
        edges.add(new Edge(originVertex, vertex, EdgeLabel.DECLARE));
      }
    }
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


