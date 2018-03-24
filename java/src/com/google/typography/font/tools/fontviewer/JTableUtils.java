package com.google.typography.font.tools.fontviewer;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public enum JTableUtils {
  ;

  static void setNumberColumn(JTable table, int column) {
    DefaultTableCellRenderer rightAligned = new DefaultTableCellRenderer();
    rightAligned.setHorizontalAlignment(SwingConstants.RIGHT);

    table.getColumnModel().getColumn(column).setMinWidth(70);
    table.getColumnModel().getColumn(column).setMaxWidth(70);
    table.getColumnModel().getColumn(column).setResizable(false);
    table.getColumnModel().getColumn(column).setCellRenderer(rightAligned);
  }
}
