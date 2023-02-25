package com.example.demo1.operation;

import com.example.demo1.data.DataCenter;
import com.example.demo1.vf3.graph.Edge;
import com.example.demo1.vf3.graph.Graph;
import com.example.demo1.vf3.graph.Vertex;
import com.example.demo1.vf3.utils.EdgeLabel;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class TargetGraphOperator {

  /**
   * 根据psiField构建目标图
   *
   * @param psiField 目标psiField节点
   * @return 目标图
   */
  public static Graph buildTargetGraph(PsiField psiField) {
    if (DataCenter.PROJECT == null) {
      return null;
    }
    // 查找用法，无论是一步还是两步，都只需要查找调用
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
    // 构件图
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
    return new Graph(vertices, edges);
  }

  /**
   * 根据psiMethod构建目标图
   *
   * @param psiMethod 目标psiMethod节点
   * @return 目标图
   */
  public static Graph buildTargetGraph(PsiMethod psiMethod) {
    if (DataCenter.PROJECT == null) {
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
    System.out.println("method: start");
    HashSet<PsiMethod> useMethods = new HashSet<>();
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
            id[0]++;
            Vertex callVertex = new Vertex(id[0], "CLASS", callClass);
            vertices.add(callVertex);
            edges.add(new Edge(originVertex, callVertex, EdgeLabel.CALL));
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
            id[0]++;
            Vertex callVertex = new Vertex(id[0], "METHOD", callMethod);
            vertices.add(callVertex);
            edges.add(new Edge(originVertex, callVertex, EdgeLabel.CALL));
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

    System.out.println("method: end");
    return new Graph(vertices, edges);
  }

  /**
   * 根据psiClass构建目标图
   *
   * @param psiClass 目标psiClass节点
   * @return 目标图
   */
  public static Graph buildTargetGraph(PsiClass psiClass) {
    return new Graph();
  }
}
