/*
 * File:    ResourceUtility.java
 * Package: utility
 * Author:  Zachary Gill
 */

package utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.access.Filesystem;
import commons.string.StringUtility;

public class ResourceUtility {
    
    //Constants
    
    private static final File resources = new File("resources");
    
    
    //Static Fields
    
    private static Map<String, String> imageReferences = new LinkedHashMap<>();
    
    private static Map<String, String> vialRackReferences = new LinkedHashMap<>();
    
    private static boolean fullImageCopy = false;
    
    
    //Getters
    
    public static Map<String, String> getImageReferences() {
        return imageReferences;
    }
    
    public static Map<String, String> getVialRackReferences() {
        return vialRackReferences;
    }
    
    
    //Functions
    
    public static void loadResources() throws Exception {
        CloudinaryUtility.initialize();
        
        File vialRackReferencesFile = new File(resources, "vialRackReferences.csv");
        if (!vialRackReferencesFile.exists()) {
            throw new FileNotFoundException("Could not find Vial Rack References resource");
        }
        for (String vialRackReference : Filesystem.readLines(vialRackReferencesFile)) {
            String[] vialRackReferenceParts = vialRackReference.split(",");
            vialRackReferences.put(vialRackReferenceParts[0], vialRackReferenceParts[1]);
        }
        
        File imageReferencesFile = new File(resources, "imageReferences.csv");
        if (!vialRackReferencesFile.exists()) {
            throw new FileNotFoundException("Could not find Image References resource");
        }
        for (String imageReference : Filesystem.readLines(imageReferencesFile)) {
            String[] imageReferenceParts = imageReference.split(",");
            imageReferences.put(imageReferenceParts[0], imageReferenceParts[1]);
        }
    }
    
    public static void saveResources() throws Exception {
        List<String> vialRackReferencesData = new ArrayList<>();
        vialRackReferences.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> vialRackReferencesData.add(e.getKey() + "," + e.getValue()));
        File vialRackReferencesFile = new File(resources, "vialRackReferences.csv");
        Filesystem.writeLines(vialRackReferencesFile, vialRackReferencesData);
        
        List<String> imageReferencesData = new ArrayList<>();
        imageReferences.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> imageReferencesData.add(e.getKey() + "," + e.getValue()));
        File imageReferencesFile = new File(resources, "imageReferences.csv");
        Filesystem.writeLines(imageReferencesFile, imageReferencesData);
    }
    
    public static String linkImage(File source, File destDir, int index) throws Exception {
        if (fullImageCopy) {
            return linkImageFullCopy(source, destDir, index);
        } else {
            return linkImageReference(source);
        }
    }
    
    private static String linkImageFullCopy(File source, File destDir, int index) throws Exception {
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
        Filesystem.copyFile(source, link);
        return "images/" + ((index >= 0) ? (index + "/") : "") + link.getName();
    }
    
    private static String linkImageReference(File source) throws Exception {
        boolean isVideo = source.getName().toLowerCase().endsWith(".mp4");
        String imageKey = source.getAbsolutePath().replace("\\", "/").replaceAll("^.*/Specimens/", "");
        if (imageReferences.containsKey(imageKey)) {
            return "http://res.cloudinary.com/specimens/" + (isVideo ? "video" : "image") + "/upload/" + imageReferences.get(imageKey);
        }
        
        String folder = imageKey.substring(0, 4);
        folder = folder.matches("\\d{4}") ? folder : "";
        
        String url = CloudinaryUtility.upload(source, folder);
        imageReferences.put(imageKey, url.replace("http://res.cloudinary.com/specimens/" + (isVideo ? "video" : "image") + "/upload/", ""));
        saveResources();
        
        return url;
    }
    
    public static String linkVialRack(File vialRack) {
        return vialRackReferences.get(vialRack.getAbsolutePath().replace("\\", "/").replaceAll("^.*/Vial Racks/", ""));
    }
    
    public static String getUrlFromShortcut(File shortcut) throws Exception {
        String content = StringUtility.removeWhiteSpace(Filesystem.readFileToString(shortcut));
        Pattern getUrlPattern = Pattern.compile("^.*URL=(?<url>.+)$");
        Matcher getUrlMatcher = getUrlPattern.matcher(content);
        if (getUrlMatcher.matches()) {
            return getUrlMatcher.group("url");
        }
        return "";
    }
    
}
