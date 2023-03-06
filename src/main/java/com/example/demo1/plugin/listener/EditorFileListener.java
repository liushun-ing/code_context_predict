package com.example.demo1.plugin.listener;

import com.example.demo1.operation.TreeDataOperator;
import com.intellij.openapi.editor.CaretActionListener;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.*;
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


//  @Override
//  public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//    FileEditorManagerListener.super.fileOpened(source, file);
//    addNewTaskContext(file);
//  }
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
//    addNewTaskContext(newFile);
//    selectedTextEditor.getSelectionModel().addSelectionListener(new MySelectionListener());
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
