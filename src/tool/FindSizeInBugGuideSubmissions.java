/*
 * File:    FindSizeInBugGuideSubmission.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Filesystem;
import common.Internet;
import main.SpecimensWebsiteGenerator;
import org.jsoup.nodes.Document;
import utility.ResourceUtility;

public class FindSizeInBugGuideSubmissions {
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        Pattern sizePattern = Pattern.compile(".*>Size:\\s(?<size>[0-9.]+mm)\\s*<.*");
        for (File specimenDir : Filesystem.getDirs(SpecimensWebsiteGenerator.source)) {
            File bugGuideSubmission = new File(specimenDir, "BugGuide Submission.url");
            if (bugGuideSubmission.exists()) {
                String url = ResourceUtility.getUrlFromShortcut(bugGuideSubmission);
                Document doc = Internet.getHtml(url);
                if (doc != null) {
                    Matcher sizeMatcher = sizePattern.matcher(doc.toString().replaceAll("\r?\n", ""));
                    if (sizeMatcher.matches()) {
                        String size = sizeMatcher.group("size");
                        System.out.println(specimenDir.getName() + " : " + size);
                    }
                }
            }
        }
    }
    
}
