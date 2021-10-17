/*
 * File:    CropAllImagesFromCategoryHelper.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import commons.access.Filesystem;
import main.SpecimensWebsiteGenerator;
import utility.CloudinaryUtility;
import utility.ResourceUtility;

public class CropAllImagesFromCategoryHelper {
    
    //Constants
    
    private static final File work = new File("work");
    
    
    //Static Fields
    
    private static int stage = 0; //0 for preparation, 1 for re-upload
    
    private static List<String> categories = Arrays.asList(
            "Preparation", "Suspension", "Pre-Finalization",
            "Final", "Re-Finalization");
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        ResourceUtility.loadResources();
        
        if (stage == 0) {
            prepForCropping();
        } else if (stage == 1) {
            reupload();
        }
    }
    
    
    //Functions
    
    private static void prepForCropping() {
        Map<String, String> imageReferences = ResourceUtility.getImageReferences();
        
        Filesystem.clearDirectory(work);
        for (String category : categories) {
            for (String imageReference : imageReferences.keySet()) {
                if (imageReference.contains("- " + category + "/")) {
                    File source = new File(SpecimensWebsiteGenerator.specimensSource, imageReference);
                    File copy = new File(work, source.getName());
                    boolean isVideo = source.getName().toLowerCase().endsWith(".mp4");
                    if (!isVideo) {
                        Filesystem.copyFile(source, copy);
                    }
                }
            }
        }
        
        System.out.println("Photos from the specified categories have been copied to /work");
        System.out.println("Crop these photos, maintaining the file names");
        System.out.println("Then run stage 2");
    }
    
    private static void reupload() {
        Map<String, String> imageReferences = ResourceUtility.getImageReferences();
        
        List<File> photos = Filesystem.getFiles(work);
        for (File photo : photos) {
            boolean isVideo = photo.getName().toLowerCase().endsWith(".mp4");
            
            String original = null;
            for (String imageReference : imageReferences.keySet()) {
                if (imageReference.endsWith("/" + photo.getName())) {
                    original = imageReference;
                    break;
                }
            }
            if (original == null) {
                continue;
            }
            File originalFile = new File(SpecimensWebsiteGenerator.specimensSource, original);
            String originalReference = imageReferences.get(original);
            String publicId = originalReference.substring(originalReference.lastIndexOf('/') + 1, originalReference.lastIndexOf('.'));
            
            String url;
            try {
                url = CloudinaryUtility.upload(photo);
            } catch (Exception e) {
                continue;
            }
            url = url.replace("http://res.cloudinary.com/specimens/" + (isVideo ? "video" : "image") + "/upload/", "");
            
            try {
                CloudinaryUtility.delete(publicId);
            } catch (Exception e) {
                continue;
            }
            
            Filesystem.moveFile(photo, originalFile, true);
            imageReferences.replace(original, url);
            try {
                ResourceUtility.saveResources();
            } catch (Exception e) {
                System.err.println("Failed to save image reference: " + original + "," + url);
            }
        }
        Filesystem.deleteDirectory(work);
    }
    
}
