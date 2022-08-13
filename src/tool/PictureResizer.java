/*
 * File:    PictureResizer.java
 * Package: tool
 * Author:  Zachary Gill
 */

package tool;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import commons.access.Filesystem;

public class PictureResizer {
    
    //Constants
    
    private static final boolean preserveMetadata = false; //when inactive, file dates will be preserved if preserveDates is enabled but 'Date Taken' will not
    
    private static final boolean preserveDates = true; //when active, file dates will be preserved but 'Date Taken' will not
    
    private static final boolean saveBackup = true;
    
    private static final boolean limitDimensions = true; //aspect ratio will be preserved
    
    private static final int maxDimension = 3072;
    
    private static final boolean crop = false;
    
    private static final int cropOffTop = 31;
    
    private static final int cropOffLeft = 0;
    
    private static final int cropOffRight = 0;
    
    private static final int cropOffBottom = 0;
    
    private static final List<String> pictureTypes = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff");
    
    private static final File dataDir = new File("data");
    
    private static final File tmpDir = new File("tmp");
    
    private static final File directory = new File("C:/Users/Zack/Desktop/New folder");
    
    private static final File backupDir = new File(directory, "original");
    
    
    //Main Method
    
    public static void main(String[] args) throws Exception {
        processPictures(getPictures(directory));
    }
    
    
    //Methods
    
    private static List<File> getPictures(File directory) throws Exception {
        return Filesystem.getFilesRecursively(directory != null ? directory : dataDir).stream()
                .filter(e -> !e.getAbsolutePath().matches(".*[\\\\/]" + backupDir.getName() + "[\\\\/].*"))
                .filter(e -> pictureTypes.contains(Filesystem.getFileType(e).toLowerCase()))
                .collect(Collectors.toList());
    }
    
    private static void processPictures(List<File> pictures) throws Exception {
        prepareDirectories();
        
        pictures.forEach(picture -> {
            System.out.println("Processing: " + picture.getAbsolutePath());
            try {
                processPicture(picture);
            } catch (Exception e) {
                System.err.println("Failed to process: " + picture.getAbsolutePath());
                e.printStackTrace(System.err);
            }
        });
    }
    
    private static void prepareDirectories() throws Exception {
        if (Stream.of(tmpDir, dataDir).anyMatch(dir ->
                (dir.getAbsolutePath().matches("[A-Z]:[\\\\/]" + directory.getName()) || (!dir.exists() && !Filesystem.createDirectory(dir))))) {
            System.err.println("Failed to create directories");
            throw new Exception();
        }
        
        if (saveBackup) {
            if (!backupDir.exists()) {
                if (!Filesystem.createDirectory(backupDir)) {
                    System.err.println("Failed to create backup directory: " + backupDir.getAbsolutePath());
                    throw new Exception();
                }
            } else {
                final File oldBackupDir = new File(backupDir.getParentFile(), "old");
                final File saveOldBackupDir = new File(backupDir, backupDir.getName());
                Filesystem.rename(backupDir, oldBackupDir);
                Filesystem.moveDirectory(oldBackupDir, saveOldBackupDir);
            }
        }
    }
    
    private static void processPicture(File picture) throws Exception {
        final String type = Filesystem.getFileType(picture).toLowerCase();
        final File tmp = new File(tmpDir, picture.getName().replaceAll("(?<=\\.)[^.]+$", type));
        final File output = new File(picture.getParentFile(), tmp.getName());
        
        if (saveBackup) {
            backupImage(picture);
        }
        
        if (preserveMetadata) {
            processPicturePreserveMetadata(picture, tmp);
        } else {
            processPictureLoseMetadata(picture, tmp);
        }
        
        replaceImage(picture, tmp, output);
    }
    
    private static void processPicturePreserveMetadata(File source, File target) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(source);
             FileOutputStream fileOutputStream = new FileOutputStream(target)) {
            
            final ImageInputStream imageInputStream = ImageIO.createImageInputStream(fileInputStream);
            final ImageReader reader = ImageIO.getImageReaders(imageInputStream).next();
            reader.setInput(imageInputStream, true);
            
            final IIOMetadata metadata = reader.getImageMetadata(0);
            final IIOMetadata streamMetadata = reader.getStreamMetadata();
            
            final BufferedImage originalImage = reader.read(0);
            imageInputStream.flush();
            
            final BufferedImage image = preProcessImage(originalImage);
            
            final ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fileOutputStream);
            final ImageWriter writer = ImageIO.getImageWriter(reader);
            writer.setOutput(imageOutputStream);
            
            final ImageWriteParam writeParams = writer.getDefaultWriteParam();
            writeParams.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
            
            writer.write(streamMetadata, new IIOImage(image, null, metadata), writeParams);
            writer.dispose();
            imageOutputStream.flush();
        }
    }
    
    private static void processPictureLoseMetadata(File source, File target) throws Exception {
        final BufferedImage originalImage = ImageIO.read(source);
        
        final BufferedImage image = preProcessImage(originalImage);
        ImageIO.write(image, Filesystem.getFileType(source).toLowerCase(), target);
    }
    
    private static BufferedImage preProcessImage(BufferedImage image) {
        if (crop) {
            image = cropImage(image);
        }
        if (limitDimensions) {
            image = scaleImage(image, maxDimension, maxDimension);
        }
        return image;
    }
    
    private static BufferedImage cropImage(BufferedImage image, Rectangle rect) {
        final BufferedImage cropped = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), image.getType());
        final Graphics g = cropped.getGraphics();
        g.drawImage(image, 0, 0, (int) rect.getWidth(), (int) rect.getHeight(), (int) rect.getX(), (int) rect.getY(), (int) (rect.getX() + rect.getWidth()), (int) (rect.getY() + rect.getHeight()), null);
        g.dispose();
        return cropped;
    }
    
    private static BufferedImage cropImage(BufferedImage image) {
        final Rectangle rect = new Rectangle(cropOffLeft, cropOffTop,
                (image.getWidth() - cropOffLeft - cropOffRight), (image.getHeight() - cropOffTop - cropOffBottom));
        return cropImage(image, rect);
    }
    
    public static BufferedImage scaleImage(BufferedImage image, double scale) {
        if ((scale <= 0.0) || (scale >= 1.0)) {
            return image;
        }
        
        final AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        final AffineTransformOp transformOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BICUBIC);
        
        final BufferedImage scaled = new BufferedImage((int) (image.getWidth() * scale), (int) (image.getHeight() * scale), image.getType());
        return transformOp.filter(image, scaled);
    }
    
    public static BufferedImage scaleImage(BufferedImage image, int maxWidth, int maxHeight) {
        if ((image.getWidth() <= maxWidth) && (image.getHeight() <= maxHeight)) {
            return image;
        }
        
        double scale = Math.min(((double) maxWidth / image.getWidth()), ((double) maxHeight / image.getHeight()));
        return scaleImage(image, scale);
    }
    
    private static void backupImage(File picture) throws Exception {
        final File backup = new File(backupDir, picture.getName());
        if (!Filesystem.copyFile(picture, backup)) {
            System.err.println("Failed to create backup: " + backup.getAbsolutePath());
            throw new Exception();
        }
    }
    
    private static void replaceImage(File source, File tmp, File target) throws Exception {
        if (preserveDates) {
            final Map<String, FileTime> dates = Filesystem.readDates(source);
            Filesystem.writeDates(tmp, dates);
        }
        
        Filesystem.move(tmp, target, true);
    }
    
}
