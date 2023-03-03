package com.example.demo1.data;

import com.example.demo1.plugin.model.MyTableModel;
import com.intellij.openapi.project.Project;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

public class DataCenter {
  /**
   * 当前打开的项目对象
   */
  public static Project PROJECT;

  /**
   * 时间间隔
   */
  public static int TIME_INTERVAL = 5;

  /**
   * 预测步长
   */
  public static int PREDICTION_STEP = 1;

  /**
   * 预测的建议数据对象,中转数据
   */
  public static List<SuggestionData> SUGGESTION_LIST = new ArrayList<>();

  /**
   * 表格模型，用于ToolWindow右侧的列表展示
   */
  public static MyTableModel TABLE_MODEL = new MyTableModel(null, Constants.HEAD);

  /**
   * 树模型，用于ToolWindow的左侧树结构展示
   */
  public static DefaultTreeModel TREE_MODEL = new DefaultTreeModel(new DefaultMutableTreeNode(new ContextTaskData(), true));

  /**
   * 重置插件数据结构
   *
   * @param project 当前项目对象
   */
  public static void reset(Project project) {
    PROJECT = project;
    TIME_INTERVAL = 5;
    PREDICTION_STEP = 1;
    SUGGESTION_LIST.clear();
    TABLE_MODEL.setDataVector(null, Constants.HEAD);
    TREE_MODEL.setRoot(new DefaultMutableTreeNode(new ContextTaskData(), true));
    TREE_MODEL.reload();
  }

}