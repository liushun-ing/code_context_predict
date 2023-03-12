package com.example.contextcodepredict.plugin.render;

import com.example.contextcodepredict.data.ContextTaskData;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * 树节点渲染规则
 */
public class MyTreeNodeRenderer extends DefaultTreeCellRenderer {

  private static final Icon otherIcon = IconLoader.getIcon("/img/other.svg", MyTreeNodeRenderer.class);
  private static final Icon classIcon = IconLoader.getIcon("/img/class.svg", MyTreeNodeRenderer.class);
  private static final Icon fieldIcon = IconLoader.getIcon("/img/field.svg", MyTreeNodeRenderer.class);
  private static final Icon methodIcon = IconLoader.getIcon("/img/method.svg", MyTreeNodeRenderer.class);
  private static final Icon interfaceIcon = IconLoader.getIcon("/img/interface.svg", MyTreeNodeRenderer.class);
  private static final Icon enumIcon = IconLoader.getIcon("/img/enum.svg", MyTreeNodeRenderer.class);
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    // 取得节点
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    ContextTaskData contextTaskData = (ContextTaskData) node.getUserObject();
    Object captureElement = contextTaskData.getCaptureElement();
    if (captureElement instanceof PsiClass) {
      PsiClass psiObject = (PsiClass) captureElement;
      if (psiObject.isInterface()) {
        setIcon(interfaceIcon);
      } else if (psiObject.isEnum()) {
        setIcon(enumIcon);
      } else {
        setIcon(classIcon);
      }
      setText(psiObject.getQualifiedName());
    } else if (captureElement instanceof PsiMethod) {
      PsiMethod psiObject = (PsiMethod) captureElement;
      setIcon(methodIcon);
      if (psiObject.getReturnType() == null) {
        setText(psiObject.getName() + psiObject.getParameterList().getText());
      } else {
        setText(psiObject.getName() + psiObject.getParameterList().getText() + ": " + psiObject.getReturnType().getCanonicalText());
      }
    } else if (captureElement instanceof PsiField) {
      PsiField psiObject = (PsiField) captureElement;
      setText(psiObject.getName() + ": " + psiObject.getType().getPresentableText());
      setIcon(fieldIcon);
    } else {
      setIcon(otherIcon);
      setText(captureElement.toString());
    }
    return this;
  }
}
