/*
 * File:    DeleteAllPhotosFromCloud.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.util.ArrayList;
import java.util.Map;

import utility.CloudinaryUtility;
import utility.ResourceUtility;

public class DeleteAllPhotosFromCloud {
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        ResourceUtility.loadResources();
        Map<String, String> imageReferences = ResourceUtility.getImageReferences();
        
        for (String imageFile : new ArrayList<>(imageReferences.keySet())) {
            String imageUrl = imageReferences.get(imageFile);
            
            String publicId = imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.lastIndexOf('.'));
            try {
                CloudinaryUtility.delete(publicId);
            } catch (Exception e) {
                continue;
            }
            
            imageReferences.remove(imageFile);
            try {
                ResourceUtility.saveResources();
            } catch (Exception e) {
                System.err.println("Failed to remove image reference: " + imageFile + "," + imageUrl);
            }
        }
    }
    
}
