package com.example.contextcodepredict.plugin.listener;

import com.example.contextcodepredict.operation.TreeDataOperator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 编辑器事件监听
 */
public class EditorFileListener implements FileEditorManagerListener {

  private final Project project;

  public EditorFileListener(Project project) {
    this.project = project;
  }

  @Override
  public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    FileEditorManagerListener.super.selectionChanged(event);
    VirtualFile newFile = event.getNewFile();

    PsiFile psiFile = PsiManager.getInstance(project).findFile(newFile);
    if (psiFile == null) {
      return;
    }
    Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    if (selectedTextEditor == null) {
      return;
    }
    selectedTextEditor.addEditorMouseListener(new MyEditorMouseListener());
    selectedTextEditor.getCaretModel().addCaretListener(new MyCaretListener());
    // 只有当项目的索引已经构建好之后，才添加元素，不然分析psi树会报错
    if (!MarkFlag.isMouseInEditor && !DumbService.getInstance(project).isDumb()) {
      addNewTaskContext(newFile);
    }
//    DumbService.isDumb(project);
    // 等待项目处于 smart模式 时，再执行业务操作
//    DumbService.getInstance(project).runWhenSmart(()->{
//      // 业务逻辑
//    });
  }

  public void addNewTaskContext(@NotNull VirtualFile file) {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
    if (psiFile == null) {
      return;
    }
    PsiClass[] psiClasses = new PsiClass[1];
    PsiTreeUtil.findChildrenOfType(psiFile.getNavigationElement(), PsiClass.class).toArray(psiClasses);
    if (psiClasses[0] != null) {
      TreeDataOperator.addNewData(psiClasses[0]);
    }
  }

}
