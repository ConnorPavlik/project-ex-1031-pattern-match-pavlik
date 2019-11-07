package model;

public class ProgElem {
   String file, pkg, clazz, method;
   int offset, lineNumber;

   public ProgElem(String fileName, String pkg, String clazz, String method, int offset, int lineNum) {
      this.file = fileName;
      this.pkg = pkg;
      this.clazz = clazz;
      this.method = method;
      this.offset = offset;
      this.lineNumber = lineNum;
   }

   public String getFile() {
      return file;
   }

   public void setFile(String file) {
      this.file = file;
   }

   public String getPkg() {
      return pkg;
   }

   public void setPkg(String pkg) {
      this.pkg = pkg;
   }

   public String getClazz() {
      return clazz;
   }

   public void setClazz(String clazz) {
      this.clazz = clazz;
   }

   public String getMethod() {
      return method;
   }

   public void setMethod(String method) {
      this.method = method;
   }

   public int getOffset() {
      return offset;
   }

   public void setOffset(int offset) {
      this.offset = offset;
   }

   public int getLineNumber() {
      return lineNumber;
   }

   public void setLineNumber(int lineNumber) {
      this.lineNumber = lineNumber;
   }
}
