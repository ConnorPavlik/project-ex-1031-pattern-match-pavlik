package model;

import java.util.ArrayList;
import java.util.List;

public enum ModelProvider {
   INSTANCE;
   private List<ProgElem> progElements = new ArrayList<ProgElem>();;

   public void addProgramElements(String fileName, String pkg, String clazz, String method, int offset, int lineNum) {
      progElements.add(new ProgElem(fileName, pkg, clazz, method, offset, lineNum));
   }

   public List<ProgElem> getProgramElements() {
      return progElements;
   }

   public void clearProgramElements() {
      progElements.clear();
   }
}
