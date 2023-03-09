package com.example.demo1.plugin.window;

import com.example.demo1.data.Constants;
import com.example.demo1.data.ContextTaskData;
import com.example.demo1.data.DataCenter;
import com.example.demo1.data.SuggestionData;
import com.example.demo1.operation.TableDataOperator;
import com.example.demo1.plugin.listener.MarkFlag;
import com.example.demo1.plugin.render.MyTableCellRender;
import com.example.demo1.plugin.render.MyTreeNodeRenderer;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiElement;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.*;

public class CodePredictWindow {
  private JPanel CodePredict;
  private JComboBox TimeInterval;
  private JComboBox PredictionStep;
  private JTree ContextTree;
  private JTable SuggestionTable;

  private void init() {
    SuggestionTable.setModel(DataCenter.TABLE_MODEL);
    SuggestionTable.setRowSelectionAllowed(true);
    SuggestionTable.setFocusable(false);
//    SuggestionTable.setCellSelectionEnabled(false);
//    SuggestionTable.setColumnSelectionAllowed(false);
    SuggestionTable.setDragEnabled(false);
    TableColumn column = null;
    for (int i = 0; i < 4; i++) {
      column = SuggestionTable.getColumnModel().getColumn(i);
      if (i == 0) {
        column.setPreferredWidth(300);
        column.setMinWidth(300);
        column.setCellRenderer(new MyTableCellRender());
      } else if (i == 1) {
        column.setPreferredWidth(200);
        column.setMinWidth(150);
      } else if (i == 2) {
        column.setPreferredWidth(180);
        column.setMinWidth(130);
      } else {
        column.setPreferredWidth(90);
        column.setMinWidth(90);
      }
    }
    // 这样会使table错位，采用自行对Suggestion进行排序
    // 设置根据置信值排序，其余的列不能排序
//    TableRowSorter<TableModel> sorter = new TableRowSorter<>(SuggestionTable.getModel());
//    ArrayList<RowSorter.SortKey> sorterKeys = new ArrayList<>();
//    sorterKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
//    sorter.setSortKeys(sorterKeys);
//    sorter.setSortable(0, false);
//    sorter.setSortable(1, false);
//    sorter.setSortable(2, false);
//    SuggestionTable.setRowSorter(sorter);

    ContextTree.setRootVisible(false);
    ContextTree.setModel(DataCenter.TREE_MODEL);
    ContextTree.setCellRenderer(new MyTreeNodeRenderer());
  }

  public CodePredictWindow(Project project, ToolWindow toolWindow) {
    init();

    ContextTree.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        TreePath selectionPath = ContextTree.getSelectionPath();
        if (selectionPath == null) {
          return;
        }
        DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        ContextTaskData userObject = (ContextTaskData) lastPathComponent.getUserObject();
        // 导航定位元素
        NavigationUtil.activateFileWithPsiElement((PsiElement) userObject.getCaptureElement());
        // 点击树的时候，重新预测一下
        TableDataOperator.executePrediction((PsiElement) userObject.getCaptureElement());
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseReleased(MouseEvent e) {

      }

      @Override
      public void mouseEntered(MouseEvent e) {
        // 防止点击改变caret也进行新的捕捉和预测
        MarkFlag.isMouseInEditor = true;
      }

      @Override
      public void mouseExited(MouseEvent e) {
        MarkFlag.isMouseInEditor = false;
      }
    });

    SuggestionTable.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int i = SuggestionTable.rowAtPoint(e.getPoint());
        SuggestionData suggestionData = DataCenter.SUGGESTION_LIST.get(i);
        PsiElement element = suggestionData.getElement();
        NavigationUtil.activateFileWithPsiElement(element);
      }

      @Override
      public void mousePressed(MouseEvent e) {

      }

      @Override
      public void mouseReleased(MouseEvent e) {

      }

      @Override
      public void mouseEntered(MouseEvent e) {
        MarkFlag.isMouseInEditor = true;
      }

      @Override
      public void mouseExited(MouseEvent e) {
        MarkFlag.isMouseInEditor = false;
      }
    });

    // 事件间隔的选择事件
    TimeInterval.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DataCenter.TIME_INTERVAL = Constants.TIME_INTERVAL_ENUM[TimeInterval.getSelectedIndex()];
      }
    });

    // 预测步长的选择事件
    PredictionStep.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DataCenter.PREDICTION_STEP = PredictionStep.getSelectedIndex() + 1;
      }
    });
  }

  /**
   * 获取代码窗口的主panel
   *
   * @return panel
   */
  public JPanel getCodePredict() {
    return CodePredict;
  }
}
