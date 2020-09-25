/*
 * File:    CheckForDuplicatePhotoNames.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utility.ResourceUtility;

public class CheckForDuplicatePhotoNames {
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        ResourceUtility.loadResources();
        Map<String, String> imageReferences = ResourceUtility.getImageReferences();
        
        Map<String, List<String>> photoNameMap = new HashMap<>();
        for (String imageReference : imageReferences.keySet()) {
            String photoName = imageReference.substring(
                    imageReference.contains("/") ? (imageReference.lastIndexOf('/') + 1) : 0);
            photoNameMap.putIfAbsent(photoName, new ArrayList<>());
            photoNameMap.get(photoName).add(imageReference);
        }
        
        for (Map.Entry<String, List<String>> photoNameMapEntry : photoNameMap.entrySet()) {
            if (photoNameMapEntry.getValue().size() > 1) {
                for (String photo : photoNameMapEntry.getValue()) {
                    System.out.println(photo);
                }
                System.out.println();
            }
        }
        
    }
    
}
