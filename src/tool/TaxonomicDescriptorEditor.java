/*
 * File:    TaxonomicDescriptorEditor.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.io.File;
import java.util.List;

import commons.access.Filesystem;
import main.SpecimensWebsiteGenerator;

public class TaxonomicDescriptorEditor {
    
    private static final String taxon = "Syrphidae";
    
    private static final String oldDescriptor = "Syrphid Flies";
    
    private static final String newDescriptor = "Hover Flies";
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        String replaceFrom = ' ' + taxon + " (" + oldDescriptor + ')';
        String replaceTo = ' ' + taxon + " (" + newDescriptor + ')';
        
        for (File specimen : Filesystem.getDirs(SpecimensWebsiteGenerator.specimensSource)) {
            File taxonomy = new File(specimen, "taxonomy.txt");
            if (!taxonomy.exists()) {
                System.err.println("Taxonomy not found: " + taxonomy.getAbsolutePath());
                continue;
            }
            
            boolean edited = false;
            List<String> lines = Filesystem.readLines(taxonomy);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.contains(replaceFrom)) {
                    lines.set(i, line.replace(replaceFrom, replaceTo));
                    edited = true;
                }
            }
            if (edited) {
                Filesystem.writeLines(taxonomy, lines);
                System.out.println("Updated taxonomic descriptor for: " + taxonomy.getAbsolutePath());
            }
        }
    }
    
}
