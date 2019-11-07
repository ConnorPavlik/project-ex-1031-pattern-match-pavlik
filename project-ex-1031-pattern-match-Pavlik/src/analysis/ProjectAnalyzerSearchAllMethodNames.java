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
import org.eclipse.jdt.core.search.MethodNameMatch;
import org.eclipse.jdt.core.search.MethodNameMatchRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import model.ModelProvider;
import util.UtilFile;
import view.SimpleTableViewer;

/**
 * @since JavaSE-1.8
 */
public class ProjectAnalyzerSearchAllMethodNames {
   final String JAVANATURE = "org.eclipse.jdt.core.javanature";
   String RUNTIME_PRJ_PATH;

   IProject[] projects;
   SimpleTableViewer viewer;
   String queryQualifiedName, queryMethodName;
   IDocument doc;

   public ProjectAnalyzerSearchAllMethodNames(SimpleTableViewer v) {
      this.viewer = v;
      this.viewer.reset();
      this.queryMethodName = v.getQuery();
      RUNTIME_PRJ_PATH = System.getProperty("RUNTIME_PRJ_PATH");
   }

   public ProjectAnalyzerSearchAllMethodNames(String queryQualifiedName, String queryMethodName) {
      this.queryQualifiedName = queryQualifiedName;
      this.queryMethodName = queryMethodName;
      RUNTIME_PRJ_PATH = System.getProperty("RUNTIME_PRJ_PATH");
   }

   public void analyze() {
      // =============================================================
      // 1st step: Project
      // =============================================================
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
      // =============================================================
      // 2nd step: Packages
      // =============================================================
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
      // step 1: Create search scope
      IJavaSearchScope scope = SearchEngine.createJavaSearchScope(packages);

      // =============================================================
      // step 2: define a result collector
      MethodNameMatchRequestor methodRequestor = new MethodNameMatchRequestor() {
         @Override
         public void acceptMethodNameMatch(MethodNameMatch match) {
            Object element = match.getMethod();

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
      // step 3: start searching
      SearchEngine searchEngine = new SearchEngine();
      searchEngine.searchAllMethodNames((this.queryQualifiedName + "*").toCharArray(), //
            SearchPattern.R_PATTERN_MATCH, //
            (this.queryMethodName + "*").toCharArray(), //
            SearchPattern.R_PATTERN_MATCH, //
            scope, //
            methodRequestor, //
            IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
   }
}