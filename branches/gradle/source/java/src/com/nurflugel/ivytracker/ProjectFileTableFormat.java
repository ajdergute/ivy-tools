package com.nurflugel.ivytracker;

import ca.odell.glazedlists.gui.TableFormat;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivytracker.domain.Project;

/** Table format to determine how the Project table gets displayed. */
public class ProjectFileTableFormat implements TableFormat
{
  static final int INCLUDE      = 0;
  static final int PROJECT_NAME = 1;
  private String[] columnNames  = { "Include?", "Project" };

  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface TableFormat ---------------------

  @Override
  public int getColumnCount()
  {
    return 2;
  }

  @Override
  public String getColumnName(int i)
  {
    if ((i >= 0) && (i < columnNames.length))
    {
      return columnNames[i];
    }

    throw new IllegalStateException();
  }

  @Override
  public Object getColumnValue(Object o, int i)
  {
    Project project = (Project) o;

    switch (i)
    {
      case INCLUDE:
        return project.isIncluded();

      case PROJECT_NAME:
        return project.getProjectUrl();

      default:
        throw new IllegalStateException();
    }
  }
}
