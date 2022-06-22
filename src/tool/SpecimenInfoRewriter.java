/*
 * File:    SpecimenInfoRewriter.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.io.File;
import java.util.List;

import commons.access.Filesystem;
import main.SpecimensWebsiteGenerator;

public class SpecimenInfoRewriter {
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        final List<String> infoFiles = List.of("id.txt", "taxonomy.txt");
        
        for (File specimen : Filesystem.getDirs(SpecimensWebsiteGenerator.specimensSource)) {
            for (String infoFileName : infoFiles) {
                File infoFile = new File(specimen, infoFileName);
                if (!infoFile.exists()) {
                    System.err.println("File not found: " + infoFile.getAbsolutePath());
                    continue;
                }
                
                List<String> lines = Filesystem.readLines(infoFile);
                Filesystem.writeLines(infoFile, lines);
                
                List<String> newLines = Filesystem.readLines(infoFile);
                if (lines.size() > newLines.size()) {
                    throw new RuntimeException("New file shorter than original: " + infoFile.getAbsolutePath());
                }
                boolean changed = false;
                for (int i = 0; i < newLines.size(); i++) {
                    if (!newLines.get(i).isEmpty()) {
                        if (!newLines.get(i).equals(lines.get(i))) {
                            changed = true;
                        }
                    }
                }
                if (changed || (newLines.size() != lines.size())) {
                    System.out.println("File changed: " + infoFile.getAbsolutePath());
                }
            }
        }
    }
    
}
