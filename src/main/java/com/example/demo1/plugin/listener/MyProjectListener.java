package com.example.demo1.plugin.listener;

import com.example.demo1.data.DataCenter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public class MyProjectListener implements ProjectManagerListener {
  @Override
  public void projectOpened(@NotNull Project project) {
    // 初始化操作
    DataCenter.reset(project);
  }
}