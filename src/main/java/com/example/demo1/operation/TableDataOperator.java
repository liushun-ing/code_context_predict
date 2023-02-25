package com.example.demo1.operation;

import com.example.demo1.data.SuggestionData;

public class TableDataOperator {
  /**
   * 根据suggestionData转换为table展示的数据类型
   * @param suggestionData 需要转换的对象
   * @return 转换后的table行对象
   */
  public static Object[] convertSuggestionData(SuggestionData suggestionData) {
    Object[] raw = new Object[4];
    raw[0] = suggestionData.getElement();
    raw[1] = suggestionData.getPackagePath();
    raw[2] = suggestionData.getStereotype();
    raw[3] = suggestionData.getConfidence();
    return raw;
  }
}