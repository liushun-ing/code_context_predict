package com.example.contextcodepredict.plugin.listener;

import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import org.jetbrains.annotations.NotNull;

/**
 * toolWindow事件监听器
 */
public class MyToolWindowListener implements ToolWindowManagerListener {

  @Override
  public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
    ToolWindowManagerListener.super.stateChanged(toolWindowManager);
    ToolWindow codePredictWindow = toolWindowManager.getToolWindow("CodePredictWindow");
    MarkFlag.isToolWindowActive = codePredictWindow != null && codePredictWindow.isVisible();
  }
}
