/**
 * @file SimpleTableViewer.java
 */
package view;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import analysis.ProjectAnalyzerSearch;
import analysis.ProjectAnalyzerSearchAllMethodNames;
import analysis.RenameProjectAnalyzer;
import model.LabelProviderProgElem;
import model.ModelProvider;
import model.ProgElem;
import util.UtilConfig;
import util.UtilFile;

/**
 * @since JavaSE-1.8
 */
public class SimpleTableViewer {
   public static String VIEW_ID = "project-ex-1031-pattern-match-pavlik.partdescriptor.simplecodepatternmatchviewpavlik";
   private TableViewer viewer;
   private Text searchText;
   private Text searchTextQualifier;
   private Text searchTextMethodName;

   public SimpleTableViewer() {
      UtilConfig.load();
   }

   @PostConstruct
   public void createControls(Composite parent) {
      GridLayout layout = new GridLayout(2, false);
      parent.setLayout(layout);
      createSearchTextV1(parent);
      createSearchTextV2(parent);
      createViewer(parent);

      // * Use this popup menu interface to start.
      Menu popupMenu = new Menu(viewer.getTable());
      viewer.getTable().setMenu(popupMenu);
      final MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
      menuItem.setText("Rename Selected Method");
      menuItem.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
            TableItem item = viewer.getTable().getSelection()[0];
            Object data = item.getData();
            if (data instanceof ProgElem) {
               ProgElem progElem = (ProgElem) data;
               RenameDialog dialog = new RenameDialog(viewer.getTable().getShell());
               dialog.create();
               if (dialog.open() == Window.OK) {
                  System.out.println("[DBG] PACKAGE NAME: " + progElem.getPkg());
                  System.out.println("[DBG] CLASS NAME: " + progElem.getClazz());
                  System.out.println("[DBG] METHOD NAME: " + progElem.getMethod());
                  System.out.println("[DBG] NEW METHOD NAME: " + dialog.getNewMethodName());
               }
               
               try {
            	   RenameProjectAnalyzer name = new RenameProjectAnalyzer(progElem, dialog.getNewMethodName());
            	   name.analyze();
                } catch (CoreException ex) {
                   ex.printStackTrace();
                }
            }
         }
      });
      //

      viewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
         public void handleEvent(Event event) {
            TableItem[] selection = viewer.getTable().getSelection();
            if (selection == null || selection.length != 1) {
               return;
            }
            TableItem item = viewer.getTable().getSelection()[0];
            Object data = item.getData();
            if (data instanceof ProgElem) {
               ProgElem progElem = (ProgElem) data;
               String filePath = progElem.getFile();
               UtilFile.openEditor(new File(filePath), progElem.getLineNumber());
            }
         }
      });
   }

   private void createSearchTextV1(Composite parent) {
      Label searchLabel = new Label(parent, SWT.NONE);
      searchLabel.setText("Search V1 (by Method Signature): ");

      Composite container = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(1, true);
      container.setLayout(layout);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = GridData.FILL;
      container.setLayoutData(gridData);

      searchText = new Text(container, SWT.BORDER | SWT.SEARCH);
      searchText.setText("foo(*,*) void");
      searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

      searchText.addListener(SWT.KeyDown, new Listener() {
         public void handleEvent(Event e) {
            if (e.keyCode == SWT.CR) {
               reset();
               ProjectAnalyzerSearch analyzer = new ProjectAnalyzerSearch(searchText.getText());
               analyzer.analyze();

               Object data = ModelProvider.INSTANCE.getProgramElements();
               viewer.setInput(data);
               viewer.refresh();
            }
         }
      });
   }

   private void createSearchTextV2(Composite parent) {
      Label searchLabel = new Label(parent, SWT.NONE);
      searchLabel.setText("Search V2 (by Program Element Name): ");

      Composite container = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout(2, true);
      container.setLayout(layout);
      GridData gridData = new GridData();
      gridData.horizontalAlignment = GridData.FILL;
      container.setLayoutData(gridData);

      searchTextQualifier = new Text(container, SWT.BORDER | SWT.SEARCH);
      searchTextQualifier.setText("pkg*");
      searchTextQualifier.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

      searchTextMethodName = new Text(container, SWT.BORDER | SWT.SEARCH);
      searchTextMethodName.setText("ba*");
      searchTextMethodName.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));

      Listener evtListener = new Listener() {
         public void handleEvent(Event e) {
            if (e.keyCode == SWT.CR) {
               reset();
               ProjectAnalyzerSearchAllMethodNames analyzer = //
                     new ProjectAnalyzerSearchAllMethodNames(searchTextQualifier.getText(), //
                           searchTextMethodName.getText());
               analyzer.analyze();

               Object data = ModelProvider.INSTANCE.getProgramElements();
               viewer.setInput(data);
               viewer.refresh();
            }
         }
      };
      searchTextQualifier.addListener(SWT.KeyDown, evtListener);
      searchTextMethodName.addListener(SWT.KeyDown, evtListener);
   }

   private void createViewer(Composite parent) {
      viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
      createColumnsProgElem(parent, viewer);
      final Table table = viewer.getTable();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);
      viewer.setContentProvider(ArrayContentProvider.getInstance());

      GridData gridData = new GridData();
      gridData.verticalAlignment = GridData.FILL;
      gridData.horizontalSpan = 2;
      gridData.grabExcessHorizontalSpace = true;
      gridData.grabExcessVerticalSpace = true;
      gridData.horizontalAlignment = GridData.FILL;
      viewer.getControl().setLayoutData(gridData);
   }

   public void setInput(Object data) {
      viewer.setInput(data);
   }

   private void createColumnsProgElem(final Composite parent, final TableViewer viewer) {
      String[] titles = { "Search Results" };
      int[] bounds = { 500 };
      TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0]);
      col.setLabelProvider(new LabelProviderProgElem());
   }

   private TableViewerColumn createTableViewerColumn(String title, int bound) {
      final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
      final TableColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(bound);
      column.setResizable(true);
      column.setMoveable(true);
      return viewerColumn;
   }

   @PreDestroy
   public void dispose() {
   }

   @Focus
   public void setFocus() {
   }

   public void refresh() {
      viewer.refresh();
   }

   public void reset() {
      ModelProvider.INSTANCE.getProgramElements().clear();
      setInput(ModelProvider.INSTANCE.getProgramElements());
      refresh();
   }

   public String getQuery() {
      return searchText.getText().toString();
   }
}
