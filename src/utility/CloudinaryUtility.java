/*
 * File:    CloudinaryUtility.java
 * Package: utility
 * Author:  Zachary Gill
 */

package utility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.apache.commons.io.FileUtils;

public class CloudinaryUtility {
    
    //Static Fields
    
    private static List<String> API_KEY = new ArrayList<>();
    
    static {
        try {
            API_KEY.addAll(FileUtils.readLines(new File("apiKey"), "UTF-8"));
            if (API_KEY.size() != 3) {
                throw new Exception();
            }
        } catch (Exception e) {
            System.out.println("Must supply Cloudinary API keys in /apiKey:\n<cloud_name>\n<api_key>\n<api_secret>");
            System.exit(0);
        }
    }
    
    private static Cloudinary cloudinary;
    
    private static AtomicBoolean initialized = new AtomicBoolean(false);
    
    
    //Functions
    
    public static void initialize() {
        if (initialized.compareAndSet(false, true)) {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", API_KEY.get(0),
                    "api_key", API_KEY.get(1),
                    "api_secret", API_KEY.get(2)));
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static String upload(File file, String folder) throws Exception {
        boolean isVideo = file.getName().toLowerCase().endsWith(".mp4");
        
        String url;
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("type", "upload");
            options.put("access_mode", "public");
            options.put("access_type", "anonymous");
            options.put("resource_type", isVideo ? "video" : "image");
            options.put("folder", folder);
            options.put("tags", folder);
            
            Map upload = cloudinary.uploader().upload(file, options);
            url = (String) upload.get("url");
            
            System.out.println("Uploaded: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Error uploading: " + file.getAbsolutePath());
            throw e;
        }
        return url;
    }
    
    public static String upload(File file) throws Exception {
        return upload(file, "");
    }
    
    public static void delete(String publicId) throws Exception {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Deleted: " + publicId);
        } catch (Exception e) {
            System.err.println("Error deleting: " + publicId);
            throw e;
        }
    }
    
}
