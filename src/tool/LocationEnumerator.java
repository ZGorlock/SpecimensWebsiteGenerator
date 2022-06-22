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

public class LocationEnumerator {
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        Map<String, List<String>> locations = new HashMap<>();
        
        for (File specimen : Filesystem.getDirs(SpecimensWebsiteGenerator.specimensSource)) {
            File idFile = new File(specimen, "id.txt");
            if (!idFile.exists()) {
                System.err.println("Id not found: " + idFile.getAbsolutePath());
                continue;
            }
            
            List<String> lines = Filesystem.readLines(idFile);
            if (lines.stream().filter(String::isEmpty).count() < 2) {
                continue;
            }
            
            boolean inLocation = false;
            List<String> location = new ArrayList<>();
            for (String line : lines) {
                if (line.isEmpty()) {
                    if (inLocation) {
                        break;
                    }
                    inLocation = true;
                    continue;
                }
                if (inLocation) {
                    location.add(line);
                    if (line.matches(".*,\\s\\d+m$")) {
                        break;
                    }
                }
            }
            
            String locationKey = String.join(" | ", location);
            if (!location.isEmpty() && location.get(0).startsWith("USA")) {
                locations.putIfAbsent(locationKey, new ArrayList<>());
                locations.get(locationKey).add(StringUtility.lSnip(specimen.getName(), 4));
            } else {
                System.err.println(StringUtility.lSnip(specimen.getName(), 4) + " - " + locationKey);
            }
            System.out.println();
        }
        
        locations.entrySet().stream().sorted(Comparator.comparingInt(o -> -o.getValue().size())).forEachOrdered(e -> {
            System.out.println(e.getKey() + " (" + e.getValue().size() + ')');
            System.out.println(e.getKey().replace(" | ", System.lineSeparator()));
            System.out.println();
        });
        
    }
    
}
