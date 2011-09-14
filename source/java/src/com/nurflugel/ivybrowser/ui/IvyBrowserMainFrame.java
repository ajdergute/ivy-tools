package com.nurflugel.ivybrowser.ui;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.nurflugel.Os;
import com.nurflugel.WebAuthenticator;

import com.nurflugel.common.ui.FindMultiplePreferencesItemsDialog;
import com.nurflugel.common.ui.UiMainFrame;
import static com.nurflugel.common.ui.Util.addHelpListener;
import static com.nurflugel.common.ui.Util.centerApp;
import static com.nurflugel.common.ui.Version.VERSION;

import static com.nurflugel.externalsreporter.ui.ExternalsFinderMainFrame.sizeTableColumns;

import com.nurflugel.ivybrowser.InfiniteProgressPanel;
import com.nurflugel.ivybrowser.Preferences;
import com.nurflugel.ivybrowser.domain.DataSerializer;
import com.nurflugel.ivybrowser.domain.IvyPackage;
import com.nurflugel.ivybrowser.handlers.BaseWebIvyRepositoryBrowserHandler;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import java.awt.Component;
import java.awt.Cursor;
import static java.awt.Cursor.getPredefinedCursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;

import java.net.Authenticator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import static javax.swing.BoxLayout.Y_AXIS;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/** Main UI frame for the Ivy Browser. */
@SuppressWarnings({ "MethodParameterNamingConvention", "CallToPrintStackTrace", "MethodOnlyUsedFromInnerClass", "unchecked" })
public class IvyBrowserMainFrame extends JFrame implements UiMainFrame
{
  private static final long                                 serialVersionUID    = 8982188831570838035L;
  public static final String                                IVYBROWSER_DATA_XML = "ivybrowser_data.xml";
  private Cursor                                            busyCursor          = getPredefinedCursor(WAIT_CURSOR);
  private Cursor                                            normalCursor        = getPredefinedCursor(DEFAULT_CURSOR);
  private JButton                                           specifyButton       = new JButton("Specify Repository");
  private JButton                                           reparseButton       = new JButton("Re-parse Repository");
  private JButton                                           quitButton          = new JButton("Quit");
  private JButton                                           helpButton          = new JButton("Help");
  private JLabel                                            findLabel           = new JLabel("Find library:");
  private JLabel                                            statusLabel         = new JLabel();
  private JCheckBox                                         parseOnOpenCheckbox = new JCheckBox("Parse Repository on Open", false);
  private JTable                                            resultsTable        = new JTable();
  private JTextField                                        libraryField        = new JTextField();
  private Preferences                                       preferences         = new Preferences(this);
  private EventList<IvyPackage>                             repositoryList      = new BasicEventList<IvyPackage>();
  private JScrollPane                                       scrollPane;
  private JPanel                                            holdingPanel;
  private String                                            ivyRepositoryPath;
  private BaseWebIvyRepositoryBrowserHandler                parsingHandler;
  private Map<String, Map<String, Map<String, IvyPackage>>> packageMap          = Collections.synchronizedMap(new HashMap<String, Map<String, Map<String, IvyPackage>>>());
  private InfiniteProgressPanel                             progressPanel       = new InfiniteProgressPanel("Accessing the Ivy repository, please be patient - click to cancel",
                                                                                                            this);

  // --------------------------- CONSTRUCTORS ---------------------------
  public IvyBrowserMainFrame()
  {
    initializeComponents();
    pack();
    setSize(800, 600);
    centerApp(this);
    Authenticator.setDefault(new WebAuthenticator());
    libraryField.setEnabled(false);
    setVisible(true);

    boolean parseOnOpen = preferences.getParseOnOpen();

    // parseOnOpen = false;
    parseOnOpenCheckbox.setSelected(parseOnOpen);

    if (doSavedResultsExistForRepository())
    {
      getSavedResults();
    }

    if (parseOnOpen)
    {
      // reparse();
    }
  }

  private void getSavedResults()
  {
    EventList<IvyPackage> dibble         = new BasicEventList<IvyPackage>();
    DataSerializer        dataSerializer = new DataSerializer(dibble);

    dataSerializer.retrieveFromXml();

    List<IvyPackage> ivyPackages = dataSerializer.getIvyPackages();

    repositoryList.addAll(ivyPackages);
    stopProgressPanel();
  }

  private boolean doSavedResultsExistForRepository()
  {
    return (DataSerializer.getDataFile()).exists();
  }

  private void initializeComponents()
  {
    JPanel mainPanel = new JPanel(new BorderLayout(0, 0));

    setGlassPane(progressPanel);
    setContentPane(mainPanel);
    libraryField.setPreferredSize(new Dimension(200, 25));
    setTitle("Ivy Repository Browser v. " + VERSION);

    JPanel textPanel   = new JPanel();
    JPanel buttonPanel = new JPanel();

    holdingPanel = new JPanel();

    BoxLayout layout = new BoxLayout(holdingPanel, Y_AXIS);

    holdingPanel.setLayout(layout);
    textPanel.add(findLabel);
    textPanel.add(libraryField);
    buttonPanel.add(specifyButton);
    buttonPanel.add(reparseButton);
    buttonPanel.add(parseOnOpenCheckbox);
    buttonPanel.add(helpButton);
    buttonPanel.add(quitButton);
    holdingPanel.add(textPanel);
    holdingPanel.add(buttonPanel);
    scrollPane = new JScrollPane(resultsTable);
    holdingPanel.add(scrollPane);
    mainPanel.add(holdingPanel, CENTER);
    mainPanel.add(statusLabel, SOUTH);
    addListeners();
    setupTable();

    // setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);
  }

  private void addListeners()
  {
    quitButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          System.exit(0);
        }
      });
    reparseButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          reparse();
        }
      });
    specifyButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          specifyRepository();
        }
      });
    resultsTable.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent e)
        {
          try
          {
            showIvyLine(e);
          }
          catch (IOException e1)
          {
            e1.printStackTrace();  // todo show error dialog
          }
        }
      });
    addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosing(WindowEvent e)
        {
          super.windowClosing(e);
          System.exit(0);
        }
      });
    parseOnOpenCheckbox.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent actionEvent)
        {
          boolean isSelected = parseOnOpenCheckbox.isSelected();

          preferences.savaeParseOnOpen(isSelected);
        }
      });
    addHelpListener("ivyBrowserHelp.hs", helpButton, this);
  }

  public static final String IVY_REPOSITORY = "IvyRepository";

  @SuppressWarnings({ "LocalVariableHidesMemberVariable" })
  public void specifyRepository()
  {
    FindMultiplePreferencesItemsDialog dialog = new FindMultiplePreferencesItemsDialog(preferences, "Select Ivy Repository", IVY_REPOSITORY);

    dialog.setVisible(true);

    if (dialog.isOk())
    {
      String ivyRepositoryPath = dialog.getRepositoryLocation();

      if (!isEmpty(ivyRepositoryPath) && !ivyRepositoryPath.equals(this.ivyRepositoryPath))
      {
        this.ivyRepositoryPath = ivyRepositoryPath;
        reparse();
      }
    }
  }

  @SuppressWarnings({ "UseOfSystemOutOrSystemErr" })
  private void showIvyLine(MouseEvent e) throws IOException
  {
    int row = resultsTable.getSelectedRow();

    if (row > -1)
    {
      setCursor(WAIT_CURSOR);

      EventTableModel tableModel = (EventTableModel) resultsTable.getModel();
      IvyPackage      ivyFile    = (IvyPackage) tableModel.getElementAt(row);
      IvyLineDialog   dialog     = new IvyLineDialog(ivyFile, ivyRepositoryPath, this, preferences);

      setCursor(DEFAULT_CURSOR);
      dialog.setVisible(true);
    }
    else
    {
      System.out.println("No row selected...");
    }
  }

  private void setupTable()
  {
    SortedList<IvyPackage>      sortedPackages             = new SortedList<IvyPackage>(repositoryList);
    TextComponentMatcherEditor  textComponentMatcherEditor = new TextComponentMatcherEditor(libraryField, new IvyPackageFilterator());
    FilterList<IvyPackage>      filteredPackages           = new FilterList<IvyPackage>(sortedPackages, textComponentMatcherEditor);
    EventTableModel<IvyPackage> tableModel                 = new EventTableModel<IvyPackage>(filteredPackages, new IvyPackageTableFormat());

    resultsTable.setModel(tableModel);
    resultsTable.setDefaultRenderer(Object.class, new CheckboxCellRenderer(false));

    TableComparatorChooser<IvyPackage> tableSorter = new TableComparatorChooser<IvyPackage>(resultsTable, sortedPackages, true);
  }

  private void reparse()
  {
    setBusyCursor();
    holdingPanel.remove(scrollPane);
    progressPanel.start();
    ivyRepositoryPath = preferences.getIndexedProperty(IVY_REPOSITORY, 0);

    if (isEmpty(ivyRepositoryPath))
    {
      specifyRepository();
    }

    if (!isEmpty(ivyRepositoryPath))
    {
      repositoryList.clear();
      packageMap.clear();
      parsingHandler = HandlerFactory.getIvyRepositoryHandler(this, ivyRepositoryPath, repositoryList, packageMap, preferences);
      parsingHandler.execute();
      holdingPanel.add(scrollPane);
    }
  }

  // ------------------------ INTERFACE METHODS ------------------------
  // --------------------- Interface UiMainFrame ---------------------
  // tod implement these
  @Override
  public void addStatus(String statusLine)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Os getOs()
  {
    return null;  // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean getTestDataFromFile()
  {
    return false;  // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void initializeStatusBar(int minimum, int maximum, int initialValue, boolean visible)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean isTest()
  {
    return false;  // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setBusyCursor()
  {
    setCursor(busyCursor);
  }

  @Override
  public void setNormalCursor()
  {
    setCursor(normalCursor);
    statusLabel.setText("");
    adjustColumnWidths();
    libraryField.setEnabled(true);
    libraryField.requestFocus();
  }

  @Override
  public void setReady(boolean isReady)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void showSevereError(String message, Exception e)
  {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void stopThreads()
  {
    parsingHandler.halt();
  }

  @Override
  public void setStatusLabel(String text)
  {
    statusLabel.setText(text);
    progressPanel.setText(text);
  }

  @Override
  public void stopProgressPanel()
  {
    progressPanel.stop();
    setNormalCursor();
  }

  @Override
  public void resizeTableColumns()
  {
    sizeTableColumns(resultsTable);
  }

  // -------------------------- OTHER METHODS --------------------------
  private void adjustColumnWidths()
  {
    TableColumnModel columnModel = resultsTable.getColumnModel();

    for (int col = 0; col < resultsTable.getColumnCount(); col++)
    {
      int maxWidth = 50;

      for (int row = 0; row < resultsTable.getRowCount(); row++)
      {
        TableCellRenderer cellRenderer      = resultsTable.getCellRenderer(row, col);
        Object            value             = resultsTable.getValueAt(row, col);
        Component         rendererComponent = cellRenderer.getTableCellRendererComponent(resultsTable, value, false, false, row, col);

        maxWidth = Math.max(rendererComponent.getPreferredSize().width, maxWidth);
      }

      TableColumn column = columnModel.getColumn(col);

      column.setPreferredWidth(maxWidth);
      column.setMinWidth(maxWidth);
    }
  }

  public Map<String, Map<String, Map<String, IvyPackage>>> getPackageMap()
  {
    return packageMap;
  }

  // --------------------------- main() method ---------------------------
  @SuppressWarnings({ "ResultOfObjectAllocationIgnored" })
  public static void main(String[] args)
  {
    new IvyBrowserMainFrame();
  }
}
