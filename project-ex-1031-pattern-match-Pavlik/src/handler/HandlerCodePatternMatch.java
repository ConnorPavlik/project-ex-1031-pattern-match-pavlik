/**
 * @file HandlerCodePatternMatch.java
 */
package handler;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import analysis.ProjectAnalyzerSearch;
import model.ModelProvider;
import view.SimpleTableViewer;

/**
 * @since JavaSE-1.8
 */
public class HandlerCodePatternMatch {
   @Execute
   public void execute(EPartService service) throws CoreException {
      MPart part = service.findPart(SimpleTableViewer.VIEW_ID);

      if (part != null) {
         if (part.getObject() instanceof SimpleTableViewer) {
            SimpleTableViewer viewer = (SimpleTableViewer) part.getObject();
            ProjectAnalyzerSearch analyzer = new ProjectAnalyzerSearch(viewer);
            analyzer.analyze();
            
            Object data = ModelProvider.INSTANCE.getProgramElements();
            viewer.setInput(data);
            viewer.refresh();
            System.out.println("[DBG] Done.");
         }
      }
   }
}