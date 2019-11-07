package model;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

public class LabelProviderProgElem extends StyledCellLabelProvider {
   @Override
   public void update(ViewerCell cell) {
      String cellText = getCellText(cell);
      cell.setText(cellText);
      super.update(cell);
   }

   protected String getCellText(ViewerCell cell) {
      ProgElem p = (ProgElem) cell.getElement();
      String cellText = p.getPkg() + "." + p.getClazz() + "." + p.getMethod() + //
            " at LINE: " + p.getLineNumber();
      return cellText;
   }
}
