/*
 * File:    TaxidermyWebsiteGenerator.java
 * Package: 
 * Author:  Zachary Gill
 */

import javax.imageio.ImageIO;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Filesystem;
import common.StringUtility;

public class TaxidermyWebsiteGenerator {
    
    private static final File source = new File("E:/Documents/Taxidermy/Specimens");
    private static final File sink = new File("E:/Coding/HTML/Taxidermy");
    
    private static final boolean fullCopy = true;
    
    private static final Map<String, String> specimens = new LinkedHashMap<>();
    
    
    public static void main(String[] args) throws Exception {
        Filesystem.clearDirectory(sink);
        
        makeIndex();
        makeMainPage();
        makeStyle();
        makeSpecimenPages();
        makeNavbar();
    }
    
    
    private static void makeIndex() throws Exception {
        File landingPage = new File(sink, "index.html");
        Filesystem.writeLines(landingPage, wrapHtml(null, true, false, false));
    }
    
    private static void makeMainPage() throws Exception {
        File mainPage = new File(sink, "main.html");
        List<String> content = new ArrayList<>();
        content.add("<h1>SPECIMENS</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        
        File coverImage = new File(source, "Specimens.jpg");
        if (coverImage.exists()) {
            content.add("<center>");
            content.add("\t<img src=\"" + linkImage(coverImage, sink, -1) + "\" width=\"65%\" height=\"65%\"/>");
            content.add("</center>");
            content.add("<br>");
            content.add("");
        }
    
        content.add("<center>");
        content.add("\t<p>Zachary Gill</p>");
        content.add("</center>");
        Filesystem.writeLines(mainPage, wrapHtml(content, false, false, false));
    }
    
    private static void makeNavbar() throws Exception {
        List<String> content = new ArrayList<>();
        content.add("<a href=\"index.html\" target=\"_top\">HOME</a>");
        for (Map.Entry<String, String> specimen : specimens.entrySet()) {
            content.add("<a href=\"specimens/" + specimen.getKey() + "/main.html\" target=\"_top\">" + specimen.getValue() + "</a>");
        }
        content.add("<br>");
        content.add("<br>");
        content.add("<br>");
        Filesystem.writeLines(new File(sink, "navbar.html"), wrapHtml(content, false, false, true));
    }
    
    private static void makeStyle() throws Exception {
        File style = new File(sink, "css/style.css");
        List<String> content = new ArrayList<>();
        content.add(".navbarFrame {float:left; width:20%; height:98%; position: fixed;}");
        content.add(".mainFrame {float:left; width:79%; height:98%; margin-left: 20%; position: fixed;}");
        content.add("");
        content.add("body {color: #f1f1f1; background-color: #111; font: normal normal normal 16px oloron, monospace;}");
        content.add("h1 {font: normal bold normal 48px monospace; text-align: center; text-decoration: underline, overline; text-transform: uppercase;}");
        content.add("");
        content.add("a:link {color: #818181; text-decoration: underline;}");
        content.add("a:visited {color: #818181; text-decoration: underline;}");
        content.add("a:hover {color: #f1f1f1;}");
        content.add("");
        content.add(".td-left {text-align: right; padding-right: 8px; width: 50%;}");
        content.add(".td-right {text-align: left; padding-left: 8px; width: 50%;}");
        content.add("");
        content.add(".navbar {height: 100%; width: 100%; position: fixed; z-index: 1; top: 0; left: 0; background-color: #111; overflow-x: hidden; padding-top: 20px;}");
        content.add(".navbar a {padding: 6px 8px 6px 16px; text-decoration: none; font-size: 14px; color: #818181; display: block;}");
        Filesystem.writeLines(style, content);
    }
    
    private static void makeSpecimenPages() throws Exception {
        File specimensSinkDir = new File(sink, "specimens");
        Filesystem.createDirectory(specimensSinkDir);
        
        List<File> specimenDirs = Filesystem.getDirs(source);
        for (File specimenDir : specimenDirs) {
            makeSpecimenPage(specimenDir, specimensSinkDir);
        }
    }
    
    private static void makeSpecimenPage(File specimenDir, File specimensSinkDir) throws Exception {
        String name = specimenDir.getName();
        String id = StringUtility.trim(name.substring(0, name.indexOf('-')));
        specimens.put(id, name);
        
        File specimenSinkDir = new File(specimensSinkDir, id);
        Filesystem.createDirectory(specimenSinkDir);
        
        Filesystem.writeLines(new File(specimenSinkDir, "main.html"), wrapHtml(null, true, true, false));
        
        List<String> content = new ArrayList<>();
        content.add("<h1>" + name + "</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        content.add("<center>");
        
        File idFile = new File(specimenDir, "id.txt");
        if (idFile.exists()) {
            content.add("\t<p>");
            List<String> idLines = Filesystem.readLines(idFile);
            for (int i = 0; i < idLines.size(); i++) {
                content.add("\t\t" + ((i == 0) ? "<b>" : "") + idLines.get(i) + ((i == idLines.size() - 1) ? "</b>" : "<br>"));
            }
            content.add("\t</p>");
            content.add("");
            content.add("\t<br>");
            content.add("\t<hr>");
            content.add("\t<br>");
            content.add("");
        }
        
        File bugGuide = new File(specimenDir, "BugGuide Submission.url");
        if (bugGuide.exists()) {
            String bugGuideUrl = getUrlFromShortcut(bugGuide);
            content.add("\t<p>");
            content.add("\t\t<a href=\"" + bugGuideUrl + "\">BugGuide Submission</a>");
            content.add("\t</p>");
            content.add("");
            content.add("\t<br>");
            content.add("\t<hr>");
            content.add("\t<br>");
            content.add("");
        }
        
        File taxonomy = new File(specimenDir, "taxonomy.txt");
        if (taxonomy.exists()) {
            content.add("\t<table>");
            List<String> taxonomyLines = Filesystem.readLines(taxonomy);
            for (String taxonomyLine : taxonomyLines) {
                if (taxonomyLine.toUpperCase().startsWith("NO TAXON")) {
                    continue;
                }
                String key = StringUtility.trim(taxonomyLine.substring(0, taxonomyLine.indexOf(' ')));
                String value = StringUtility.trim(taxonomyLine.substring(taxonomyLine.indexOf(' ')).replaceAll("\\(.*$", ""));
                content.add("\t\t<tr>");
                content.add("\t\t\t<td class=\"td-left\"><b>" + key + "</b></td>");
                content.add("\t\t\t<td class=\"td-right\">" + value + "</td>");
                content.add("\t\t</tr>");
            }
            content.add("\t</table>");
            content.add("");
            content.add("\t<br>");
            content.add("\t<hr>");
            content.add("\t<br>");
            content.add("");
        }
        
        File photosDir = new File(specimenDir, "Photos");
        if (photosDir.exists()) {
            List<File> photoSubDirs = Filesystem.getDirs(photosDir);
            int photoSubDirIndex = 0;
            for (File photoSubDir : photoSubDirs) {
                
                String photoDirName = photoSubDir.getName().replaceAll("\\d\\s-\\s", "");
                content.add("\t<p>" + photoDirName + "</p>");
                content.add("\t<br>");
                
                List<File> photoList = Filesystem.getFiles(photoSubDir);
                for (File video : photoList) {
                    if (video.getName().toLowerCase().endsWith("mp4")) {
                        content.add("\t<video width=\"720\" height=\"480\" controls><source src=\"" + linkImage(video, specimenSinkDir, photoSubDirIndex) + "\" type=\"video/mp4\"></video><br>");
                    }
                }
                for (File photo : photoList) {
                    if (!photo.getName().toLowerCase().endsWith("mp4")) {
                        content.add("\t<img src=\"" + linkImage(photo, specimenSinkDir, photoSubDirIndex) + "\" width=\"50%\" height=\"50%\"/>");
                    }
                }
                
                content.add("\t<br>");
                content.add("");
                content.add("\t<br>");
                content.add("\t<hr>");
                content.add("\t<br>");
                content.add("");
                
                photoSubDirIndex++;
            }
        }
        
        File reference = new File(specimenDir, "Reference");
        if (reference.exists()) {
            List<File> references = Filesystem.getFiles(reference);
            Map<String, String> referenceMap = new LinkedHashMap<>();
            for (File referenceEntry : references) {
                if (referenceEntry.getName().toUpperCase().contains("BUGGUIDE")) {
                    referenceMap.putIfAbsent(referenceEntry.getName(), getUrlFromShortcut(referenceEntry));
                }
            }
            for (File referenceEntry : references) {
                if (!referenceEntry.getName().toUpperCase().contains("BUGGUIDE") && !referenceEntry.getName().toUpperCase().contains("WIKIPEDIA")) {
                    referenceMap.putIfAbsent(referenceEntry.getName(), getUrlFromShortcut(referenceEntry));
                }
            }
            for (File referenceEntry : references) {
                if (referenceEntry.getName().toUpperCase().contains("WIKIPEDIA")) {
                    referenceMap.putIfAbsent(referenceEntry.getName(), getUrlFromShortcut(referenceEntry));
                }
            }
    
            content.add("\t<p>References</p>");
            for (Map.Entry<String, String> referenceEntry : referenceMap.entrySet()) {
                content.add("\t<a href=\"" + referenceEntry.getValue() + "\">" + StringUtility.rShear(referenceEntry.getKey(), 4) + "</a><br>");
            }
            
            content.add("");
            content.add("\t<br>");
            content.add("\t<hr>");
            content.add("");
            
        }
        content.add("</center>");
        
        Filesystem.writeLines(new File(specimenSinkDir, "content.html"), wrapHtml(content, false, true, false));
    }
    
    
    private static List<String> wrapHtml(List<String> content, boolean index, boolean specimen, boolean navbar) throws Exception {
        List<String> wrapped = new ArrayList<>();
        wrapped.add("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        wrapped.add("\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        wrapped.add("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">");
        wrapped.add("");
        wrapped.add("\t<head>");
        wrapped.add("\t\t<title>Specimens</title>");
        wrapped.add("\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + (specimen ? "../../" : "") + "css/style.css\"/>");
        wrapped.add("\t</head>");
        wrapped.add("");
        wrapped.add("\t<body>");
        wrapped.add("\t\t<div" + (index ? ">" : (" class=\"" + (navbar ? "navbar" : "main") + "\">")));
        if (index) {
            wrapped.add("\t\t\t<iframe class=\"navbarFrame\" src=\"" + (specimen ? "../../" : "") + "navbar.html\"></iframe>");
            wrapped.add("\t\t\t<iframe class=\"mainFrame\" src=\"" + (specimen ? "content" : "main") + ".html\"></iframe>");
        } else {
            for (String contentLine : content) {
                wrapped.add("\t\t\t" + contentLine);
            }
        }
        wrapped.add("\t\t</div>");
        wrapped.add("\t</body>");
        wrapped.add("");
        wrapped.add("</html>");
        return wrapped;
    }
    
    private static String linkImage(File source, File destDir, int index) throws Exception {
        File imageDir = new File(destDir, "images");
        if (!imageDir.exists()) {
            Filesystem.createDirectory(imageDir);
        }
        if (index >= 0) {
            imageDir = new File(imageDir, String.valueOf(index));
            if (!imageDir.exists()) {
                Filesystem.createDirectory(imageDir);
            }
        }
        File link = new File(imageDir, StringUtility.rShear(source.getName(), 4) + StringUtility.rSnip(source.getName(), 4).toLowerCase());
        
        if (fullCopy) {
            Filesystem.copyFile(source, link);
        } else {
            Path linkPath = link.toPath();
            Path sourcePath = source.toPath();
            Files.createLink(linkPath, sourcePath);
        }
        return "images/" + ((index >= 0) ? (index + "/") : "") + link.getName();
    }
    
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
