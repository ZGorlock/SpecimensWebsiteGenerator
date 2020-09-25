/*
 * File:    VerifyPhotosExist.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.io.File;
import java.util.Map;

import main.SpecimensWebsiteGenerator;
import utility.ResourceUtility;

public class VerifyPhotosExist {
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        ResourceUtility.loadResources();
        Map<String, String> imageReferences = ResourceUtility.getImageReferences();
        
        for (String imageReference : imageReferences.keySet()) {
            File image = new File(SpecimensWebsiteGenerator.specimensSource, imageReference);
            if (!image.exists()) {
                System.err.println("File: " + imageReference + " does not exist");
            }
        }
    }
    
}
