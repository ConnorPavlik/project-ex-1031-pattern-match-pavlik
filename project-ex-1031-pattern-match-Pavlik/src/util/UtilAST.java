/**
 */
package util;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class UtilAST {
   static final int INVALID_DOC = -1;
   static String fileContents = null;

   public static ASTParser parse() {
      ASTParser parser = ASTParser.newParser(AST.JLS11);
      configParser(parser);
      return parser;
   }

   public static CompilationUnit parse(char[] unit) {
      ASTParser parser = parse();
      parser.setSource(unit);
      return (CompilationUnit) parser.createAST(null); // parse
   }

   public static CompilationUnit parse(ICompilationUnit unit) {
      ASTParser parser = parse();
      parser.setSource(unit);
      return (CompilationUnit) parser.createAST(null); // parse
   }

   private static void configParser(ASTParser parser) {
      parser.setResolveBindings(true);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setBindingsRecovery(true);
      Map<String, String> options = JavaCore.getOptions();
      options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
      options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
      options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
      parser.setCompilerOptions(options);
   }

   public static boolean contains(ICompilationUnit iUnit, String typeName) {
      boolean rst = false;
      try {
         IType[] types = iUnit.getAllTypes();
         for (IType iType : types) {
            String iTypeName = iType.getElementName();
            if (typeName.equals(iTypeName)) {
               rst = true;
               break;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return rst;
   }

   /*
    * http://www.eclipse.org/articles/article.php?file=Article-Unleashing-the-Power-of-Refactoring/index.html
    */
   public static void rename(IJavaElement javaElement, String newName, String kind) {
      RefactoringContribution contribution = RefactoringCore.getRefactoringContribution(kind);
      RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor) contribution.createDescriptor();
      String projectName = javaElement.getResource().getProject().getName();
      descriptor.setProject(projectName);
      descriptor.setUpdateReferences(true);
      descriptor.setNewName(newName);
      descriptor.setJavaElement(javaElement);

      RefactoringStatus status = new RefactoringStatus();
      try {
         Refactoring refactoring = descriptor.createRefactoring(status);
         IProgressMonitor monitor = new NullProgressMonitor();
         refactoring.checkInitialConditions(monitor);
         refactoring.checkFinalConditions(monitor);
         Change change = refactoring.createChange(monitor);
         change.perform(monitor);
      } catch (CoreException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

}
