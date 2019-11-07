/**
 * @file ProjectAnalyzerCodePatternMatch.java
 */
package analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import model.ModelProvider;
import util.UtilFile;
import view.SimpleTableViewer;

/**
 * @since JavaSE-1.8
 */
public class ProjectAnalyzerSearch {
   final String JAVANATURE = "org.eclipse.jdt.core.javanature";
   String RUNTIME_PRJ_PATH;

   IProject[] projects;
   SimpleTableViewer viewer;
   String query;
   IDocument doc;

   public ProjectAnalyzerSearch(SimpleTableViewer v) {
      this.viewer = v;
      this.viewer.reset();
      this.query = v.getQuery();
      RUNTIME_PRJ_PATH = System.getProperty("RUNTIME_PRJ_PATH");
   }

   public ProjectAnalyzerSearch(String q) {
      this.query = q;
      RUNTIME_PRJ_PATH = System.getProperty("RUNTIME_PRJ_PATH");
   }

   public void analyze() {
      try {
         projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
         for (IProject project : projects) {
            if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) {
               continue;
            }
            analyzePackages(JavaCore.create(project).getPackageFragments());
         }
      } catch (Exception e) {
      }
   }

   protected void analyzePackages(IPackageFragment[] packages) throws CoreException, JavaModelException {
      List<IPackageFragment> listIPackageFragment = new ArrayList<IPackageFragment>();

      for (IPackageFragment iPackage : packages) {
         if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
            if (iPackage.getCompilationUnits().length < 1) {
               continue;
            }
            listIPackageFragment.add(iPackage);
         }
      }
      search(listIPackageFragment.toArray(new IPackageFragment[0]));
   }

   protected void search(IPackageFragment[] packages) throws CoreException, JavaModelException {

      // =============================================================
      // step 1: Create a search pattern
      SearchPattern pattern = SearchPattern.createPattern(this.query, //
            IJavaSearchConstants.METHOD, //
            IJavaSearchConstants.DECLARATIONS, //
            SearchPattern.R_PATTERN_MATCH);

      // =============================================================
      // step 2: Create search scope
      IJavaSearchScope scope = SearchEngine.createJavaSearchScope(packages);

      // =============================================================
      // step 3: define a result collector
      SearchRequestor requestor = new SearchRequestor() {
         @Override
         public void acceptSearchMatch(SearchMatch match) throws CoreException {
            Object element = match.getElement();

            if (element != null && element instanceof IMethod) {
               IMethod method = (IMethod) element;
               IType declaringType = method.getDeclaringType();
               String filePath = method.getCompilationUnit().getPath().toFile().getAbsolutePath();

               try {
                  String source = UtilFile.readEntireFile(RUNTIME_PRJ_PATH + filePath);
                  IDocument doc = new Document(source);
                  int offset = method.getSourceRange().getOffset();
                  int lineNumber = doc.getLineOfOffset(offset) + 1;
                  ModelProvider.INSTANCE.addProgramElements(RUNTIME_PRJ_PATH + filePath, //
                        declaringType.getPackageFragment().getElementName(), //
                        declaringType.getElementName(), method.getElementName(), offset, lineNumber);
               } catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }
      };

      // =============================================================
      // step4: start searching
      SearchEngine searchEngine = new SearchEngine();
      searchEngine.search(pattern, //
            new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, //
            scope, requestor, null);
   }
}