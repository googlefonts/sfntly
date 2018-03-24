package com.google.typography.font.tools.fontviewer;

import javax.swing.table.AbstractTableModel;

abstract class ColumnTableModel extends AbstractTableModel {

  private final String[] columnNames;

  ColumnTableModel(String... columnNames) {
    assert columnNames.length > 0;
    this.columnNames = columnNames;
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }
}
