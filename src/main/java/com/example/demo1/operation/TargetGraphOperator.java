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
   * 判断一个元素是不是已经与某个节点建立关系了,先判断节点是否存在，再判断是否有边
   *
   * @param vertices 已经创建的节点集合
   * @param psiElement 目标元素
   * @param edges 边集合
   * @param originVertex 源节点
   * @param type 类型，关系的指向性，0=originVertex->psiElement 1=psiElement->originVertex
   * @return 是否存在
   */
  public static boolean relationNotExist(ArrayList<Vertex> vertices, PsiElement psiElement, ArrayList<Edge> edges, Vertex originVertex, int type) {
    if (psiElement == null) {
      return false;
    }
    for (Vertex v : vertices) {
      // 先判断是不是已经有该元素创建的节点
      if (v.getPsiElement().equals(psiElement)) {
        for (Edge e : edges) {
          if (type == 0) {
            if (e.getStartV().equals(originVertex) && e.getEndV().equals(v)) {
              return false;
            }
          } else {
            if (e.getStartV().equals(v) && e.getEndV().equals(originVertex)) {
              return false;
            }
          }
        }
        // 如果存在节点了，但是不存在边，直接返回false
        return true;
      }
    }
    return true;
  }

  /**
   * 根据psiElement(PsiField,PsiMethod,PsiClass)构建目标图
   *
   * @param psiElement 目标psiElement节点
   * @return 目标图
   */
  public static Graph buildTargetGraph(PsiElement psiElement) {
    if (DataCenter.PROJECT == null || psiElement == null) {
      return null;
    }
    // 构建图
    ArrayList<Vertex> vertices = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();
    ID = 1;
    if (psiElement instanceof PsiField) {
      Vertex fieldVertex = new Vertex(ID, "FIELD", psiElement);
      vertices.add(fieldVertex);
      extendFieldGraph(fieldVertex, vertices, edges);
    } else if (psiElement instanceof PsiMethod) {
      Vertex originVertex = new Vertex(ID, "METHOD", psiElement);
      vertices.add(originVertex);
      extendMethodGraph(originVertex, vertices, edges);
    } else if (psiElement instanceof PsiClass) {
      Vertex originVertex = new Vertex(ID, "CLASS", psiElement);
      vertices.add(originVertex);
      extendClassGraph(originVertex, vertices, edges);
    } else {
      return null;
    }
    // 如果预测步长为2，则进行二次扩展
    if (DataCenter.PREDICTION_STEP == 2) {
      extendTwoStepGraph(vertices, edges);
    }
    return new Graph(vertices, edges);
  }

  /**
   * 一步扩展一个FIELD节点子图
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
      if (relationNotExist(vertices, psiMethod, edges, originVertex, 1)) {
        ID++;
        Vertex methodVertex = new Vertex(ID, "METHOD", psiMethod);
        vertices.add(methodVertex);
        edges.add(new Edge(methodVertex, originVertex, EdgeLabel.CALL));
      }
    }
    // 查找谁申明了这个字段
    PsiClass containingClass = psiField.getContainingClass();
    if (elementIsInProject(containingClass)) {
      if (relationNotExist(vertices, containingClass, edges, originVertex, 1)) {
        ID++;
        Vertex vertex = new Vertex(ID, "CLASS", containingClass);
        vertices.add(vertex);
        edges.add(new Edge(vertex, originVertex, EdgeLabel.DECLARE));
      }
    }
  }

  /**
   * 一步扩展一个METHOD节点子图
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
      if (relationNotExist(vertices, p, edges, originVertex, 1)) {
        ID++;
        Vertex vertex = new Vertex(ID, "METHOD", p);
        vertices.add(vertex);
        edges.add(new Edge(vertex, originVertex, EdgeLabel.CALL));
      }
    }
    // 查找这个方法实现了谁
    PsiMethod[] superMethods = psiMethod.findSuperMethods();
    for (PsiMethod pm : superMethods) {
      if (relationNotExist(vertices, pm, edges, originVertex, 0)) {
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
              if (relationNotExist(vertices, callClass, edges, originVertex, 0)) {
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
            if (relationNotExist(vertices, callField, edges, originVertex, 0)) {
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
              if (relationNotExist(vertices, callMethod, edges, originVertex, 0)) {
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
      if (relationNotExist(vertices, pm, edges, originVertex, 1)) {
        ID++;
        Vertex overridingV = new Vertex(ID, "METHOD", pm);
        vertices.add(overridingV);
        edges.add(new Edge(overridingV, originVertex, EdgeLabel.IMPLEMENT));
      }
    }
  }

  /**
   * 一步扩展一个CLASS节点子图
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
      if (relationNotExist(vertices, p, edges, originVertex, 1)) {
        ID++;
        Vertex vertex = new Vertex(ID, "METHOD", p);
        vertices.add(vertex);
        edges.add(new Edge(vertex, originVertex, EdgeLabel.CALL));
      }
    }
    // 查找这个类有没有继承别的类，也就是有没有super class
    PsiClass superClass = psiClass.getSuperClass();
    if (superClass != null) {
      // 需要判断超类是不是属于这个项目
      if (elementIsInProject(superClass)) {
        if (relationNotExist(vertices, superClass, edges, originVertex, 0)) {
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
            if (relationNotExist(vertices, implementInterface, edges, originVertex, 0)) {
              ID++;
              Vertex impInterfaceVertex = new Vertex(ID, "CLASS", implementInterface);
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
      if (relationNotExist(vertices, inheritor, edges, originVertex, 1)) {
        ID++;
        Vertex inheritorVertex = new Vertex(ID, "CLASS", inheritor);
        vertices.add(inheritorVertex);
        edges.add(new Edge(inheritorVertex, originVertex, EdgeLabel.INHERIT));
      }
    }
    // 遍历这个类的所有声明字段
    PsiField[] allFields = psiClass.getFields();
    for (PsiField pf : allFields) {
      if (relationNotExist(vertices, pf, edges, originVertex, 0)) {
        ID++;
        Vertex vertex = new Vertex(ID, "FIELD", pf);
        vertices.add(vertex);
        edges.add(new Edge(originVertex, vertex, EdgeLabel.DECLARE));
      }
    }
    // 遍历这个类的所有声明方法
    PsiMethod[] allMethod = psiClass.getMethods();
    for (PsiMethod pm : allMethod) {
      if (relationNotExist(vertices, pm, edges, originVertex, 0)) {
        ID++;
        Vertex vertex = new Vertex(ID, "METHOD", pm);
        vertices.add(vertex);
        edges.add(new Edge(originVertex, vertex, EdgeLabel.DECLARE));
      }
    }
  }

  /**
   * 2步扩展目标图
   *
   * @param vertices 节点集合
   * @param edges 边集合
   */
  public static void extendTwoStepGraph(ArrayList<Vertex> vertices, ArrayList<Edge> edges) {
    // 从第二个节点开始遍历扩展就好了，注意扩展的截至点
    int length = vertices.size();
    for (int i = 1; i < length; i++) {
      Vertex currVertex = vertices.get(i);
      if (currVertex.getPsiElement() instanceof PsiField) {
        extendFieldGraph(currVertex, vertices, edges);
      } else if (currVertex.getPsiElement() instanceof PsiMethod) {
        extendMethodGraph(currVertex, vertices, edges);
      } else if (currVertex.getPsiElement() instanceof PsiClass) {
        extendClassGraph(currVertex, vertices, edges);
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


