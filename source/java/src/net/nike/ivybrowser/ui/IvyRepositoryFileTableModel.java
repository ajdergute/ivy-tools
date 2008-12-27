package net.nike.ivybrowser.ui;

import net.nike.ivybrowser.domain.IvyPackage;
import net.nike.ivybrowser.domain.IvyRepositoryItem;

import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;


public class IvyRepositoryFileTableModel implements TableModel
{

    // static final int         JAVADOCS    = 5;
    static final int MODULE   = 1;
    static final int ORG      = 0;
    static final int REVISION = 2;

    // static final int         SOURCE      = 4;
    private List<IvyRepositoryItem> list;
    private String[]                columnNames = { "Org", "Module", "Revision" };

    // --------------------------- CONSTRUCTORS ---------------------------

    public IvyRepositoryFileTableModel(List<IvyRepositoryItem> list)
    {
        this.list = list;
    }

    // ------------------------ INTERFACE METHODS ------------------------


    // --------------------- Interface TableModel ---------------------


    public int getRowCount() { return list.size(); }

    public int getColumnCount() { return columnNames.length; }

    public String getColumnName(int columnIndex) { return columnNames[columnIndex]; }

    public Class getColumnClass(int c) { return getValueAt(0, c).getClass(); }

    public boolean isCellEditable(int rowIndex,
                                  int columnIndex) { return false; }

    public Object getValueAt(int rowIndex,
                             int columnIndex)
    {
        IvyRepositoryItem ivyPackage = list.get(rowIndex);

        switch (columnIndex) {

            case ORG:
                return ivyPackage.getOrg();

            case MODULE:
                return ivyPackage.getModule();

            case REVISION:
            default:
                return ivyPackage.getRev();
        }
    }

    public void setValueAt(Object aValue,
                           int    rowIndex,
                           int    columnIndex) { }

    public void addTableModelListener(TableModelListener l) { }

    public void removeTableModelListener(TableModelListener l) { }

    // -------------------------- OTHER METHODS --------------------------

    public IvyRepositoryItem getItemAt(int row) { return list.get(row); }
}
