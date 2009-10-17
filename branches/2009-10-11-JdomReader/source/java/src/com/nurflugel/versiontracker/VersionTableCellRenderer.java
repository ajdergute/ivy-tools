package com.nurflugel.versiontracker;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import static java.awt.Color.*;

/** Renderer to highlight rows which exceed the threshold. */
public class VersionTableCellRenderer extends JLabel implements TableCellRenderer
{
  private VersionTrackerUi ui;

  public VersionTableCellRenderer(VersionTrackerUi ui)
  {
    this.ui = ui;
  }

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TableCellRenderer ---------------------

  public Component getTableCellRendererComponent(JTable jTable, Object whatToRender, boolean isSelected, boolean b1, int row, int col)
  {
    setText(whatToRender.toString());

    if (col == 0)
    {
      setForeground(BLACK);

      return this;
    }

    String text      = (String) whatToRender;
    int    level     = Integer.parseInt(text.trim());
    Jdk    threshold = ui.getJdkThreshold();

    if (level >= threshold.getVersion())
    {
      setForeground(RED);
    }
    else
    {
      setForeground(BLACK);
    }

    return this;
  }
}
