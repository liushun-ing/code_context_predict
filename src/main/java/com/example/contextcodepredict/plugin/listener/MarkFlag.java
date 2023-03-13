package com.example.contextcodepredict.plugin.listener;

/**
 * 捕获元素所用到的一些标志
 */
public class MarkFlag {
  /**
   * 鼠标是否在编辑器中
   */
  public static boolean isMouseInEditor = false;

  /**
   * toolWindow是否打开了
   */
  public static boolean isToolWindowActive = false;

  /**
   * 插件是否活跃，活跃状态才可捕捉元素
   *
   * @return 是否活跃
   */
  public static boolean isPluginActive() {
    return isToolWindowActive && isMouseInEditor;
  }
}
