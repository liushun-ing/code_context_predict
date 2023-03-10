package com.example.contextcodepredict.plugin.window;

import com.example.contextcodepredict.data.Constants;
import com.example.contextcodepredict.data.ContextTaskData;
import com.example.contextcodepredict.data.DataCenter;
import com.example.contextcodepredict.data.SuggestionData;
import com.example.contextcodepredict.operation.TableDataOperator;
import com.example.contextcodepredict.plugin.listener.MarkFlag;
import com.example.contextcodepredict.plugin.render.MyTableCellRender;
import com.example.contextcodepredict.plugin.render.MyTreeNodeRenderer;
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
    // ????????????table????????????????????????Suggestion????????????
    // ??????????????????????????????????????????????????????
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
        // ??????????????????
        NavigationUtil.activateFileWithPsiElement((PsiElement) userObject.getCaptureElement());
        // ???????????????????????????????????????
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
        // ??????????????????caret??????????????????????????????
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

    // ???????????????????????????
    TimeInterval.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DataCenter.TIME_INTERVAL = Constants.TIME_INTERVAL_ENUM[TimeInterval.getSelectedIndex()];
      }
    });

    // ???????????????????????????
    PredictionStep.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        DataCenter.PREDICTION_STEP = PredictionStep.getSelectedIndex() + 1;
      }
    });
  }

  /**
   * ????????????????????????panel
   *
   * @return panel
   */
  public JPanel getCodePredict() {
    return CodePredict;
  }
}
