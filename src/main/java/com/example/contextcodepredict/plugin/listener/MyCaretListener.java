package com.example.contextcodepredict.plugin.listener;

import com.example.contextcodepredict.operation.TreeDataOperator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 编辑器光标事件
 */
public class MyCaretListener implements CaretListener {
  @Override
  public void caretPositionChanged(@NotNull CaretEvent e) {
    CaretListener.super.caretPositionChanged(e);
    if (e.getCaret() == null) {
      return;
    }
    // 只有当鼠标在编辑器类并没有选中文本的时候才不捕获（说明是单机事件）
    if (MarkFlag.isPluginCaretActive() && e.getCaret().getSelectionStart() == e.getCaret().getSelectionEnd()) {
      return;
    }
    Editor editor = e.getEditor();
    if (editor.getProject() == null) {
      return;
    }
    PsiFile psiFile = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
    if (psiFile == null) {
      return;
    }
    int offset = editor.getCaretModel().getOffset();

    PsiElement element = psiFile.findElementAt(offset);
    // 确保索引已经构建
    if (element != null && !DumbService.getInstance(editor.getProject()).isDumb()) {
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
