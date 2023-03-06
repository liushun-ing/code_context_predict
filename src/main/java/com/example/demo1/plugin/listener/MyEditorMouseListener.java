package com.example.demo1.plugin.listener;

import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import org.jetbrains.annotations.NotNull;

public class MyEditorMouseListener implements EditorMouseListener {
  @Override
  public void mouseEntered(@NotNull EditorMouseEvent event) {
    EditorMouseListener.super.mouseEntered(event);
    MarkFlag.isMouseInEditor = true;
  }

  @Override
  public void mouseExited(@NotNull EditorMouseEvent event) {
    EditorMouseListener.super.mouseExited(event);
    MarkFlag.isMouseInEditor = false;
  }
}
