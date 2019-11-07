/**
 * @file ProjectAnalyzerCodePatternMatch.java
 */
package analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
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
import model.ProgElem;
import util.UtilAST;
import util.UtilFile;
import view.SimpleTableViewer;
import util.UtilPath;
/**
 * @since JavaSE-1.8
 */
public class RenameProjectAnalyzer {
   final String JAVANATURE = "org.eclipse.jdt.core.javanature";
   String RUNTIME_PRJ_PATH;

   IProject[] projects;
   SimpleTableViewer viewer;
   private ProgElem curMethodElem;
   String str;
   IDocument doc;

   public RenameProjectAnalyzer(SimpleTableViewer v) {
     this.viewer = v;
   }

   public RenameProjectAnalyzer(ProgElem method, String q) {
	   this.curMethodElem = method;
	   this.str = q;
   }

   public void analyze() throws CoreException {
	      // =============================================================
	      // 1st step: Project
	      // =============================================================
	      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	      for (IProject project : projects) {
	         if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) { // Check if we have a Java project.
	            continue;
	         }
	         analyzePackages(JavaCore.create(project).getPackageFragments());
	      }
	   }

	   protected void analyzePackages(IPackageFragment[] packages) throws CoreException, JavaModelException {
	      // =============================================================
	      // 2nd step: Packages
	      // =============================================================
	      for (IPackageFragment iPackage : packages) {
	         if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE && //
	               iPackage.getCompilationUnits().length >= 1 && //
	               iPackage.getElementName().equals(curMethodElem.getPkg())) {
	            analyzeCompilationUnit(iPackage.getCompilationUnits());
	         }
	      }
	   }
	   private IMethod iMethod;
	   private void analyzeCompilationUnit(ICompilationUnit[] iCompilationUnits) throws JavaModelException {
	      // =============================================================
	      // 3rd step: ICompilationUnits
	      // =============================================================
	      for (ICompilationUnit iCUnit : iCompilationUnits) {
	         String nameICUnit = UtilPath.getClassNameFromJavaFile(iCUnit.getElementName());
	         if (nameICUnit.equals(this.curMethodElem.getClazz()) == false) {
	            continue;
	         }
	         CompilationUnit cUnit = UtilAST.parse(iCUnit);

	         ASTVisitor iMethodFinder = new ASTVisitor() {
	            public boolean visit(MethodDeclaration node) {
	               if (node.getName().getFullyQualifiedName().equals(curMethodElem.getMethod())) {
	                  IJavaElement javaElement = node.resolveBinding().getJavaElement();
	                  if (javaElement instanceof IMethod) {
	                     iMethod = (IMethod) javaElement;
	                  }
	               }
	               return true;
	            }
	         };
	         cUnit.accept(iMethodFinder);
	         UtilAST.rename(iMethod, this.str, IJavaRefactorings.RENAME_METHOD);
	      }
	   }
}