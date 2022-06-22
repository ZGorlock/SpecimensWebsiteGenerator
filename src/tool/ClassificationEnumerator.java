/*
 * File:    ClassificationEnumerator.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import commons.access.Filesystem;
import commons.string.StringUtility;
import main.SpecimensWebsiteGenerator;

public class ClassificationEnumerator {
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        Map<String, List<String>> classifications = new HashMap<>();
        
        for (File specimen : Filesystem.getDirs(SpecimensWebsiteGenerator.specimensSource)) {
            File idFile = new File(specimen, "id.txt");
            if (!idFile.exists()) {
                System.err.println("Id not found: " + idFile.getAbsolutePath());
                continue;
            }
            
            List<String> lines = Filesystem.readLines(idFile);
            if (!lines.get(1).isEmpty() && !lines.get(1).matches("\\d.*")) {
                String classification = lines.get(1);
                classifications.putIfAbsent(classification, new ArrayList<>());
                classifications.get(classification).add(StringUtility.lSnip(specimen.getName(), 4));
            }
        }
        
        classifications.entrySet().stream().sorted(Comparator.comparingInt(o -> -o.getValue().size())).forEachOrdered(e ->
                System.out.println(e.getKey() + " (" + e.getValue().size() + ')'));
        
    }
    
}
