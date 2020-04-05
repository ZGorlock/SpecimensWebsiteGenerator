/*
 * File:    FindSizeInBugGuideSubmission.java
 * Package: utility
 * Author:  Zachary Gill
 */

package utility;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Filesystem;
import common.Internet;
import common.StringUtility;
import org.jsoup.nodes.Document;

public class FindSizeInBugGuideSubmissions {
    
    //Constants
    
    private static final File source = new File("E:/Documents/Specimens/Specimens");
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        Pattern sizePattern = Pattern.compile(".*>Size:\\s(?<size>[0-9.]+mm)\\s*<.*");
        for (File specimenDir : Filesystem.getDirs(source)) {
            File bugGuideSubmission = new File(specimenDir, "BugGuide Submission.url");
            if (bugGuideSubmission.exists()) {
                String url = getUrlFromShortcut(bugGuideSubmission);
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
    
    
    //Functions
    
    private static String getUrlFromShortcut(File shortcut) throws Exception {
        String content = StringUtility.removeWhiteSpace(Filesystem.readFileToString(shortcut));
        Pattern getUrlPattern = Pattern.compile("^.*URL=(?<url>.+)$");
        Matcher getUrlMatcher = getUrlPattern.matcher(content);
        if (getUrlMatcher.matches()) {
            return getUrlMatcher.group("url");
        }
        return "";
    }
    
}
