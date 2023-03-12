package com.example.contextcodepredict.data;

/**
 * 数据常量
 */
public class Constants {
  /**
   * 表格数据的head
   */
  public static String[] HEAD = {"Element", "Package", "Stereotype", "Confidence"};
  /**
   * 事件间隔数组
   */
  public static int[] TIME_INTERVAL_ENUM = {5, 10, 20, 30, 60};
  /**
   * 处理超时时间
   */
  public static long TIME_OUT = 5000L;
  /**
   * 超时提示的文案
   */
  public static String TIME_OUT_TIP = "Time Out! Please adjust the Prediction Step.";
  /**
   * 超时提示的标题
   */
  public static String TIME_OUT_TITLE = "Warning";
}
