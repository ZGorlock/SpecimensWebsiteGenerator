/*
 * File:    SpecimensWebsiteGenerator.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cloudinary.utils.StringUtils;
import common.Filesystem;
import common.StringUtility;
import utility.CloudinaryUtility;
import utility.ResourceUtility;

public class SpecimensWebsiteGenerator {
    
    //Constants
    
    public static final File source = new File("E:/Documents/Specimens");
    
    public static final File specimensSource = new File(source, "Specimens");
    
    public static final File referencesSource = new File(source, "References");
    
    public static final File vialRacksSource = new File(source, "Vial Racks");
    
    public static final File sink = new File("E:/Coding/HTML/Specimens");
    
    public static final List<String> categories = Arrays.asList(
            "From Store", "From Vendor", "Preliminary Attempt",
            "Alive", "Dead", "Preparation", "Suspension", "Pre-Finalization",
            "Final", "Exhumation", "Re-Finalization");
    
    private static final boolean openLinksInternally = false;
    
    
    //Static Fields
    
    private static Map<String, String> specimens = new LinkedHashMap<>();
    
    private static List<String> favorites = new ArrayList<>();
    
    private static TaxonomyMap taxonomyMap = new TaxonomyMap();
    
    private static Map<String, String> taxonomyDescriptionMap = new HashMap<>();
    
    static {
        taxonomyMap.nodeValue = "SPECIMENS";
    }
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        ResourceUtility.loadResources();
        
        cleanup();
        makeWebsite();
        
        ResourceUtility.saveResources();
    }
    
    
    //Methods
    
    private static void cleanup() throws Exception {
        Filesystem.deleteDirectory(new File(sink, "css"));
        Filesystem.deleteDirectory(new File(sink, "references"));
        Filesystem.deleteDirectory(new File(sink, "scripts"));
        Filesystem.deleteDirectory(new File(sink, "specimens"));
        Filesystem.deleteDirectory(new File(sink, "treeview"));
        Filesystem.deleteDirectory(new File(sink, "vialRacks"));
        Filesystem.deleteFile(new File(sink, "index.html"));
        Filesystem.deleteFile(new File(sink, "main.html"));
        Filesystem.deleteFile(new File(sink, "navbar.html"));
        cleanupPhotos();
    }
    
    private static void cleanupPhotos() {
        Map<String, String> imageReferences = ResourceUtility.getImageReferences();
        List<String> deleteImages = new ArrayList<>();
        for (Map.Entry<String, String> imageReference : imageReferences.entrySet()) {
            String image = imageReference.getKey();
            File imageFile = new File(specimensSource, image);
            
            if (!imageFile.exists()) {
                deleteImages.add(image);
            }
        }
        
        for (String deleteImage : deleteImages) {
            String reference = imageReferences.get(deleteImage);
            String publicId = reference.substring(reference.lastIndexOf('/') + 1, reference.lastIndexOf('.'));
            
            try {
                CloudinaryUtility.delete(publicId);
            } catch (Exception e) {
                continue;
            }
            
            imageReferences.remove(deleteImage);
            try {
                ResourceUtility.saveResources();
            } catch (Exception e) {
                System.err.println("Failed to remove image reference: " + deleteImage + "," + deleteImage);
            }
        }
    }
    
    private static void makeWebsite() throws Exception {
        makeIndex();
        makeMainPage();
        makeStyle();
        makeTogglers();
        makeSpecimenPages();
        makeReferences();
        makeVialRacks();
        makeTreeView();
        makeNavbar();
    }
    
    
    //Website Methods
    
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
            content.add("\t<img src=\"" + ResourceUtility.linkImage(coverImage, sink, -1) + "\" width=\"70%\" height=\"70%\" style=\"max-height: 80vh; object-fit: contain;\"/>");
            content.add("</center>");
            content.add("<br>");
            content.add("");
        }
        
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
        content.add(".img_div {z-index:9999; display:none; background-color:#000; position:fixed; height:100%; width:100%; left: 0px; top: 0px; text-align: center; overflow: hidden;}");
        content.add(".img_div_img {width: 100%; height: 100%; object-fit: contain;}");
        content.add(".img_div_close {position: absolute; right: 0px; top: 0px; background: #000; color: #fff; cursor: pointer; width: 32px; height: 32px; text-align: center; line-height: 32px;}");
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
    
    private static void makeTogglers() throws Exception {
        makeNavbarToggler();
        makeTreeViewToggler();
    }
    
    private static void makeNavbarToggler() throws Exception {
        File scriptsDir = new File(sink, "scripts");
        Filesystem.createDirectory(scriptsDir);
        
        List<String> content = new ArrayList<>();
        content.add("$(document).ready(function() {");
        content.add("\tvar toggler = document.getElementsByClassName(\"caret\");");
        content.add("");
        
        content.add("\tvar i;");
        content.add("\tfor (i = 0; i < toggler.length; i++) {");
        content.add("");
        
        content.add("\t\ttoggler[i].setAttribute('id', i);");
        content.add("\t\tif (sessionStorage.getItem('navbarCollapsed' + i) === \"true\") {");
        content.add("\t\t\ttoggler[i].parentElement.querySelector(\".nested\").classList.toggle(\"active\");");
        content.add("\t\t\ttoggler[i].classList.toggle(\"caret-down\");");
        content.add("\t\t}");
        content.add("");
        
        content.add("\t\ttoggler[i].addEventListener(\"click\", function() {");
        content.add("\t\t\tthis.parentElement.querySelector(\".nested\").classList.toggle(\"active\");");
        content.add("\t\t\tthis.classList.toggle(\"caret-down\");");
        content.add("");
        
        content.add("\t\t\tif (sessionStorage.getItem('navbarCollapsed' + this.getAttribute('id')) == \"true\") {");
        content.add("\t\t\t\tsessionStorage.setItem('navbarCollapsed' + this.getAttribute('id'), false);");
        content.add("\t\t\t} else {");
        content.add("\t\t\t\tsessionStorage.setItem('navbarCollapsed' + this.getAttribute('id'), true);");
        content.add("\t\t\t}");
        content.add("");
        
        content.add("\t\t});");
        content.add("\t}");
        content.add("});");
        
        Filesystem.writeLines(new File(scriptsDir, "navbarToggler.js"), content);
    }
    
    private static void makeTreeViewToggler() throws Exception {
        File scriptsDir = new File(sink, "scripts");
        Filesystem.createDirectory(scriptsDir);
        
        List<String> content = new ArrayList<>();
        content.add("$(document).ready(function() {");
        content.add("\tvar toggler = document.getElementsByClassName(\"caret\");");
        content.add("");
        
        content.add("\tvar i;");
        content.add("\tfor (i = 0; i < toggler.length; i++) {");
        content.add("");
        
        content.add("\t\ttoggler[i].setAttribute('id', i);");
        content.add("\t\tif (sessionStorage.getItem('treeViewCollapsed' + i) === \"true\") {");
        content.add("\t\t\ttoggler[i].parentElement.querySelector(\".nested\").classList.toggle(\"active\");");
        content.add("\t\t\ttoggler[i].classList.toggle(\"caret-down\");");
        content.add("\t\t}");
        content.add("");
        
        content.add("\t\ttoggler[i].addEventListener(\"click\", function() {");
        content.add("\t\t\tthis.parentElement.querySelector(\".nested\").classList.toggle(\"active\");");
        content.add("\t\t\tthis.classList.toggle(\"caret-down\");");
        content.add("");
        
        content.add("\t\t\tif (sessionStorage.getItem('treeViewCollapsed' + this.getAttribute('id')) == \"true\") {");
        content.add("\t\t\t\tsessionStorage.setItem('treeViewCollapsed' + this.getAttribute('id'), false);");
        content.add("\t\t\t} else {");
        content.add("\t\t\t\tsessionStorage.setItem('treeViewCollapsed' + this.getAttribute('id'), true);");
        content.add("\t\t\t}");
        content.add("");
        
        content.add("\t\t});");
        content.add("\t}");
        content.add("});");
        
        Filesystem.writeLines(new File(scriptsDir, "treeViewToggler.js"), content);
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
                    "<a href=\"../" + prev + "/content.html\" target=\"mainFrame\">" +
                    "&lt;&lt; Previous (" + prev + ")" +
                    "</a></span>");
        } else {
            content.add("\t<span style\"float: left;\"/>");
        }
        if (!last) {
            String next = StringUtility.padZero(String.valueOf(Integer.parseInt(id) + 1), 4);
            content.add("\t<span style=\"float: right; padding-right: 8px;\">" +
                    "<a href=\"../" + next + "/content.html\" target=\"mainFrame\">" +
                    "(" + next + ") Next &gt;&gt;" +
                    "</a></span>");
        } else {
            content.add("\t<span style\"float: right;\"/>");
        }
        content.add("</p>");
        content.add("<br>");
        content.add("");
        
        content.add("<h1>" + name + "</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        content.add("<center>");
        
        File idFile = new File(specimenDir, "id.txt");
        if (idFile.exists()) {
            content.add("\t<p><b>");
            List<String> idLines = new ArrayList<>();
            Pattern referencePattern = Pattern.compile("#(?<id>\\d{4})");
            for (String idLine : Filesystem.readLines(idFile)) {
                Matcher referenceMatcher = referencePattern.matcher(idLine);
                while (referenceMatcher.find()) {
                    idLine = idLine.replace(referenceMatcher.group(), "<a href=\"../" + referenceMatcher.group("id") + "/content.html\" target=\"mainFrame\">" + referenceMatcher.group("id") + "</a>");
                }
                idLines.add(idLine);
                if (idLine.toUpperCase().contains("LOST BEFORE") || idLine.toUpperCase().contains("DESTROYED BEFORE") ||
                        idLine.toUpperCase().contains("LOST DURING") || idLine.toUpperCase().contains("DESTROYED DURING") ||
                        idLine.toUpperCase().contains("LOST AFTER") || idLine.toUpperCase().contains("DESTROYED AFTER") ||
                        idLine.toUpperCase().contains("REPLACED WITH")) {
                    finalized = true;
                }
            }
            for (int i = 0; i < idLines.size(); i++) {
                content.add("\t\t" + idLines.get(i) + ((i < idLines.size() - 1) ? "<br>" : ""));
            }
            content.add("\t</b></p>");
            content.add("");
            content.add("\t<br>");
            content.add("\t<hr>");
            content.add("\t<br>");
            content.add("");
        }
        
        File bugGuide = new File(specimenDir, "BugGuide Submission.url");
        if (bugGuide.exists()) {
            String bugGuideUrl = ResourceUtility.getUrlFromShortcut(bugGuide);
            content.add("\t<p>");
            content.add("\t\t<a href=\"" + bugGuideUrl + "\" target=\"" + (openLinksInternally ? "mainFrame" : "#") + "\">BugGuide Submission</a>");
            content.add("\t</p>");
            content.add("");
        }
        String thisPageUrl = "https://zgorlock.github.io/Specimens/specimens/" + StringUtility.padZero(id, 4) + "/main.html";
        content.add("\t<p>");
        content.add("\t\t<a href=\"" + thisPageUrl + "\" target=\"" + (openLinksInternally ? "mainFrame" : "#") + "\">Link to This Page</a>");
        content.add("\t</p>");
        content.add("");
        content.add("\t<br>");
        content.add("\t<hr>");
        content.add("\t<br>");
        content.add("");
        
        File taxonomy = new File(specimenDir, "taxonomy.txt");
        if (taxonomy.exists()) {
            content.add("\t<p>");
            content.add("\t<table>");
            List<String> taxonomyLines = Filesystem.readLines(taxonomy);
            TaxonomyMap.addSpecimen(taxonomyLines, id, name);
            for (String taxonomyLine : taxonomyLines) {
                String key = StringUtility.trim(taxonomyLine.substring(0, taxonomyLine.indexOf(' ')));
                String value = StringUtility.trim(taxonomyLine.substring(taxonomyLine.indexOf(' ')).replaceAll("\\(.*$", ""));
                if (taxonomyLine.toUpperCase().startsWith("NO TAXON") || value.isEmpty()) {
                    System.err.println("Taxonomy Invalid: " + id);
                }
                if (!key.equals("Species") && !key.equals("Subspecies")) {
                    String description = StringUtility.trim(taxonomyLine.replace(key + " " + value, ""));
                    if (!taxonomyDescriptionMap.containsKey(value)) {
                        taxonomyDescriptionMap.put(value, description);
                    } else {
                        if (!taxonomyDescriptionMap.get(value).equals(description)) {
                            System.err.println("Taxonomy description for: " + value + " " + description + " does not match " + taxonomyDescriptionMap.get(value));
                        }
                    }
                }
                content.add("\t\t<tr>");
                content.add("\t\t\t<td class=\"td-left\"><b>" + key + "</b></td>");
                content.add("\t\t\t<td class=\"td-right\">" + value + "</td>");
                content.add("\t\t</tr>");
            }
            content.add("\t</table>");
            content.add("\t</p>");
            content.add("");
            content.add("\t<br>");
            content.add("\t<hr>");
            content.add("\t<br>");
            content.add("");
        }
        
        File favorite = new File(specimenDir, "favorite.txt");
        if (favorite.exists()) {
            favorites.add(id);
        }
        
        File photosDir = new File(specimenDir, "Photos");
        List<String> images = new ArrayList<>();
        if (photosDir.exists()) {
            List<File> photoSubDirs = Filesystem.getDirs(photosDir);
            int photoSubDirIndex = 0;
            for (File photoSubDir : photoSubDirs) {
                if (!photoSubDir.getName().matches("\\d\\s-\\s.+")) {
                    System.err.println("Photo directory: " + photoSubDir.getName() + " is invalid for: " + id);
                }
                
                String photoDirName = photoSubDir.getName().replaceAll("\\d\\s-\\s", "");
                String photoDirIndex = photoSubDir.getName().replaceAll("\\s-\\s[a-zA-Z\\-\\s]+", "");
                if (!categories.contains(photoDirName) || !String.valueOf(photoSubDirIndex).equals(photoDirIndex)) {
                    System.err.println("Photo directory name: " + photoDirName + " is invalid for: " + id);
                }
                
                if (photoDirName.equalsIgnoreCase("FINAL")) {
                    finalized = true;
                }
                content.add("\t<p>" + photoDirName + "</p>");
                content.add("\t<br>");
                
                List<File> photoList = Filesystem.getFiles(photoSubDir);
                int index = 0;
                for (File photo : photoList) {
                    String image = ResourceUtility.linkImage(photo, specimenSinkDir, photoSubDirIndex);
                    String imageId = "img_" + photoDirName.toLowerCase().replace(" ", "_") + index;
                    if (photo.getName().toLowerCase().endsWith("mp4")) {
                        content.add("\t<a id=\"" + imageId + "\" href=\"#\" target=\"_blank\"><video width=\"720\" height=\"480\" controls><source src=\"" + image + "\" type=\"video/mp4\"></video></a><br>");
                        content.add("\t<div id=\"div_" + imageId + "\" class=\"img_div\">");
                        content.add("\t\t<video class=\"img_div_img\" controls><source src=\"" + image + "\" type=\"video/mp4\"></video>");
                        content.add("\t\t<div id=\"div_close_" + imageId + "\" class=\"img_div_close\">X</div>");
                        content.add("\t</div>");
                    } else {
                        content.add("\t<a id=\"" + imageId + "\" href=\"#\" target=\"_blank\"><img src=\"" + image + "\" width=\"50%\" height=\"50%\" style=\"max-height: 100vh; object-fit: contain;\"/></a><br>");
                        content.add("\t<div id=\"div_" + imageId + "\" class=\"img_div\">");
                        content.add("\t\t<img class=\"img_div_img\" src=\"" + image + "\"/>");
                        content.add("\t\t<div id=\"div_close_" + imageId + "\" class=\"img_div_close\">X</div>");
                        content.add("\t</div>");
                    }
                    images.add(imageId);
                    index++;
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
        
        makeSpecimenImagePopupScript(specimenSinkDir, images);
        
        content.add("\t<p width=\"75%\">");
        if (!first) {
            String prev = StringUtility.padZero(String.valueOf(Integer.parseInt(id) - 1), 4);
            content.add("\t\t<span style=\"float: left; padding-left: 8px;\">" +
                    "<a href=\"../" + prev + "/content.html\" target=\"mainFrame\">" +
                    "&lt;&lt; Previous (" + prev + ")" +
                    "</a></span>");
        } else {
            content.add("\t\t<span style\"float: left;\"/>");
        }
        if (!last) {
            String next = StringUtility.padZero(String.valueOf(Integer.parseInt(id) + 1), 4);
            content.add("\t\t<span style=\"float: right; padding-right: 8px;\">" +
                    "<a href=\"../" + next + "/content.html\" target=\"mainFrame\">" +
                    "(" + next + ") Next &gt;&gt;" +
                    "</a></span>");
        } else {
            content.add("\t\t<span style\"float: right;\"/>");
        }
        content.add("\t</p>");
        content.add("\t<br>");
        content.add("");
        
        File reference = new File(specimenDir, "Reference");
        if (reference.exists()) {
            List<File> references = Filesystem.getFiles(reference);
            Map<String, String> referenceMap = new LinkedHashMap<>();
            for (File referenceEntry : references) {
                if (referenceEntry.getName().toUpperCase().contains("BUGGUIDE")) {
                    referenceMap.putIfAbsent(referenceEntry.getName(), ResourceUtility.getUrlFromShortcut(referenceEntry));
                }
            }
            for (File referenceEntry : references) {
                if (!referenceEntry.getName().toUpperCase().contains("BUGGUIDE") && !referenceEntry.getName().toUpperCase().contains("WIKIPEDIA")) {
                    referenceMap.putIfAbsent(referenceEntry.getName(), ResourceUtility.getUrlFromShortcut(referenceEntry));
                }
            }
            for (File referenceEntry : references) {
                if (referenceEntry.getName().toUpperCase().contains("WIKIPEDIA")) {
                    referenceMap.putIfAbsent(referenceEntry.getName(), ResourceUtility.getUrlFromShortcut(referenceEntry));
                }
            }
            
            content.add("\t<p>References</p>");
            for (Map.Entry<String, String> referenceEntry : referenceMap.entrySet()) {
                content.add("\t<a href=\"" + referenceEntry.getValue() + "\" target=\"" + (openLinksInternally ? "mainFrame" : "#") + "\">" + StringUtility.rShear(referenceEntry.getKey(), 4) + "</a><br>");
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
        
        Filesystem.writeLines(new File(specimenSinkDir, "content.html"), wrapHtml(content, false, false, 2, "scripts/imagePopup.js"));
    }
    
    private static void makeSpecimenImagePopupScript(File specimenSinkDir, List<String> images) {
        File scriptsDir = new File(specimenSinkDir, "scripts");
        Filesystem.createDirectory(scriptsDir);
        
        List<String> content = new ArrayList<>();
        content.add("$(document).ready(function() {");
        content.add("\tvar scroll = 0;");
        content.add("");
        content.add("\t$(document).on('scroll', 'html, body', function() {");
        content.add("\t\tscroll = $(window).scrollTop();");
        content.add("\t});");
        content.add("");
        content.add("\tfunction prepareShow() {");
        content.add("\t\tscroll = $(window).scrollTop();");
        content.add("\t\t$('html, body').css({");
        content.add("\t\t\toverflow: 'hidden',");
        content.add("\t\t\theight: '100%'");
        content.add("\t\t});");
        content.add("\t}");
        content.add("\tfunction prepareHide() {");
        content.add("\t\t$('html, body').css({");
        content.add("\t\t\toverflow: 'auto',");
        content.add("\t\t\theight: 'auto'");
        content.add("\t\t});");
        content.add("\t\t$(\"html\").scrollTop(scroll);");
        content.add("\t}");
        content.add("");
        
        for (String image : images) {
            content.add("\t$('#" + image + "').click( function(e) {");
            content.add("\t\te.preventDefault();");
            content.add("\t\tprepareShow();");
            content.add("\t\t$(\"#div_" + image + "\").show();");
            content.add("\t});");
            content.add("\t$('#div_" + image + "').click(function(){");
            content.add("\t\tprepareHide();");
            content.add("\t\t$(\"#div_" + image + "\").hide();");
            content.add("\t});");
            content.add("\t$('#div_close_" + image + "').click(function(){");
            content.add("\t\tprepareHide();");
            content.add("\t\t$(\"#div_" + image + "\").hide();");
            content.add("\t});");
            content.add("");
        }
        
        content.add("\t$(document).keyup(function(e) {");
        content.add("\t\tif (e.key === \"Escape\") {");
        content.add("\t\t\tprepareHide();");
        for (String image : images) {
            content.add("\t\t\t$(\"#div_" + image + "\").hide();");
        }
        content.add("\t\t}");
        content.add("\t});");
        content.add("");
        content.add("});");
        
        Filesystem.writeLines(new File(scriptsDir, "imagePopup.js"), content);
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
                content.add("\t\t<li><a href=\"" + ResourceUtility.getUrlFromShortcut(reference) + "\" target=\"_blank\">" + StringUtility.rShear(reference.getName(), 4) + "</a></li>");
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
            content.add("\t\t<li><a href=\"" + ResourceUtility.getUrlFromShortcut(reference) + "\" target=\"_blank\">" + StringUtility.rShear(reference.getName(), 4) + "</a></li>");
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
        
        List<String> content = new ArrayList<>();
        content.add("<h1>Vial Racks</h1>");
        content.add("<hr>");
        content.add("<br>");
        content.add("");
        
        content.add("<br>");
        content.add("<div style=\"padding-left: 10%\">");
        for (File vialRack : Filesystem.getFiles(vialRacksSource)) {
            String remoteLocation = ResourceUtility.linkVialRack(vialRack);
            if (remoteLocation == null) {
                continue;
            }
            content.add("\t<a href=\"https://docs.google.com/uc?export=download&id=" + remoteLocation + "\" target=\"mainFrame\" download type=\"application/octet-stream\">" + vialRack.getName() + "</a><br>");
        }
        content.add("</div>");
        content.add("<br>");
        content.add("<br>");
        content.add("");
        
        List<File> vialRackDirectories = Filesystem.getDirs(vialRacksSource);
        vialRackDirectories.sort((o1, o2) -> {
            Double o1Number = null;
            try {
                o1Number = Double.parseDouble(o1.getName().substring(0, o1.getName().indexOf(' ')));
            } catch (Exception ignored) {
            }
            
            Double o2Number = null;
            try {
                o2Number = Double.parseDouble(o2.getName().substring(0, o2.getName().indexOf(' ')));
            } catch (Exception ignored) {
            }
            
            if (o1Number == null) {
                if (o2Number == null) {
                    return o1.getName().compareTo(o2.getName());
                } else {
                    return 1;
                }
            } else {
                if (o2Number == null) {
                    return -1;
                } else {
                    return o1Number.compareTo(o2Number);
                }
            }
        });
        
        for (File vialRackDirectory : vialRackDirectories) {
            content.add("<div style=\"padding-left: 10%\">");
            content.add("\t<p>" + vialRackDirectory.getName() + "</p>");
            content.add("\t<ul>");
            for (File vialRack : Filesystem.getFiles(vialRackDirectory)) {
                String remoteLocation = ResourceUtility.linkVialRack(vialRack);
                if (remoteLocation == null) {
                    continue;
                }
                content.add("\t<a href=\"https://docs.google.com/uc?export=download&id=" + remoteLocation + "\" target=\"mainFrame\" download type=\"application/octet-stream\">" + vialRack.getName() + "</a><br>");
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
        
        Filesystem.writeLines(new File(treeViewDirectory, "content.html"), wrapHtml(content, false, false, 1, "../scripts/treeViewToggler.js"));
    }
    
    private static List<String> makeSubTreeView(TaxonomyMap node, int indent) {
        String tab = StringUtility.fillStringOfLength('\t', (indent * 2) + 1);
        List<String> content = new ArrayList<>();
        
        boolean isTerminal = node.nodes.isEmpty();
        content.add(tab + "<li><span" + (isTerminal ? "" : (" class=\"caret caret-down\"")) + ">" +
                (isTerminal ? ("<a href=\"" + "../specimens/" + node.nodeValue.substring(0, node.nodeValue.indexOf(' ')) + "/content.html\" target=\"mainFrame\">") : "") +
                (node.nodeKey.isEmpty() ? "" : (node.nodeKey + ": ")) + node.nodeValue +
                (!StringUtils.isEmpty(taxonomyDescriptionMap.get(node.nodeValue)) ? (" - " + taxonomyDescriptionMap.get(node.nodeValue)) : "") +
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
        content.add("<a href=\"main.html\" target=\"mainFrame\" style=\"padding-left: 24px;\">HOME</a>");
        content.add("<a href=\"treeview/content.html\" target=\"mainFrame\" style=\"padding-left: 24px;\">TREE VIEW</a>");
        
        content.add("<ul id=\"myUL\" style=\"padding: 6px 8px 6px 6px; color: #818181; font-size: 14px;\">");
        content.add("\t<li><span class=\"caret caret-down\">SPECIMENS</span>");
        content.add("\t\t<ul class=\"nested active\" style=\"padding-left: 20px;\">");
        
        List<String> specimenKeys = new ArrayList<>(specimens.keySet());
        Collections.reverse(specimenKeys);
        for (String specimenKey : specimenKeys) {
            content.add("\t\t\t<li><span><a href=\"specimens/" + specimenKey + "/content.html\" target=\"mainFrame\">" + specimens.get(specimenKey) + "</a></span></li>");
        }
        content.add("\t\t</ul>");
        content.add("\t</li>");
        content.add("</ul>");
        
        content.add("<ul id=\"myUL\" style=\"padding: 6px 8px 6px 6px; color: #818181; font-size: 14px;\">");
        content.add("\t<li><span class=\"caret caret-down\">FAVORITES</span>");
        content.add("\t\t<ul class=\"nested active\" style=\"padding-left: 20px;\">");
        
        List<String> favoriteKeys = new ArrayList<>(favorites);
        Collections.reverse(favoriteKeys);
        for (String favoriteKey : favoriteKeys) {
            content.add("\t\t\t<li><span><a href=\"specimens/" + favoriteKey + "/content.html\" target=\"mainFrame\">" + specimens.get(favoriteKey) + "</a></span></li>");
        }
        content.add("\t\t</ul>");
        content.add("\t</li>");
        content.add("</ul>");
        
        content.add("<a href=\"references/content.html\" target=\"mainFrame\" style=\"padding-left: 24px;\">REFERENCES</a>");
        content.add("<a href=\"vialRacks/content.html\" target=\"mainFrame\" style=\"padding-left: 24px;\">VIAL RACKS</a>");
        
        content.add("<br>");
        content.add("<br>");
        content.add("<br>");
        content.add("");
        
        Filesystem.writeLines(new File(sink, "navbar.html"), wrapHtml(content, false, true, 0, "scripts/navbarToggler.js"));
    }
    
    private static List<String> wrapHtml(List<String> content, boolean index, boolean navbar, int depth, String... scripts) throws Exception {
        String depthNavigation = StringUtility.repeatString("../", depth);
        List<String> wrapped = new ArrayList<>();
        wrapped.add("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        wrapped.add("\t\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        wrapped.add("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\">");
        wrapped.add("");
        wrapped.add("\t<head>");
        wrapped.add("\t\t<title>Specimens</title>");
        wrapped.add("\t\t<link rel=\"stylesheet\" type=\"text/css\" href=\"" + depthNavigation + "css/style.css\"/>");
        if (scripts.length > 0) {
            wrapped.add("\t\t<script src=\"https://code.jquery.com/jquery-2.1.1.min.js\"></script>");
            for (String script : scripts) {
                wrapped.add("\t\t<script src=\"" + script + "\" type=\"text/javascript\"></script>");
            }
        }
        wrapped.add("\t</head>");
        wrapped.add("");
        wrapped.add("\t<body>");
        wrapped.add("\t\t<div" + (index ? ">" : (" class=\"" + (navbar ? "navbar" : "main") + "\">")));
        if (index) {
            wrapped.add("\t\t\t<iframe class=\"navbarFrame\" src=\"" + depthNavigation + "navbar.html\"></iframe>");
            wrapped.add("\t\t\t<iframe class=\"mainFrame\" name=\"mainFrame\" src=\"" + ((depth > 0) ? "content" : "main") + ".html\"></iframe>");
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
    
    
    //Inner Classes
    
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
