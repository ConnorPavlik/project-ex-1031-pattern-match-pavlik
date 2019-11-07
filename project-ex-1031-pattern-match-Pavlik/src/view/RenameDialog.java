package view;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RenameDialog extends TitleAreaDialog {
   private Text txtNewMethodName;
   private String newMethodName;

   public RenameDialog(Shell parentShell) {
      super(parentShell);
   }

   @Override
   public void create() {
      super.create();
      setTitle("Dialog");
      setMessage("Method Rename Dialog", IMessageProvider.INFORMATION);
   }

   @Override
   protected Control createDialogArea(Composite parent) {
      Composite area = (Composite) super.createDialogArea(parent);
      Composite container = new Composite(area, SWT.NONE);
      container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      GridLayout layout = new GridLayout(2, false);
      container.setLayout(layout);
      createNewMethodName(container);
      return area;
   }

   private void createNewMethodName(Composite container) {
      Label lbName = new Label(container, SWT.NONE);
      lbName.setText("New Method Name");

      GridData data = new GridData();
      data.grabExcessHorizontalSpace = true;
      data.horizontalAlignment = GridData.FILL;
      txtNewMethodName = new Text(container, SWT.BORDER);
      txtNewMethodName.setLayoutData(data);
   }

   @Override
   protected boolean isResizable() {
      return true;
   }

   private void saveInput() {
      newMethodName = txtNewMethodName.getText();
   }

   @Override
   protected void okPressed() {
      saveInput();
      super.okPressed();
   }

   protected Point getInitialSize() {
      return new Point(500, 200);
   }

   public String getNewMethodName() {
      return newMethodName;
   }
}
