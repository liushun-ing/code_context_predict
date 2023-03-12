package com.example.contextcodepredict.plugin.listener;

import com.example.contextcodepredict.data.DataCenter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

/**
 * 项目监听事件
 */
public class MyProjectListener implements ProjectManagerListener {
  @Override
  public void projectOpened(@NotNull Project project) {
    // 初始化操作
    DataCenter.reset(project);
  }
}