/*
 * File:    Filesystem.java
 * Package: dla.resource.access
 * Author:  Zachary Gill
 */

package common;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that wraps up apache.commons.io and java.io functionality.
 */
public final class Filesystem {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Filesystem.class);
    
    
    //Constants
    
    /**
     * The default value of the flag to enable filesystem logging or not.
     */
    public static final boolean DEFAULT_LOG_FILESYSTEM = false;
    
    
    //Functions
    
    /**
     * Attempts to create a the specified file.
     *
     * @param file The file to create.
     * @return Whether the operation was successful or not.<br>
     * Will return true if the file already exists.
     */
    public static boolean createFile(File file) {
        if (logFilesystem()) {
            logger.debug("Filesystem: Creating file: {}", file.getPath());
        }
        
        if (file.exists()) {
            return true;
        }
        
        //make missing directories before creating file
        File parent = file.getParentFile();
        if ((parent != null) && !parent.exists() && !createDirectory(parent)) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Could not create destination directory: {}", parent.getPath());
            }
            return false;
        }
        
        try {
            if (file.createNewFile() || FileUtils.waitFor(file, 1)) {
                return true;
            }
        } catch (IOException ignored) {
        }
        
        if (logFilesystem()) {
            logger.debug("Filesystem: Unable to create file: {}", file.getPath());
        }
        return false;
    }
    
    /**
     * Attempts to create the specified directory.
     *
     * @param dir The directory to create.
     * @return Whether the operation was successful or not.<br>
     * Will return true if the directory already exists.
     */
    public static boolean createDirectory(File dir) {
        if (logFilesystem()) {
            logger.debug("Filesystem: Creating directory: {}", dir.getPath());
        }
        
        if (dir.exists()) {
            return true;
        }
        
        if (dir.mkdirs() || FileUtils.waitFor(dir, 1)) {
            return true;
        }
        
        if (logFilesystem()) {
            logger.debug("Filesystem: Unable to create directory: {}", dir.getPath());
        }
        return false;
    }
    
    /**
     * Attempts to delete the specified file.
     *
     * @param file The file to delete.
     * @return Whether the operation was successful or not.
     */
    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            return deleteDirectory(file);
        }
        if (logFilesystem()) {
            logger.debug("Filesystem: Deleting file: {}", file.getPath());
        }
        
        if (!file.exists()) {
            return true;
        }
        
        if (FileUtils.deleteQuietly(file)) {
            return true;
        }
        
        if (logFilesystem()) {
            logger.debug("Filesystem: Unable to delete file: {}", file.getPath());
        }
        return false;
    }
    
    /**
     * Attempts to recursively delete the specified directory.
     *
     * @param dir The directory to delete.
     * @return Whether the operation was successful or not.
     */
    public static boolean deleteDirectory(File dir) {
        if (dir.isFile()) {
            return deleteFile(dir);
        }
        if (logFilesystem()) {
            logger.debug("Filesystem: Deleting directory: {}", dir.getPath());
        }
        
        if (!dir.exists()) {
            return true;
        }
        
        try {
            FileUtils.deleteDirectory(dir);
            return true;
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to delete directory: {}", dir.getPath());
            }
            return false;
        }
    }
    
    /**
     * Attempts to delete the specified file or directory.
     *
     * @param file The file or directory.
     * @return Whether the operation was successful or not.
     *
     * @see #deleteFile(File)
     * @see #deleteDirectory(File)
     */
    public static boolean delete(File file) {
        return file.isFile() ? deleteFile(file) : deleteDirectory(file);
    }
    
    /**
     * Attempts to rename the specified file.
     *
     * @param fileSrc  The original file.
     * @param fileDest The renamed file.
     * @return Whether the operation was successful or not.
     */
    public static boolean renameFile(File fileSrc, File fileDest) {
        if (fileSrc.isDirectory()) {
            return renameDirectory(fileSrc, fileDest);
        }
        if (logFilesystem()) {
            logger.debug("Filesystem: Renaming file: {} to: {}", fileSrc.getPath(), fileDest.getPath());
        }
        
        if (!fileSrc.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Source file does not exist: {}", fileSrc.getPath());
            }
            return false;
        }
        if (fileSrc.getAbsolutePath().equals(fileDest.getAbsolutePath())) {
            return true;
        }
        if (fileDest.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Destination file already exists: {}", fileDest.getPath());
            }
            return false;
        }
        
        if (fileSrc.renameTo(fileDest)) {
            return true;
        }
        
        if (logFilesystem()) {
            logger.debug("Filesystem: Unable to rename file: {} to: {}", fileSrc.getPath(), fileDest.getPath());
        }
        return false;
    }
    
    /**
     * Attempts to rename the specified directory.
     *
     * @param dirSrc  The original directory.
     * @param dirDest The renamed directory.
     * @return Whether the operation was successful or not.
     */
    public static boolean renameDirectory(File dirSrc, File dirDest) {
        if (dirSrc.isFile()) {
            return renameFile(dirSrc, dirDest);
        }
        if (logFilesystem()) {
            logger.debug("Filesystem: Renaming directory: {} to: {}", dirSrc.getPath(), dirDest.getPath());
        }
        
        if (!dirSrc.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Source directory does not exist: {}", dirSrc.getPath());
            }
            return false;
        }
        if (dirSrc.getAbsolutePath().equals(dirDest.getAbsolutePath())) {
            return true;
        }
        if (dirDest.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Destination directory already exists: {}", dirDest.getPath());
            }
            return false;
        }
        
        if (dirSrc.renameTo(dirDest)) {
            return true;
        }
        
        if (logFilesystem()) {
            logger.debug("Filesystem: Unable to rename directory: {} to: {}", dirSrc.getPath(), dirDest.getPath());
        }
        return false;
    }
    
    /**
     * Attempts to rename the specified file or directory.
     *
     * @param src  The original file or directory.
     * @param dest The renamed file or directory.
     * @return Whether the operation was successful or not.
     *
     * @see #renameFile(File, File)
     * @see #renameDirectory(File, File)
     */
    public static boolean rename(File src, File dest) {
        return (src.isFile() && dest.isFile()) ? renameFile(src, dest) : renameDirectory(src, dest);
    }
    
    /**
     * Attempts to copy the file fileSrc to a file fileDest.<br>
     * If fileDest is a directory, fileSrc will be copied into that directory.<br>
     * If fileDest is a file, fileSrc will be copied to that file path.
     *
     * @param fileSrc   The source file.
     * @param fileDest  The destination file or directory.
     * @param overwrite Whether or not to overwrite the destination file if it exists.
     * @return Whether the operation was successful or not.
     */
    public static boolean copyFile(File fileSrc, File fileDest, boolean overwrite) {
        if (fileSrc.isDirectory()) {
            return copyDirectory(fileSrc, fileDest, overwrite);
        }
        if (logFilesystem()) {
            logger.debug("Filesystem: Copying file: {} to: {}", fileSrc.getPath(), fileDest.getPath());
        }
        
        if (!fileSrc.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Source file does not exist: {}", fileSrc.getPath());
            }
            return false;
        }
        if (fileSrc.getAbsolutePath().equalsIgnoreCase(fileDest.getAbsolutePath())) {
            return true;
        }
        
        try {
            if (fileDest.exists() && fileDest.isDirectory()) {
                File destFile = new File(fileDest, fileSrc.getName());
                if (destFile.exists()) {
                    if (overwrite) {
                        deleteFile(destFile);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination file already exists: {}", destFile.getPath());
                        }
                        return false;
                    }
                }
                FileUtils.copyFileToDirectory(fileSrc, fileDest); //copies file into destination directory
            } else {
                if (fileDest.exists()) {
                    if (overwrite) {
                        delete(fileDest);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination file already exists: {}", fileDest.getPath());
                        }
                        return false;
                    }
                }
                FileUtils.copyFile(fileSrc, fileDest); //copies file to destination file path
            }
            return true;
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to copy file: {} to: {}", fileSrc.getPath(), fileDest.getPath());
            }
            return false;
        }
    }
    
    /**
     * Attempts to copy the file fileSrc to a file fileDest.<br>
     * If fileDest is a directory, fileSrc will be copied into that directory.<br>
     * If fileDest is a file, fileSrc will be copied to that file path.
     *
     * @param fileSrc  The source file.
     * @param fileDest The destination file or directory.
     * @return Whether the operation was successful or not.
     */
    public static boolean copyFile(File fileSrc, File fileDest) {
        return copyFile(fileSrc, fileDest, false);
    }
    
    /**
     * Attempts to copy directory dirSrc to directory dirDest.
     *
     * @param dirSrc    The source directory.
     * @param dirDest   The destination directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @param insert    If set to true, dirSrc will be copied inside dirDest.<br>
     *                  Otherwise, dirSrc will be copied to the location dirDest.
     * @return Whether the operation was successful or not.
     */
    public static boolean copyDirectory(File dirSrc, File dirDest, boolean overwrite, boolean insert) {
        if (dirSrc.isFile()) {
            return copyFile(dirSrc, dirDest, overwrite);
        }
        if (logFilesystem()) {
            logger.debug("Filesystem: Copying directory: {} to: {}", dirSrc.getPath(), dirDest.getPath());
        }
        
        if (!dirSrc.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Source directory does not exist: {}", dirSrc.getPath());
            }
            return false;
        }
        if (dirSrc.getAbsolutePath().equalsIgnoreCase(dirDest.getAbsolutePath())) {
            return true;
        }
        
        try {
            if (insert) {
                if (dirDest.isFile()) { //and if dirSrc is a directory
                    if (overwrite) {
                        deleteFile(dirDest);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination directory is a file: {}", dirDest.getPath());
                        }
                        return false;
                    }
                }
                if (!dirDest.exists()) {
                    if (!createDirectory(dirDest)) { //attempt to create destination directory if it doesn't exist
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Could not create destination directory: {}", dirDest.getPath());
                        }
                        return false;
                    }
                }
                File destDir = new File(dirDest, dirSrc.getName());
                if (destDir.exists()) {
                    if (overwrite) {
                        deleteDirectory(destDir);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination directory already exists: {}", destDir.getPath());
                        }
                        return false;
                    }
                }
                FileUtils.copyDirectoryToDirectory(dirSrc, dirDest); //copies directory within the destination directory
            } else {
                if (dirDest.exists()) {
                    if (overwrite) {
                        delete(dirDest);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination directory already exists: {}", dirDest.getPath());
                        }
                        return false;
                    }
                }
                if (!dirDest.getParentFile().exists()) {
                    if (!createDirectory(dirDest.getParentFile())) { //attempt to create destination directory if it doesn't exist
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Could not create destination directory: {}", dirDest.getParentFile().getPath());
                        }
                        return false;
                    }
                }
                FileUtils.copyDirectory(dirSrc, dirDest); //copies directory to destination directory path
            }
            return true;
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to copy directory: {} to: {}", dirSrc.getPath(), dirDest.getPath());
            }
            return false;
        }
    }
    
    /**
     * Attempts to copy directory dirSrc to directory dirDest.
     *
     * @param dirSrc    The source directory.
     * @param dirDest   The destination directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @return Whether the operation was successful or not.
     *
     * @see #copyDirectory(File, File, boolean, boolean)
     */
    public static boolean copyDirectory(File dirSrc, File dirDest, boolean overwrite) {
        return copyDirectory(dirSrc, dirDest, overwrite, false);
    }
    
    /**
     * Attempts to copy directory dirSrc to directory dirDest.
     *
     * @param dirSrc  The source directory.
     * @param dirDest The destination directory.
     * @return Whether the operation was successful or not.
     *
     * @see #copyDirectory(File, File, boolean)
     */
    public static boolean copyDirectory(File dirSrc, File dirDest) {
        return copyDirectory(dirSrc, dirDest, false);
    }
    
    /**
     * Attempts to copy src to dest.
     *
     * @param src       The path to the source file or directory.
     * @param dest      The path to the destination file or directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @return Whether the operation was successful or not.
     *
     * @see #copyFile(File, File, boolean)
     * @see #copyDirectory(File, File, boolean)
     */
    public static boolean copy(File src, File dest, boolean overwrite) {
        return src.isFile() ? copyFile(src, dest, overwrite) : copyDirectory(src, dest, overwrite);
    }
    
    /**
     * Attempts to copy src to dest.
     *
     * @param src  The path to the source file or directory.
     * @param dest The path to the destination file or directory.
     * @return Whether the operation was successful or not.
     *
     * @see #copy(File, File, boolean)
     */
    public static boolean copy(File src, File dest) {
        return copy(src, dest, false);
    }
    
    /**
     * Attempts to move file fileSrc to file fileDest.<br>
     * If fileDest is a directory, fileSrc will be moved into that directory.<br>
     * If fileDest is a file, fileSrc will be moved to that file path.
     *
     * @param fileSrc   The source file.
     * @param fileDest  The destination file.
     * @param overwrite Whether or not to overwrite the destination file if it exists.
     * @return Whether the operation was successful or not.
     */
    public static boolean moveFile(File fileSrc, File fileDest, boolean overwrite) {
        if (fileSrc.isDirectory()) {
            return moveDirectory(fileSrc, fileDest, overwrite);
        }
        if (logFilesystem()) {
            logger.debug("Filesystem: Moving file: {} to: {}", fileSrc.getPath(), fileDest.getPath());
        }
        
        if (!fileSrc.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Source file does not exist: {}", fileSrc.getName());
            }
            return false;
        }
        if (fileSrc.getAbsolutePath().equalsIgnoreCase(fileDest.getAbsolutePath())) {
            return true;
        }
        
        try {
            if (fileDest.exists() && fileDest.isDirectory()) {
                File destFile = new File(fileDest, fileSrc.getName());
                if (destFile.exists()) {
                    if (overwrite) {
                        deleteFile(destFile);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination file already exists: {}", destFile.getPath());
                        }
                        return false;
                    }
                }
                FileUtils.moveFileToDirectory(fileSrc, fileDest, true); //moves file into destination directory, creating the directory if it doesn't exist
            } else {
                if (fileDest.exists()) {
                    if (overwrite) {
                        delete(fileDest);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination file already exists: {}", fileDest.getPath());
                        }
                        return false;
                    }
                }
                FileUtils.moveFile(fileSrc, fileDest); //moves file from the source file path to the destination file path
            }
            return true;
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to move file: {} to: {}", fileSrc.getPath(), fileDest.getPath());
            }
            return false;
        }
    }
    
    /**
     * Attempts to move file fileSrc to file fileDest.<br>
     * If fileDest is a directory, fileSrc will be moved into that directory.<br>
     * If fileDest is a file, fileSrc will be moved to that file path.
     *
     * @param fileSrc  The source file.
     * @param fileDest The destination file.
     * @return Whether the operation was successful or not.
     *
     * @see #moveFile(File, File, boolean)
     */
    public static boolean moveFile(File fileSrc, File fileDest) {
        return moveFile(fileSrc, fileDest, false);
    }
    
    /**
     * Attempts to move directory dirSrc to directory dirDest.
     *
     * @param dirSrc    The source directory.
     * @param dirDest   The destination directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @param insert    If set to true, dirSrc will be moved inside dirDest.<br>
     *                  Otherwise, dirSrc will be moved to the location dirDest.
     * @return Whether the operation was successful or not.
     */
    public static boolean moveDirectory(File dirSrc, File dirDest, boolean overwrite, boolean insert) {
        if (dirSrc.isFile()) {
            return moveFile(dirSrc, dirDest, overwrite);
        }
        if (logFilesystem()) {
            logger.debug("Filesystem: Moving directory: {} to: {}", dirSrc.getPath(), dirDest.getPath());
        }
        
        if (!dirSrc.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Source directory does not exist: {}", dirSrc.getPath());
            }
            return false;
        }
        if (dirSrc.getAbsolutePath().equalsIgnoreCase(dirDest.getAbsolutePath())) {
            return true;
        }
        
        try {
            if (insert) {
                if (dirDest.isFile()) { //and if dirSrc is a directory
                    if (overwrite) {
                        deleteFile(dirDest);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination directory is a file: {}", dirDest.getPath());
                        }
                        return false;
                    }
                }
                File destDir = new File(dirDest, dirSrc.getName());
                if (destDir.exists()) {
                    if (overwrite) {
                        deleteDirectory(destDir);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination directory already exists: {}", destDir.getPath());
                        }
                        return false;
                    }
                }
                FileUtils.moveDirectoryToDirectory(dirSrc, dirDest, true); //moves directory within the destination directory
            } else {
                if (dirDest.exists()) {
                    if (overwrite) {
                        delete(dirDest);
                    } else {
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Destination directory already exists: {}", dirDest.getPath());
                        }
                        return false;
                    }
                }
                if (!dirDest.getParentFile().exists()) {
                    if (!createDirectory(dirDest.getParentFile())) { //attempt to create destination directory if it doesn't exist
                        if (logFilesystem()) {
                            logger.debug("Filesystem: Could not create destination directory: {}", dirDest.getParentFile().getPath());
                        }
                        return false;
                    }
                }
                FileUtils.moveDirectory(dirSrc, dirDest); //moves directory to destination directory path
            }
            return true;
        } catch (IOException ignore) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to move directory: {} to: {}", dirSrc.getPath(), dirDest.getPath());
            }
            return false;
        }
    }
    
    /**
     * Attempts to move directory dirSrc to directory dirDest.
     *
     * @param dirSrc    The source directory.
     * @param dirDest   The destination directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @return Whether the operation was successful or not.
     *
     * @see #moveDirectory(File, File, boolean, boolean)
     */
    public static boolean moveDirectory(File dirSrc, File dirDest, boolean overwrite) {
        return moveDirectory(dirSrc, dirDest, overwrite, false);
    }
    
    /**
     * Attempts to move directory dirSrc to directory dirDest.
     *
     * @param dirSrc  The source directory.
     * @param dirDest The destination directory.
     * @return Whether the operation was successful or not.
     *
     * @see #moveDirectory(File, File, boolean)
     */
    public static boolean moveDirectory(File dirSrc, File dirDest) {
        return moveDirectory(dirSrc, dirDest, false);
    }
    
    /**
     * Attempts to move src to dest.
     *
     * @param src       The source file or directory.
     * @param dest      The destination file or directory.
     * @param overwrite Whether or not to overwrite the destination directory if it exists.
     * @return Whether the operation was successful or not.
     *
     * @see #moveFile(File, File, boolean)
     * @see #moveDirectory(File, File, boolean)
     */
    public static boolean move(File src, File dest, boolean overwrite) {
        return src.isFile() ? moveFile(src, dest, overwrite) : moveDirectory(src, dest, overwrite);
    }
    
    /**
     * Attempts to move src to dest.
     *
     * @param src  The source file or directory.
     * @param dest The destination file or directory.
     * @return Whether the operation was successful or not.
     *
     * @see #move(File, File, boolean)
     */
    public static boolean move(File src, File dest) {
        return move(src, dest, false);
    }
    
    /**
     * Attempts to delete all the files within a directory.
     *
     * @param dir The directory to clear.
     * @return Whether the operation was successful or not.
     */
    public static boolean clearDirectory(File dir) {
        if (!dir.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Target directory does not exist: {}", dir.getPath());
            }
            return false;
        }
        
        boolean success = true;
        List<File> entries = getFilesAndDirs(dir);
        
        if (logFilesystem()) {
            logger.debug("Filesystem: Clearing directory: {}", dir.getPath());
        }
        
        for (File entry : entries) {
            success &= delete(entry);
        }
        
        return success;
    }
    
    /**
     * Returns a list of files in the specified directory that pass the specified filter.
     *
     * @param directory The directory to search for files in.
     * @param filter    The filter for files in the directory.
     * @return A list of files that were discovered.
     */
    public static List<File> listFiles(File directory, FileFilter filter) {
        if (!directory.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: The target directory is not a directory: {}", directory.getPath());
            }
            return new ArrayList<>();
        }
        
        File[] files = directory.listFiles(filter);
        
        if (files == null) {
            if (logFilesystem()) {
                logger.trace("Filesystem: Error while listing files in directory: {}", directory.getPath());
            }
            return new ArrayList<>();
        }
        
        return new ArrayList<>(Arrays.asList(files));
    }
    
    /**
     * Returns a list of files that pass the specified filter in the specified directory and in all subdirectories that pass the filter.
     *
     * @param directory       The directory to search for files in.
     * @param regexFileFilter The filter for files.
     * @param regexDirFilter  The filter for directories.
     * @return A list of files that were discovered.
     */
    public static List<File> getFilesRecursively(File directory, String regexFileFilter, String regexDirFilter) {
        if (!directory.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: The target directory is not a directory: {}", directory.getPath());
            }
            return new ArrayList<>();
        }
        
        return getFilesRecursivelyHelper(directory, regexFileFilter, regexDirFilter);
    }
    
    /**
     * Recursive helper for the getFilesRecursively() method.
     *
     * @param directory       The directory to search for files in.
     * @param regexFileFilter The filter for files.
     * @param regexDirFilter  The filter for directories.
     * @return A list of files that were discovered.
     */
    private static List<File> getFilesRecursivelyHelper(File directory, String regexFileFilter, String regexDirFilter) {
        List<File> returnList = new ArrayList<>();
        
        File[] list = directory.listFiles(file -> (file.isFile() && file.getName().matches(regexFileFilter)) || (file.isDirectory() && file.getName().matches(regexDirFilter))); //only get files and directories that match the specified filters
        
        if (list == null) {
            if (logFilesystem()) {
                logger.trace("Filesystem: Error while recursively listing files in directory: {}", directory.getPath());
            }
            return returnList;
        }
        
        for (File f : list) {
            if (f.isFile()) {
                returnList.add(f); //add file to list
            } else {
                List<File> subList = getFilesRecursively(f, regexFileFilter, regexDirFilter); //enter directory
                if (subList != null) {
                    returnList.addAll(subList);
                }
            }
        }
        
        return returnList;
    }
    
    /**
     * Returns a list of files that pass the specified filter in the specified directory and in all subdirectories.
     *
     * @param directory       The directory to search for files in.
     * @param regexFileFilter The filter for files.
     * @return A list of files that were discovered.
     *
     * @see #getFilesRecursively(File, String, String)
     */
    public static List<File> getFilesRecursively(File directory, String regexFileFilter) {
        return getFilesRecursively(directory, regexFileFilter, "^.*$");
    }
    
    /**
     * Returns a list of files in the specified directory and in all subdirectories.
     *
     * @param directory The directory to search for files in.
     * @return A list of files that were discovered.
     *
     * @see #getFilesRecursively(File, String)
     */
    public static List<File> getFilesRecursively(File directory) {
        return getFilesRecursively(directory, "^.*$");
    }
    
    /**
     * Returns a list of directories that pass the specified filter in the specified directory and in all subdirectories.
     *
     * @param directory      The directory to search for directories in.
     * @param regexDirFilter The filter for directories.
     * @return A list of directories that were discovered.
     */
    public static List<File> getDirsRecursively(File directory, String regexDirFilter) {
        if (!directory.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: The target directory is not a directory: {}", directory.getPath());
            }
            return new ArrayList<>();
        }
        
        return getDirsRecursivelyHelper(directory, regexDirFilter);
    }
    
    /**
     * Recursive helper for the getDirsRecursively() method.
     *
     * @param directory      The directory to search for directories in.
     * @param regexDirFilter The filter for directories.
     * @return A list of directories that were discovered.
     */
    private static List<File> getDirsRecursivelyHelper(File directory, String regexDirFilter) {
        List<File> returnList = new ArrayList<>();
        
        File[] list = directory.listFiles(file -> file.isDirectory() && file.getName().matches(regexDirFilter)); //only get files that are directories and match the filter
        
        if (list == null) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Error while recursively listing directories in directory: {}", directory.getPath());
            }
            return returnList;
        }
        
        for (File f : list) {
            returnList.add(f); //add directory to list
            
            List<File> subList = getDirsRecursively(f, regexDirFilter); //enter directory
            if (subList != null) {
                returnList.addAll(subList);
            }
        }
        
        return returnList;
    }
    
    /**
     * Returns a list of directories in the specified directory and in all subdirectories.
     *
     * @param directory The directory to search for directories in.
     * @return A list of directories that were discovered.
     *
     * @see #getDirsRecursively(File, String)
     */
    public static List<File> getDirsRecursively(File directory) {
        return getDirsRecursively(directory, "^.*$");
    }
    
    /**
     * Returns a list of files and directories that pass the specified filters in the specified directory and in all subdirectories.
     *
     * @param directory       The directory to search for files and directories in.
     * @param regexFileFilter The filter for files.
     * @param regexDirFilter  The filter for directories.
     * @return A list of files and directories that were discovered.
     */
    public static List<File> getFilesAndDirsRecursively(File directory, String regexFileFilter, String regexDirFilter) {
        if (!directory.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: The target directory is not a directory: {}", directory.getPath());
            }
            return new ArrayList<>();
        }
        
        return getFilesAndDirsRecursivelyHelper(directory, regexFileFilter, regexDirFilter);
    }
    
    /**
     * Recursive helper for the getFilesAndDirsRecursively() method.
     *
     * @param directory       The directory to search for files and directories in.
     * @param regexFileFilter The filter for files.
     * @param regexDirFilter  The filter for directories.
     * @return A list of files and directories that were discovered.
     */
    public static List<File> getFilesAndDirsRecursivelyHelper(File directory, String regexFileFilter, String regexDirFilter) {
        List<File> returnList = new ArrayList<>();
        
        File[] list = directory.listFiles(file -> (file.isFile() && file.getName().matches(regexFileFilter)) || (file.isDirectory() && file.getName().matches(regexDirFilter))); //only get files and directories that match the specified filters
        
        if (list == null) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Error while recursively listing files and directories in directory: {}", directory.getPath());
            }
            return new ArrayList<>();
        }
        
        for (File f : list) {
            returnList.add(f); //add file or directory to list
            
            if (f.isDirectory()) {
                List<File> subList = getFilesAndDirsRecursively(f, regexFileFilter, regexDirFilter); //enter directory
                if (subList != null) {
                    returnList.addAll(subList);
                }
            }
        }
        
        return returnList;
    }
    
    /**
     * Returns a list of files and directories that pass the specified filter in the specified directory and in all subdirectories.
     *
     * @param directory       The directory to search for files and directories in.
     * @param regexFileFilter The filter for files.
     * @return A list of files and directories that were discovered.
     *
     * @see #getFilesAndDirsRecursively(File, String, String)
     */
    public static List<File> getFilesAndDirsRecursively(File directory, String regexFileFilter) {
        return getFilesAndDirsRecursively(directory, regexFileFilter, "^.*$");
    }
    
    /**
     * Returns a list of files and directories in the specified directory and in all subdirectories.
     *
     * @param directory The directory to search for files and directories in.
     * @return A list of files and directories that were discovered.
     *
     * @see #getFilesAndDirsRecursively(File, String)
     */
    public static List<File> getFilesAndDirsRecursively(File directory) {
        return getFilesAndDirsRecursively(directory, "^.*$");
    }
    
    /**
     * Returns a list of files that pass the specified filter in the specified directory.
     *
     * @param directory   The directory to search for files in.
     * @param regexFilter The filter for files.
     * @return A list of files that were discovered.
     */
    public static List<File> getFiles(File directory, String regexFilter) {
        return listFiles(directory, file -> file.isFile() && file.getName().matches(regexFilter));
    }
    
    /**
     * Returns a list of files in the specified directory.
     *
     * @param directory The directory to search for files in.
     * @return A list of files that were discovered.
     *
     * @see #getFiles(File, String)
     */
    public static List<File> getFiles(File directory) {
        return listFiles(directory, File::isFile);
    }
    
    /**
     * Returns a list of directories that pass the specified filter in the specified directory.
     *
     * @param directory   The directory to search for files in.
     * @param regexFilter The filter for directories.
     * @return A list of directories that were discovered.
     */
    public static List<File> getDirs(File directory, String regexFilter) {
        return listFiles(directory, file -> file.isDirectory() && file.getName().matches(regexFilter));
    }
    
    /**
     * Returns a list of directories in the specified directory.
     *
     * @param directory The directory to search for files in.
     * @return A list of directories that were discovered.
     *
     * @see #getDirs(File, String)
     */
    public static List<File> getDirs(File directory) {
        return listFiles(directory, File::isDirectory);
    }
    
    /**
     * Returns a list of files and directories that pass the specified filters in the specified directory.
     *
     * @param directory       The directory to search for files and directories in.
     * @param regexFileFilter The filter for files.
     * @param regexDirFilter  The filter for directories
     * @return A list of files and directories that were discovered.
     */
    public static List<File> getFilesAndDirs(File directory, String regexFileFilter, String regexDirFilter) {
        return listFiles(directory, x -> (x.isFile() && x.getName().matches(regexFileFilter)) || (x.isDirectory() && x.getName().matches(regexDirFilter)));
    }
    
    /**
     * Returns a list of files and directories that pass the specified filters in the specified directory.
     *
     * @param directory       The directory to search for files and directories in.
     * @param regexFileFilter The filter for files.
     * @return A list of files and directories that were discovered.
     *
     * @see #getFilesAndDirs(File, String, String)
     */
    public static List<File> getFilesAndDirs(File directory, String regexFileFilter) {
        return listFiles(directory, x -> (x.isFile() && x.getName().matches(regexFileFilter)) || (x.isDirectory()));
    }
    
    /**
     * Returns a list of files and directories in the specified directory.
     *
     * @param directory The directory to search for files and directories in.
     * @return A list of files and directories that were discovered.
     *
     * @see #getFilesAndDirs(File, String)
     */
    public static List<File> getFilesAndDirs(File directory) {
        return listFiles(directory, x -> true);
    }
    
    /**
     * Determines if the content of two files is equal.
     *
     * @param a The first file.
     * @param b The second file.
     * @return True if the files are equal, false otherwise.<br>
     * Will return false if either file does not exist.
     */
    public static boolean contentEquals(File a, File b) {
        if (!a.exists() || !b.exists()) {
            if (logFilesystem()) {
                if (!a.exists()) {
                    logger.debug("Filesystem: File does not exist: {}", a.getPath());
                }
                if (!b.exists()) {
                    logger.debug("Filesystem: File does not exist: {}", b.getPath());
                }
            }
            return false;
        }
        
        try {
            return FileUtils.contentEquals(a, b);
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to compare files: {} and: {}", a.getPath(), b.getPath());
            }
            return false;
        }
    }
    
    /**
     * Calculates the size of a file.
     *
     * @param file The file to test.
     * @return The size of the file in bytes.<br>
     * Will return 0 if the file does not exist.
     */
    public static long sizeOfFile(File file) {
        if (file.isDirectory()) {
            return sizeOfDirectory(file);
        }
        if (!file.exists()) {
            return 0;
        }
        
        return FileUtils.sizeOf(file);
    }
    
    /**
     * Calculates the size of a directory recursively.
     *
     * @param dir The directory to test.
     * @return The size of the directory in bytes.<br>
     * Will return 0 if the directory does not exist.
     */
    public static long sizeOfDirectory(File dir) {
        if (dir.isFile()) {
            return sizeOfFile(dir);
        }
        if (!dir.exists()) {
            return 0;
        }
        
        return FileUtils.sizeOfDirectory(dir);
    }
    
    /**
     * Calculates the size of a file or directory.
     *
     * @param file The file or directory.
     * @return The size of the file or directory in bytes.<br>
     * Will return 0 if the file or directory does not exist.
     *
     * @see #sizeOfFile(File)
     * @see #sizeOfDirectory(File)
     */
    public static long sizeOf(File file) {
        return file.isFile() ? sizeOfFile(file) : sizeOfDirectory(file);
    }
    
    /**
     * Determines if a file is empty or not.
     *
     * @param file The file to test.
     * @return Whether the file is empty or not.<br>
     * Will return true if the file does not exist.
     */
    public static boolean fileIsEmpty(File file) {
        if (file.isDirectory()) {
            return directoryIsEmpty(file);
        }
        if (!file.exists()) {
            return true;
        }
        
        return (sizeOfFile(file) == 0);
    }
    
    /**
     * Determines if a directory is empty or not.
     *
     * @param dir The directory to test.
     * @return Whether the directory is empty or not.<br>
     * Will return true if the directory does not exist.
     */
    public static boolean directoryIsEmpty(File dir) {
        if (dir.isFile()) {
            return fileIsEmpty(dir);
        }
        if (!dir.exists()) {
            return true;
        }
        
        Path path = dir.toPath();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
            Iterator files = ds.iterator();
            return !files.hasNext();
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to determine if directory is empty: {}", dir.getPath());
            }
            return false;
        }
    }
    
    /**
     * Determines if a file or directory is empty or not.
     *
     * @param file The file or directory to test.
     * @return Whether the file or directory is empty or not.
     *
     * @see #fileIsEmpty(File)
     * @see #directoryIsEmpty(File)
     */
    public static boolean isEmpty(File file) {
        return file.isFile() ? fileIsEmpty(file) : directoryIsEmpty(file);
    }
    
    /**
     * Compares the size of two files.
     *
     * @param a The first file.
     * @param b The second file.
     * @return Less than 0 if the size of file a is less than the size of file b.<br>
     * Greater than 0 if the size of file a is greater than the size of file b.
     * 0 if the size of file a is equal to the size of file b.
     */
    public static int sizeCompare(File a, File b) {
        if (!a.exists() || !b.exists()) {
            if (logFilesystem()) {
                if (!a.exists()) {
                    logger.debug("Filesystem: File does not exist: {}", a.getPath());
                }
                if (!b.exists()) {
                    logger.debug("Filesystem: File does not exist: {}", b.getPath());
                }
            }
            return 0;
        }
        
        return Long.compare(FileUtils.sizeOf(a), FileUtils.sizeOf(b));
    }
    
    /**
     * Compares the date of two files.
     *
     * @param a The first file.
     * @param b The second file.
     * @return Less than 0 if the date of file a is older than the date of file b.<br>
     * Greater than 0 if the date of file a is newer than the date of file b.<br>
     * 0 if the date of file a is equal to the date of file b.
     */
    public static int dateCompare(File a, File b) {
        if (!a.exists() || !b.exists()) {
            if (logFilesystem()) {
                if (!a.exists()) {
                    logger.debug("Filesystem: File does not exist: {}", a.getPath());
                }
                if (!b.exists()) {
                    logger.debug("Filesystem: File does not exist: {}", b.getPath());
                }
            }
            return 0;
        }
        
        if (FileUtils.isFileOlder(a, b)) {
            return -1;
        }
        if (FileUtils.isFileNewer(a, b)) {
            return 1;
        }
        return 0;
    }
    
    /**
     * Opens an input stream for the file provided
     *
     * @param f The file to open for input.
     * @return The input stream that was opened.<br>
     * Will return null if file does not exist.
     */
    public static FileInputStream openInputStream(File f) {
        if (logFilesystem()) {
            logger.trace("Filesystem: Opening input file stream to file: {}", f.getPath());
        }
        if (!f.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: File does not exist: {}", f.getPath());
            }
            return null;
        }
        if (f.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to open input streams on directories: {}", f.getPath());
            }
            return null;
        }
        
        try {
            return FileUtils.openInputStream(f);
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to open input file stream to file: {}", f.getPath());
            }
            return null;
        }
    }
    
    /**
     * Opens an output stream for the file provided.
     *
     * @param f   The file to open for output.
     * @param app Flag to append the file or not.
     * @return The output stream that was opened.
     */
    public static FileOutputStream openOutputStream(File f, boolean app) {
        if (logFilesystem()) {
            logger.trace("Filesystem: Opening {}output file stream to file: {}", (app ? "appending " : ""), f.getPath());
        }
        if (f.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to open output streams on directories: {}", f.getPath());
            }
            return null;
        }
        
        try {
            return FileUtils.openOutputStream(f, app);
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to open output file stream to file: {}", f.getPath());
            }
            return null;
        }
    }
    
    /**
     * Opens an output stream for the file provided.
     *
     * @param f The file to open for output.
     * @return The output stream that was opened.
     *
     * @see #openOutputStream(File, boolean)
     */
    public static FileOutputStream openOutputStream(File f) {
        return openOutputStream(f, false);
    }
    
    /**
     * Reads a file out to a string.
     *
     * @param f The file to read.
     * @return The contents of the file as a string.
     */
    public static String readFileToString(File f) {
        if (logFilesystem()) {
            logger.trace("Filesystem: Reading file to string: {}", f.getPath());
        }
        if (!f.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: File does not exist: {}", f.getPath());
            }
            return "";
        }
        if (f.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to read directories to strings: {}", f.getPath());
            }
            return "";
        }
        
        try {
            return FileUtils.readFileToString(f, "UTF-8");
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to read file to string: {}", f.getPath());
            }
            return "";
        }
    }
    
    /**
     * Reads a file out to a byte array.
     *
     * @param f The file to read.
     * @return The contents of the file as a byte array.
     */
    public static byte[] readFileToByteArray(File f) {
        if (logFilesystem()) {
            logger.trace("Filesystem: Reading file to byte array: {}", f.getPath());
        }
        if (!f.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: File does not exist: {}", f.getPath());
            }
            return new byte[0];
        }
        if (f.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to read directories to byte arrays: {}", f.getPath());
            }
            return new byte[0];
        }
        
        try {
            return FileUtils.readFileToByteArray(f);
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to read file to byte array: {}", f.getPath());
            }
            return new byte[0];
        }
    }
    
    /**
     * Reads a file out to a list of lines.
     *
     * @param f The file to read.
     * @return The contents of the file as a list of strings.
     */
    public static List<String> readLines(File f) {
        if (logFilesystem()) {
            logger.trace("Filesystem: Reading lines from file: {}", f.getPath());
        }
        if (!f.exists()) {
            System.out.println("Not Found: " + f.getAbsolutePath());
            if (logFilesystem()) {
                logger.debug("Filesystem: File does not exist: {}", f.getPath());
            }
            return new ArrayList<>();
        }
        if (f.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to read lines from directories: {}", f.getPath());
            }
            return new ArrayList<>();
        }
        
        try {
            return FileUtils.readLines(f, "UTF-8");
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to read lines from file: {}", f.getPath());
            }
            return new ArrayList<>();
        }
    }
    
    /**
     * Writes a string to a file.
     *
     * @param f    The file to write to.
     * @param data The string to write to the file.
     * @param app  Flag to append the file or not.
     * @return Whether the write was successful or not.
     */
    public static boolean writeStringToFile(File f, String data, boolean app) {
        if (logFilesystem()) {
            logger.trace("Filesystem: Writing string to file: {}", f.getPath());
        }
        if (!f.exists()) {
            createFile(f);
        }
        if (f.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to write strings to directories: {}", f.getPath());
            }
            return false;
        }
        
        try {
            FileUtils.writeStringToFile(f, data, "UTF-8", app);
            return true;
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to write string to file: {}", f.getPath());
            }
            return false;
        }
    }
    
    /**
     * Writes a string to a file.
     *
     * @param f    The file to write to.
     * @param data The string to write to the file.
     * @return Whether the write was successful or not.
     *
     * @see #writeStringToFile(File, String, boolean)
     */
    public static boolean writeStringToFile(File f, String data) {
        return writeStringToFile(f, data, false);
    }
    
    /**
     * Writes a byte array to a file.
     *
     * @param f    The file to write to.
     * @param data The byte array to write to the file.
     * @param app  Flag to append the file or not.
     * @return Whether the write was successful or not.
     */
    public static boolean writeByteArrayToFile(File f, byte[] data, boolean app) {
        if (logFilesystem()) {
            logger.trace("Filesystem: Writing byte array to file: {}", f.getPath());
        }
        if (!f.exists()) {
            createFile(f);
        }
        if (f.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to write byte arrays to directories: {}", f.getPath());
            }
            return false;
        }
        
        try {
            FileUtils.writeByteArrayToFile(f, data, app);
            return true;
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to write byte array to file: {}", f.getPath());
            }
            return false;
        }
    }
    
    /**
     * Writes a byte array to a file.
     *
     * @param f    The file to write to.
     * @param data The byte array to write to the file.
     * @return Whether the write was successful or not.
     *
     * @see #writeByteArrayToFile(File, byte[], boolean)
     */
    public static boolean writeByteArrayToFile(File f, byte[] data) {
        return writeByteArrayToFile(f, data, false);
    }
    
    /**
     * Writes string lines from a collections to a file.
     *
     * @param f     The file to write to.
     * @param lines The collection of lines to be written.
     * @param app   Flag to append the file or not.
     * @return Whether the write was successful or not.
     */
    public static boolean writeLines(File f, Collection<String> lines, boolean app) {
        if (logFilesystem()) {
            logger.trace("Filesystem: Writing lines to file: {}", f.getPath());
        }
        if (!f.exists()) {
            createFile(f);
        }
        if (f.isDirectory()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to write lines to directories: {}", f.getPath());
            }
            return false;
        }
        
        try {
            FileUtils.writeLines(f, lines, app);
            return true;
        } catch (IOException ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to write lines to file: {}", f.getPath());
            }
            return false;
        }
    }
    
    /**
     * Writes string lines from a collections to a file.
     *
     * @param f     The file to write to.
     * @param lines The collection of lines to be written.
     * @return Whether the write was successful or not.
     *
     * @see #writeLines(File, Collection, boolean)
     */
    public static boolean writeLines(File f, Collection<String> lines) {
        return writeLines(f, lines, false);
    }
    
    /**
     * Returns a temporary directory.
     *
     * @return A temporary directory.
     */
    public static File getTempDirectory() {
        String path = FileUtils.getTempDirectoryPath();
        if (path != null) {
            return new File(path);
        }
        return null;
    }
    
    /**
     * Returns the current user's directory.
     *
     * @return The current user's directory.
     */
    public static File getUserDirectory() {
        String path = FileUtils.getUserDirectoryPath();
        if (path != null) {
            return new File(path);
        }
        return null;
    }
    
    /**
     * Creates a symbolic link.
     *
     * @param target The target of the symbolic link.
     * @param link   The symbolic link
     * @return Whether the operation was successful or not.
     */
    public static boolean createSymbolicLink(File target, File link) {
        if (!target.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Symbolic link target does not exist: {}", target.getPath());
            }
            return false;
        }
        if (link.exists()) {
            if (logFilesystem()) {
                logger.debug("Filesystem: File already exists: {}", link.getPath());
            }
            return false;
        }
        
        try {
            Files.createSymbolicLink(Paths.get(link.getAbsolutePath()), Paths.get(target.getAbsolutePath()));
            return true;
        } catch (Exception ignored) {
            if (logFilesystem()) {
                logger.debug("Filesystem: Unable to create symbolic link from: {} to: {}", target.getPath(), link.getPath());
            }
            return false;
        }
    }
    
    /**
     * Tests if a file is a symbolic link.
     *
     * @param f The file.
     * @return Whether the file is a symbolic link or not.
     */
    public static boolean isSymbolicLink(File f) {
        return Files.isSymbolicLink(Paths.get(f.getAbsolutePath()));
    }
    
    /**
     * Calculates the CRC32 checksum of a file.
     *
     * @param f The file.
     * @return The checksum of the specified file.
     */
    public static long checksum(File f) {
        try {
            if (f.isFile()) {
                return FileUtils.checksumCRC32(f);
            } else if (f.isDirectory()) {
                long chksum = 0;
                for (File fd : getFilesRecursively(f)) {
                    chksum += FileUtils.checksumCRC32(fd);
                    chksum %= Integer.MAX_VALUE;
                }
                return chksum;
            } else {
                return 0;
            }
        } catch (IOException ignored) {
            return 0;
        }
    }
    
    /**
     * Calculates the CRC32 checksums of the files in a directory.
     *
     * @param d The directory.
     * @return A JSON string containing the checksums of the files in the directory.
     */
    @SuppressWarnings("unchecked")
    public static String checksumDirectory(File d) {
        JSONObject json = new JSONObject();
        
        JSONArray checksums = new JSONArray();
        
        if (d.exists()) {
            for (File f : getFilesRecursively(d)) {
                String fName = StringUtility.lShear(f.getAbsolutePath(), (d.getAbsolutePath() + File.separator).length());
                if (fName.endsWith("sync")) {
                    continue;
                }
                long fChecksum = checksum(f);
                
                JSONObject checksum = new JSONObject();
                checksum.put("file", fName);
                checksum.put("checksum", fChecksum);
                checksums.add(checksum);
            }
        }
        
        json.put("checksums", checksums);
        return json.toString();
    }
    
    /**
     * Performs a comparison between a checksum store and a directory.
     *
     * @param d             The directory.
     * @param checksums     A JSON string containing a checksum store.
     * @param fileSeparator The file separator used to generate the checksums JSON string.
     * @return A JSON string specifying the modified, added, and deleted files.
     */
    @SuppressWarnings("unchecked")
    public static String compareChecksumDirectory(File d, String checksums, String fileSeparator) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject initial = (JSONObject) parser.parse(checksums);
            JSONObject target = (JSONObject) parser.parse(checksumDirectory(d));
            JSONObject compare = new JSONObject();
            
            Map<String, Long> initialChecksums = new HashMap<>();
            Map<String, Long> targetChecksums = new HashMap<>();
            for (Object initialChecksum : (Iterable) initial.get("checksums")) {
                initialChecksums.put((String) ((Map) initialChecksum).get("file"), (Long) ((Map) initialChecksum).get("checksum"));
            }
            for (Object targetChecksum : (Iterable) target.get("checksums")) {
                targetChecksums.put((String) ((Map) targetChecksum).get("file"), (Long) ((Map) targetChecksum).get("checksum"));
            }
            
            JSONArray additions = new JSONArray();
            JSONArray modifications = new JSONArray();
            JSONArray deletions = new JSONArray();
            
            //additions and modifications
            for (Entry<String, Long> entry : targetChecksums.entrySet()) {
                if (!initialChecksums.containsKey(entry.getKey().replaceAll(Matcher.quoteReplacement(File.separator), Matcher.quoteReplacement(fileSeparator)))) {
                    additions.add(entry.getKey().replaceAll(Matcher.quoteReplacement(File.separator), Matcher.quoteReplacement(fileSeparator)));
                } else if (!entry.getValue().equals(initialChecksums.get(entry.getKey().replaceAll(Matcher.quoteReplacement(File.separator), Matcher.quoteReplacement(fileSeparator))))) {
                    modifications.add(entry.getKey().replaceAll(Matcher.quoteReplacement(File.separator), Matcher.quoteReplacement(fileSeparator)));
                }
            }
            
            //deletions
            for (Entry<String, Long> entry : initialChecksums.entrySet()) {
                if (!targetChecksums.containsKey(entry.getKey().replaceAll(Matcher.quoteReplacement(fileSeparator), Matcher.quoteReplacement(File.separator)))) {
                    deletions.add(entry.getKey().replaceAll(Matcher.quoteReplacement(fileSeparator), Matcher.quoteReplacement(File.separator)));
                }
            }
            
            compare.put("additions", additions);
            compare.put("modifications", modifications);
            compare.put("deletions", deletions);
            return compare.toString();
            
        } catch (ParseException | NumberFormatException e) {
            return "";
        }
    }
    
    /**
     * Performs a comparison between a checksum store and a directory.
     *
     * @param d         The directory.
     * @param checksums A JSON string containing a checksum store.
     * @return A JSON string specifying the modified, added, and deleted files.
     */
    public static String compareChecksumDirectory(File d, String checksums) {
        return compareChecksumDirectory(d, checksums, File.separator);
    }
    
    /**
     * Generates a path from a list of directories using the proper file separators.
     *
     * @param endingSlash Whether or not to include an ending slash in the path.
     * @param paths       The list of directories of the path.
     * @return The final path string with the proper file separators.
     */
    public static String generatePath(boolean endingSlash, String... paths) {
        StringBuilder finalPath = new StringBuilder();
        for (String path : paths) {
            if ((finalPath.length() > 0) && !finalPath.toString().endsWith(File.separator)) {
                finalPath.append(File.separator);
            }
            finalPath.append(path);
        }
        if (endingSlash) {
            finalPath.append(File.separator);
        }
        return finalPath.toString();
    }
    
    /**
     * Generates a path from a list of directories using the proper file separators.
     *
     * @param paths The list of directories of the path.
     * @return The final path string with the proper file separators.
     */
    public static String generatePath(String... paths) {
        return generatePath(false, paths);
    }
    
    /**
     * Determines if filesystem logging is enabled or not.
     *
     * @return Whether filesystem logging is enabled or not.
     */
    public static boolean logFilesystem() {
        return false;
    }
    
}
