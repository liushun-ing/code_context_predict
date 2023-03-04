package com.example.demo1.plugin.listener;

import com.example.demo1.operation.TreeDataOperator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 选择事件监听
 */
public class MySelectionListener implements SelectionListener {
  @Override
  public void selectionChanged(@NotNull SelectionEvent e) {
    SelectionListener.super.selectionChanged(e);
    if (e.getNewRange().getStartOffset() == e.getNewRange().getEndOffset()) {
      return;
    }
    Editor editor = e.getEditor();
    if (editor == null || editor.getProject() == null) {
      return;
    }
    PsiFile psiFile = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
    if (psiFile == null) {
      return;
    }
    int offset = editor.getCaretModel().getOffset();

    PsiElement element = psiFile.findElementAt(offset);
    if (element != null) {
      PsiField parentField = PsiTreeUtil.getParentOfType(element, PsiField.class);
      if (parentField != null) {
        TreeDataOperator.addNewData(parentField);
        return;
      }
      PsiMethod parentMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
      if (parentMethod != null) {
        TreeDataOperator.addNewData(parentMethod);
        return;
      }
      PsiClass parentClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
      if (parentClass != null) {
        TreeDataOperator.addNewData(parentClass);
        return;
      }
      // DataCenter.addNewOtherData(element);
    }
  }
}
