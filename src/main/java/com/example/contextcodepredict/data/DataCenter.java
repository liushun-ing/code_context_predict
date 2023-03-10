package com.example.contextcodepredict.data;

import com.example.contextcodepredict.myjstereocode.entry.StereotypeAssigner;
import com.example.contextcodepredict.myjstereocode.info.ProjectInformation;
import com.example.contextcodepredict.plugin.model.MyTableModel;
import com.intellij.openapi.project.Project;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据中心
 */
public class DataCenter {
  /**
   * 当前打开的项目对象
   */
  public static Project PROJECT;

  /**
   * 项目信息
   */
  public static ProjectInformation PROJECT_INFORMATION;

  /**
   * 原型分配器
   */
  public static StereotypeAssigner STEREOTYPE_ASSIGNER;

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
    PROJECT_INFORMATION = new ProjectInformation(project);
    STEREOTYPE_ASSIGNER = new StereotypeAssigner();
    STEREOTYPE_ASSIGNER.setParameters(PROJECT_INFORMATION.getMethodsMean(), PROJECT_INFORMATION.getMethodsStdDev());
    TIME_INTERVAL = 5;
    PREDICTION_STEP = 1;
    SUGGESTION_LIST.clear();
    TABLE_MODEL.setDataVector(null, Constants.HEAD);
    TREE_MODEL.setRoot(new DefaultMutableTreeNode(new ContextTaskData(), true));
    TREE_MODEL.reload();
  }

}