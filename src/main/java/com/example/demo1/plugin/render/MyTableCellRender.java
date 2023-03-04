package com.example.demo1.plugin.render;

import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * 表格单元格渲染规则
 */
public class MyTableCellRender extends DefaultTableCellRenderer {
  private static final Icon otherIcon = IconLoader.getIcon("/img/other.svg", MyTableCellRender.class);
  private static final Icon classIcon = IconLoader.getIcon("/img/class.svg", MyTableCellRender.class);
  private static final Icon fieldIcon = IconLoader.getIcon("/img/field.svg", MyTableCellRender.class);
  private static final Icon methodIcon = IconLoader.getIcon("/img/method.svg", MyTableCellRender.class);
  private static final Icon interfaceIcon = IconLoader.getIcon("/img/interface.svg", MyTableCellRender.class);
  private static final Icon enumIcon = IconLoader.getIcon("/img/enum.svg", MyTableCellRender.class);


  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    if (value instanceof PsiClass) {
      PsiClass psiObject = (PsiClass) value;
      if (psiObject.isInterface()) {
        setIcon(interfaceIcon);
      } else if (psiObject.isEnum()) {
        setIcon(enumIcon);
      } else {
        setIcon(classIcon);
      }
      setText(psiObject.getQualifiedName());
    } else if (value instanceof PsiMethod) {
      PsiMethod psiObject = (PsiMethod) value;
      setIcon(methodIcon);
      if (psiObject.getReturnType() == null) {
        setText(psiObject.getName() + psiObject.getParameterList().getText());
      } else {
        setText(psiObject.getName() + psiObject.getParameterList().getText() + ": " + psiObject.getReturnType().getCanonicalText());
      }
    } else if (value instanceof PsiField) {
      PsiField psiObject = (PsiField) value;
      setText(psiObject.getName() + ": " + psiObject.getType().getPresentableText());
      setIcon(fieldIcon);
    } else {
      setIcon(otherIcon);
      setText(value.toString());
    }
    return this;
  }
}
