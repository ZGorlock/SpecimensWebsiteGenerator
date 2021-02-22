/*
 * File:    PictureResizer.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import common.Filesystem;
import common.StringUtility;

public class PictureResizer {
    
    //Constants
    
    private static final boolean preserveMetadata = false;
    
    private static final boolean preserveDates = true;
    
    private static final boolean saveBackup = true;
    
    private static final boolean limitDimensions = false; //only active if preserveMetadata is false
    
    private static final int maxDimension = 3072;
    
    
    //Static Fields
    
    private static File directory = new File("C:/Users/Zack/Desktop/New folder");
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        processPictures(getPictures(directory));
    }
    
    
    //Methods
    
    private static List<File> getPictures(File directory) throws Exception {
        List<File> pictures;
        if (directory != null) {
            List<File> files = Filesystem.getFilesRecursively(directory);
            pictures = new ArrayList<>();
            for (File f : files) {
                if (f.getName().toLowerCase().endsWith(".jpg") ||
                        f.getName().toLowerCase().endsWith(".png")) {
                    pictures.add(f);
                }
            }
        } else {
            pictures = Filesystem.getFiles(new File("data"));
        }
        return pictures;
    }
    
    private static void processPictures(List<File> pictures) throws Exception {
        Filesystem.createDirectory(new File("tmp"));
        for (File picture : pictures) {
            System.out.println("Processing: " + picture.getAbsolutePath());
            try {
                processPicture(picture);
            } catch (Exception e) {
                System.err.println("Failed to process: " + picture.getAbsolutePath());
                e.printStackTrace(System.err);
            }
        }
    }
    
    private static void processPicture(File picture) throws Exception {
        String type = StringUtility.rSnip(picture.getName().toLowerCase(), 3);
        File tmp = new File("tmp", picture.getName()
                .replaceAll("(\\.[jJ][pP][gG])+", ".jpg")
                .replaceAll("(\\.[pP][nN][gG])+", ".png"));
        File output = new File(picture.getParentFile(), tmp.getName());
        
        if (saveBackup) {
            backupImage(picture);
        }
        
        if (preserveMetadata) {
            processPicturePreserveMetadata(picture, tmp, type);
        } else {
            processPictureLoseMetadata(picture, tmp, type);
        }
        
        replaceImage(picture, tmp, output);
    }
    
    private static void processPicturePreserveMetadata(File source, File target, String type) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(source);
             FileOutputStream fileOutputStream = new FileOutputStream(target)) {
            
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(fileInputStream);
            ImageReader reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream);
            IIOMetadata metadata = reader.getImageMetadata(0);
            BufferedImage data = reader.read(0);
            imageInputStream.flush();
            
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fileOutputStream);
            ImageWriter writer = ImageIO.getImageWriter(reader);
            writer.setOutput(imageOutputStream);
            ImageWriteParam params = writer.getDefaultWriteParam();
            writer.write(null, new IIOImage(data, null, null), params);
            writer.dispose();
            ImageIO.write(data, type, imageOutputStream);
            imageOutputStream.flush();
        }
    }
    
    private static void processPictureLoseMetadata(File source, File target, String type) throws Exception {
        BufferedImage image = ImageIO.read(source);
        
        if (limitDimensions) {
            int dim = Math.max(image.getWidth(), image.getHeight());
            if (dim > maxDimension) {
                double scale = (double) maxDimension / dim;
                AffineTransform transform = new AffineTransform();
                transform.scale(scale, scale);
                AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
                BufferedImage scaled = new BufferedImage((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), image.getType());
                image = transformOp.filter(image, scaled);
            }
        }
        
        Image newImage = image.getScaledInstance(image.getWidth(), image.getHeight(), Image.SCALE_DEFAULT);
        image.getGraphics().drawImage(newImage, 0, 0, null);
        ImageIO.write(image, type, target);
    }
    
    private static void backupImage(File picture) throws Exception {
        File backupDir = new File(picture.getParentFile(), "original");
        if (!backupDir.exists() && !Filesystem.createDirectory(backupDir)) {
            System.err.println("Failed to create backup directory: " + backupDir.getAbsolutePath());
            throw new Exception();
        }
        File backup = new File(backupDir, picture.getName());
        if (!Filesystem.copyFile(picture, backup)) {
            System.err.println("Failed to create backup: " + backup.getAbsolutePath());
            throw new Exception();
        }
    }
    
    private static void replaceImage(File source, File tmp, File target) throws Exception {
        Map<String, FileTime> dates = readDates(source);
        Filesystem.move(tmp, target, true);
        writeDates(target, dates);
    }
    
    private static Map<String, FileTime> readDates(File image) throws Exception {
        Map<String, FileTime> dates = new HashMap<>();
        if (!preserveDates) {
            return dates;
        }
        
        List<String> attributes = Arrays.asList("lastModifiedTime", "lastAccessTime", "creationTime");
        for (String attribute : attributes) {
            dates.put(attribute, (FileTime) Files.getAttribute(image.toPath(), attribute));
        }
        return dates;
    }
    
    private static void writeDates(File image, Map<String, FileTime> dates) throws Exception {
        if (!preserveDates) {
            return;
        }
        
        List<String> attributes = Arrays.asList("lastModifiedTime", "lastAccessTime", "creationTime");
        for (String attribute : attributes) {
            FileTime date = dates.get(attribute);
            if (date == null) {
                continue;
            }
            Files.setAttribute(image.toPath(), attribute, date);
        }
    }
    
}
