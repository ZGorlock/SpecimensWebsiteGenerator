/*
 * File:    SpecimensWebsiteGenerator.java
 * Package: 
 * Author:  Zachary Gill
 */

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.Filesystem;
import common.StringUtility;

public class SpecimensWebsiteGenerator {
    
    private static final File source = new File("E:/Documents/Specimens");
    private static final File specimensSource = new File(source, "Specimens");
    private static final File referencesSource = new File(source, "References");
    private static final File vialRacksSource = new File(source, "Vial Racks");
    private static final File sink = new File("E:/Coding/HTML/Specimens");
    
    
    private static final boolean fullCopy = true;
    
    private static final Map<String, String> specimens = new LinkedHashMap<>();
    private static final TaxonomyMap taxonomyMap = new TaxonomyMap();
    static {
        taxonomyMap.nodeValue = "SPECIMENS";
    }
    
    
    public static void main(String[] args) throws Exception {
        cleanup();
        
        makeIndex();
        makeMainPage();
        makeStyle();
        makeSpecimenPages();
        makeReferences();
        makeVialRacks();
        makeTreeView();
        makeNavbar();
    }
    
    private static void cleanup() throws Exception {
        Filesystem.deleteDirectory(new File(sink, "css"));
        Filesystem.deleteDirectory(new File(sink, "images"));
        Filesystem.deleteDirectory(new File(sink, "specimens"));
        Filesystem.deleteDirectory(new File(sink, "references"));
        Filesystem.deleteDirectory(new File(sink, "vialRacks"));
        Filesystem.deleteDirectory(new File(sink, "treeview"));
        Filesystem.deleteFile(new File(sink, "index.html"));
        Filesystem.deleteFile(new File(sink, "main.html"));
        Filesystem.deleteFile(new File(sink, "navbar.html"));
    }
    
    
    private static void makeIndex() throws Exception {
        File landingPage = new File(sink, "index.html");
        Filesystem.writeLines(landingPage, wrapHtml(null, true, false, 0));
    }
    
    private static void makeMainPage() throws Exception {
        File mainPage = new File(sink, "main.html");
        List<String> content = new ArrayList<>();
        content.add("<h1>Specimens</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        
        File coverImage = new File(specimensSource, "Specimens.jpg");
        if (coverImage.exists()) {
            content.add("<center>");
            content.add("\t<img src=\"" + linkImage(coverImage, sink, -1) + "\" width=\"60%\" height=\"60%\"/>");
            content.add("</center>");
            content.add("<br>");
            content.add("");
        }
    
        content.add("<center>");
        content.add("\t<p>Zachary Gill</p>");
        content.add("</center>");
        
        Filesystem.writeLines(mainPage, wrapHtml(content, false, false, 0));
    }
    
    private static void makeStyle() throws Exception {
        File style = new File(sink, "css/style.css");
        List<String> content = new ArrayList<>();
        content.add(".navbarFrame {float:left; width:20%; height:98%; position: fixed;}");
        content.add(".mainFrame {float:left; width:79%; height:98%; margin-left: 20%; position: fixed;}");
        content.add("");
        content.add(".navbar {height: 100%; width: 100%; position: fixed; z-index: 1; top: 0; left: 0; background-color: #111; overflow-x: hidden; padding-top: 20px;}");
        content.add(".navbar a {padding: 6px 8px 6px 16px; text-decoration: none; font-size: 14px; color: #818181; display: block;}");
        content.add("");
        content.add("body {color: #f1f1f1; background-color: #111; font: normal normal normal 16px oloron, monospace;}");
        content.add("h1 {font: normal bold normal 48px monospace; text-align: center; text-decoration: underline, overline; text-transform: uppercase;}");
        content.add("");
        content.add(".td-left {text-align: right; padding-right: 8px; width: 50%;}");
        content.add(".td-right {text-align: left; padding-left: 8px; width: 50%;}");
        content.add("");
        content.add("ul, #myUL {list-style-type: none;}");
        content.add("#myUL {margin: 0; padding: 0; padding-left: 20px;}");
        content.add(".caret {cursor: pointer; user-select: none;}");
        content.add(".caret::before {content: \"\\25B6\"; color: white; display: inline-block; margin-right: 6px;}");
        content.add(".caret-down::before {transform: rotate(90deg);}");
        content.add(".nested {display: none;}");
        content.add(".active {display: block;}");
        content.add("");
        content.add("a:link {color: #818181; text-decoration: underline;}");
        content.add("a:visited {color: #818181; text-decoration: underline;}");
        content.add("a:hover {color: #f1f1f1;}");
        Filesystem.writeLines(style, content);
    }
    
    private static void makeSpecimenPages() throws Exception {
        File specimensSinkDir = new File(sink, "specimens");
        Filesystem.createDirectory(specimensSinkDir);
        
        List<File> specimenDirs = Filesystem.getDirs(specimensSource);
        for (int i = 0; i < specimenDirs.size(); i++) {
            File specimenDir = specimenDirs.get(i);
            makeSpecimenPage(specimenDir, specimensSinkDir, (i == 0), (i == (specimenDirs.size() - 1)));
        }
    }
    
    private static void makeSpecimenPage(File specimenDir, File specimensSinkDir, boolean first, boolean last) throws Exception {
        String name = specimenDir.getName();
        String id = StringUtility.trim(name.substring(0, name.indexOf('-')));
        specimens.put(id, name);
        
        File specimenSinkDir = new File(specimensSinkDir, id);
        Filesystem.createDirectory(specimenSinkDir);
        boolean finalized = false;
        
        Filesystem.writeLines(new File(specimenSinkDir, "main.html"), wrapHtml(null, true, false, 2));
        
        List<String> content = new ArrayList<>();
        content.add("<p width=\"75%\">");
        if (!first) {
            String prev = StringUtility.padZero(String.valueOf(Integer.parseInt(id) - 1), 4);
            content.add("\t<span style=\"float: left; padding-left: 8px;\">" +
                        "<a href=\"../" + prev + "/main.html\" target=\"_top\">" +
                        "&lt;&lt; Previous (" + prev + ")" +
                        "</a></span>");
        } else {
            content.add("\t<span style\"float: left;\"/>");
        }
        if (!last) {
            String next = StringUtility.padZero(String.valueOf(Integer.parseInt(id) + 1), 4);
            content.add("\t<span style=\"float: right; padding-right: 8px;\">" +
                        "<a href=\"../" + next + "/main.html\" target=\"_top\">" +
                        "(" + next + ") Next &gt;&gt;" +
                        "</a></span>");
        } else {
            content.add("\t<span style\"float: right;\"/>");
        }
        content.add("</p>");
        content.add("<br>");
        
        content.add("<h1>" + name + "</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        content.add("<center>");
        
        File idFile = new File(specimenDir, "id.txt");
        if (idFile.exists()) {
            content.add("\t<p>");
            List<String> idLines = new ArrayList<>();
            Pattern referencePattern = Pattern.compile("#(?<id>\\d{4})");
            for (String idLine : Filesystem.readLines(idFile)) {
                Matcher referenceMatcher = referencePattern.matcher(idLine);
                while (referenceMatcher.find()) {
                    idLine = idLine.replace(referenceMatcher.group(), "<a href=\"../" + referenceMatcher.group("id") + "/content.html\">" + referenceMatcher.group("id") + "</a>");
                }
                idLines.add(idLine);
                if (idLine.toUpperCase().contains("FINALIZED") || idLine.toUpperCase().contains("FINALIZATION") || idLine.toUpperCase().contains("REPLACED")) {
                    finalized = true;
                }
            }
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
            TaxonomyMap.addSpecimen(taxonomyLines, id, name);
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
                if (photoDirName.equalsIgnoreCase("FINAL")) {
                    finalized = true;
                }
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
        
        if (!finalized) {
            System.out.println("Not Finalized: " + name);
        }
        
        Filesystem.writeLines(new File(specimenSinkDir, "content.html"), wrapHtml(content, false, false, 2));
    }
    
    private static void makeReferences() throws Exception {
        File referencesSinkDir = new File(sink, "references");
        Filesystem.createDirectory(referencesSinkDir);
        Filesystem.writeLines(new File(referencesSinkDir, "main.html"), wrapHtml(null, true, false, 1));
    
        List<String> content = new ArrayList<>();
        content.add("<h1>References</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        
        for (File referencesDirectory : Filesystem.getDirs(referencesSource)) {
            content.add("<div style=\"padding-left: 10%\">");
            content.add("\t<p>" + referencesDirectory.getName() + "</p>");
            content.add("\t<ul>");
            for (File reference : Filesystem.getFiles(referencesDirectory)) {
                content.add("\t\t<li><a href=\"" + getUrlFromShortcut(reference) + "\" target=\"_blank\">" + StringUtility.rShear(reference.getName(), 4) + "</a></li>");
            }
            content.add("\t</ul>");
            content.add("</div>");
            content.add("<br>");
            content.add("");
        }
    
        content.add("<div style=\"padding-left: 10%\">");
        content.add("\t<p>Other</p>");
        content.add("\t<ul>");
        for (File reference : Filesystem.getFiles(referencesSource)) {
            content.add("\t\t<li><a href=\"" + getUrlFromShortcut(reference) + "\" target=\"_blank\">" + StringUtility.rShear(reference.getName(), 4) + "</a></li>");
        }
        content.add("\t</ul>");
        content.add("</div>");
        content.add("<br>");
        content.add("");
        
        Filesystem.writeLines(new File(referencesSinkDir, "content.html"), wrapHtml(content, false, false, 1));
    }
    
    private static void makeVialRacks() throws Exception {
        File vialRacksSinkDir = new File(sink, "vialRacks");
        Filesystem.createDirectory(vialRacksSinkDir);
        Filesystem.writeLines(new File(vialRacksSinkDir, "main.html"), wrapHtml(null, true, false, 1));
        
        File vialRackFileDir = new File(vialRacksSinkDir, "file");
        Filesystem.copyDirectory(vialRacksSource, vialRackFileDir);
        
        List<String> content = new ArrayList<>();
        content.add("<h1>Vial Racks</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        
        content.add("<br>");
        content.add("<div style=\"padding-left: 10%\">");
        for (File vialRack : Filesystem.getFiles(vialRackFileDir)) {
            content.add("\t<a href=\"" + vialRack.getAbsolutePath().replace("\\", "/").replaceAll("^.*/file/", "file/") + "\" target=\"_top\" download type=\"application/octet-stream\">" + vialRack.getName() + "</a>");
        }
        content.add("</div>");
        content.add("<br>");
        content.add("<br>");
        content.add("");
    
        for (File vialRackDirectory : Filesystem.getDirs(vialRackFileDir)) {
            content.add("<div style=\"padding-left: 10%\">");
            content.add("\t<p>" + vialRackDirectory.getName() + "</p>");
            content.add("\t<ul>");
            for (File vialRack : Filesystem.getFiles(vialRackDirectory)) {
                content.add("\t\t<li><a href=\"" + vialRack.getAbsolutePath().replace("\\", "/").replaceAll("^.*/file/", "file/") + "\"  target=\"_top\" download type=\"application/octet-stream\">" + vialRack.getName() + "</a></li>");
            }
            content.add("\t</ul>");
            content.add("</div>");
            content.add("<br>");
            content.add("");
        }
        
        Filesystem.writeLines(new File(vialRacksSinkDir, "content.html"), wrapHtml(content, false, false, 1));
    }
    
    private static void makeTreeView() throws Exception {
        File treeViewDirectory = new File(sink, "treeview");
        Filesystem.writeLines(new File(treeViewDirectory, "main.html"), wrapHtml(null, true, false, 1));
        
        TaxonomyMap.cleanMap(taxonomyMap);
        
        List<String> content = new ArrayList<>();
        content.add("<h1>Specimens Tree View</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        content.add("<ul id=\"myUL\">");
        content.addAll(makeSubTreeView(taxonomyMap, 0));
        content.add("</ul>");
        content.add("");
        content.addAll(makeToggler());
        
        Filesystem.writeLines(new File(treeViewDirectory, "content.html"), wrapHtml(content, false, false, 1));
    }
    
    private static List<String> makeSubTreeView(TaxonomyMap node, int indent) {
        String tab = StringUtility.fillStringOfLength('\t', (indent * 2) + 1);
        List<String> content = new ArrayList<>();
        
        boolean isTerminal = node.nodes.isEmpty();
        content.add(tab + "<li><span" + (isTerminal ? "" : (" class=\"caret caret-down\"")) + ">" +
                    (isTerminal ? ("<a href=\"" + "../specimens/" + node.nodeValue.substring(0, node.nodeValue.indexOf(' ')) + "/main.html\" target=\"_top\">") : "") +
                    (node.nodeKey.isEmpty() ? "" : (node.nodeKey + ": ")) + node.nodeValue +
                    (isTerminal ? "</a>" : "") +
                    "</span>");
        if (!isTerminal) {
            content.add(tab + "\t<ul class=\"nested active\">");
            for (TaxonomyMap subNode : node.nodes) {
                content.addAll(makeSubTreeView(subNode, indent + 1));
            }
            content.add(tab + "\t</ul>");
        }
        content.add(tab + "</li>");
        
        return content;
    }
    
    private static void makeNavbar() throws Exception {
        List<String> content = new ArrayList<>();
        content.add("<a href=\"index.html\" target=\"_top\" style=\"padding-left: 24px;\">HOME</a>");
        content.add("<a href=\"treeview/main.html\" target=\"_top\" style=\"padding-left: 24px;\">TREE VIEW</a>");
        
        content.add("<ul id=\"myUL\" style=\"padding: 6px 8px 6px 6px; color: #818181; font-size: 14px;\">");
        content.add("\t<li><span class=\"caret caret-down\">SPECIMENS</span>");
        content.add("\t\t<ul class=\"nested active\" style=\"padding-left: 20px;\">");
        for (Map.Entry<String, String> specimen : specimens.entrySet()) {
            content.add("\t\t\t<li><span><a href=\"specimens/" + specimen.getKey() + "/main.html\" target=\"_top\">" + specimen.getValue() + "</a></span></li>");
        }
        content.add("\t\t</ul>");
        content.add("\t</li>");
        content.add("</ul>");
    
        content.add("<a href=\"references/main.html\" target=\"_top\" style=\"padding-left: 24px;\">REFERENCES</a>");
        content.add("<a href=\"vialRacks/main.html\" target=\"_top\" style=\"padding-left: 24px;\">VIAL RACKS</a>");
        
        content.add("<br>");
        content.add("<br>");
        content.add("<br>");
        content.add("");
        content.addAll(makeToggler());
        
        Filesystem.writeLines(new File(sink, "navbar.html"), wrapHtml(content, false, true, 0));
    }
    
    private static List<String> makeToggler() {
        List<String> content = new ArrayList<>();
        content.add("<script>");
        content.add("\tvar toggler = document.getElementsByClassName(\"caret\");");
        content.add("\tvar i;");
        content.add("\tfor (i = 0; i < toggler.length; i++) {");
        content.add("\t\ttoggler[i].addEventListener(\"click\", function() {");
        content.add("\t\t\tthis.parentElement.querySelector(\".nested\").classList.toggle(\"active\");");
        content.add("\t\t\tthis.classList.toggle(\"caret-down\");");
        content.add("\t\t});");
        content.add("\t}");
        content.add("</script>");
        return content;
    }
    
    
    private static List<String> wrapHtml(List<String> content, boolean index, boolean navbar, int depth) throws Exception {
        String depthNavigation = StringUtility.repeatString("../", depth);
        List<String> wrapped = new ArrayList<>();
        wrapped.add("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        wrapped.add("\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        wrapped.add("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">");
        wrapped.add("");
        wrapped.add("\t<head>");
        wrapped.add("\t\t<title>Specimens</title>");
        wrapped.add("\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + depthNavigation + "css/style.css\"/>");
        wrapped.add("\t</head>");
        wrapped.add("");
        wrapped.add("\t<body>");
        wrapped.add("\t\t<div" + (index ? ">" : (" class=\"" + (navbar ? "navbar" : "main") + "\">")));
        if (index) {
            wrapped.add("\t\t\t<iframe class=\"navbarFrame\" src=\"" + depthNavigation + "navbar.html\"></iframe>");
            wrapped.add("\t\t\t<iframe class=\"mainFrame\" src=\"" + ((depth > 0) ? "content" : "main") + ".html\"></iframe>");
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
    
    
    private static class TaxonomyMap {
        
        private enum Taxon {
            KINGDOM,
            PHYLUM,
            CLASS,
            ORDER,
            FAMILY,
            GENUS,
            SPECIES
        }
        
        
        String nodeKey = "";
        String nodeValue = "";
        List<TaxonomyMap> nodes = new ArrayList<>();
        
        
        public static void addSpecimen(List<String> taxonomy, String id, String specimen) {
            TaxonomyMap node = taxonomyMap;
            for (String taxonomyLine : taxonomy) {
                if (taxonomyLine.toUpperCase().startsWith("NO TAXON")) {
                    continue;
                }
                String key = StringUtility.trim(taxonomyLine.substring(0, taxonomyLine.indexOf(' ')));
                String value = StringUtility.trim(taxonomyLine.substring(taxonomyLine.indexOf(' ')).replaceAll("\\(.*$", ""));
                
                boolean found = false;
                for (Taxon taxon : Taxon.values()) {
                    if (taxon.name().equalsIgnoreCase(key)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
                
                boolean hit = false;
                for (TaxonomyMap nodeEntry : node.nodes) {
                    if (nodeEntry.nodeKey.equals(key) && nodeEntry.nodeValue.equalsIgnoreCase(value)) {
                        node = nodeEntry;
                        hit = true;
                        break;
                    }
                }
                if (!hit) {
                    TaxonomyMap newNode = new TaxonomyMap();
                    newNode.nodeKey = key;
                    newNode.nodeValue = value;
                    node.nodes.add(newNode);
                    node = newNode;
                }
            }
            TaxonomyMap newNode = new TaxonomyMap();
            newNode.nodeValue = specimen;
            node.nodes.add(newNode);
        }
        
        public static void cleanMap(TaxonomyMap node) {
            node.nodes.sort(Comparator.comparing(o -> o.nodeValue));
            for (TaxonomyMap subNode : node.nodes) {
                cleanMap(subNode);
            }
        }
        
    }
    
}
