/*
 * Copyright 2018 Albert Santos.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.leeboardtools.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Some utilities for working with files.
 * @author albert
 */
public class FileUtil {
    public static final String BACKUP_SUFFIX = "_backup";
    
    /**
     * Generates a {@link File} representing the name for a backup file of a given file.
     * @param file  The file to be backed up.
     * @param makeUnique    If <code>true</code> then the returned file name will not
     * be an existing file, otherwise a standard backup file name will be used.
     * @return The backup file name.
     */
    public static File createBackupFileName(File file, boolean makeUnique) {
        FileNameParts fileNameParts = getFileNameParts(file);
        String baseName = fileNameParts.baseName + BACKUP_SUFFIX;
        file = new File(fileNameParts.directory, baseName + fileNameParts.extension);
        if (makeUnique) {
            file = makeFileNameUnique(file);
        }
        return file;
    }
    
    /**
     * Generates a {@link File} representing the name for a backup file of a given file.
     * The file name is based on the current date and is guaranteed to be unique.
     * @param file  The file to be backed up.
     * @return The backup file name.
     */
    public static File createDatedBackupFileName(File file) {
        FileNameParts fileNameParts = getFileNameParts(file);

        LocalDate now = LocalDate.now();
        String baseName = fileNameParts.baseName + now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        file = new File(fileNameParts.directory, baseName + fileNameParts.extension);
        return makeFileNameUnique(file);
    }
    
    /**
     * Makes sure a {@link File} does not exist, if it does the file name is adjusted
     * to represent a file that does not exist.
     * @param file  The file of interest.
     * @return The file argument if the file does not exist, otherwise the File that
     * is based upon the file name of file that is unique.
     */
    public static File makeFileNameUnique(File file) {
        if (!file.exists()) {
            return file;
        }
        
        FileNameParts fileParts = getFileNameParts(file);
        int index = 1;
        
        while (true) {
            String name = fileParts.baseName + "_" + index + fileParts.extension;
            file = new File(fileParts.directory, name);
            if (!file.exists()) {
                return file;
            }
            
            ++index;
        }
    }
    
    
    /**
     * Used to hold the parts of a file name.
     */
    public static class FileNameParts {
        public final String directory;
        public final String baseName;
        public final String extension;
        
        public FileNameParts(String directory, String baseName, String extension) {
            this.directory = directory;
            this.baseName = baseName;
            this.extension = extension;
        }
    };
    
    /**
     * Retrieves the file name components of a {@link File}.
     * @param file  The file of interest.
     * @return The parts, <code>null</code> if file is <code>null</code>.
     */
    public static FileNameParts getFileNameParts(File file) {
        if (file == null) {
            return null;
        }
        
        String directory = file.getParent();
        if (directory == null) {
            directory = "";
        }
        
        String baseName = file.getName();
        String extension;
        int index = file.getName().lastIndexOf('.');
        if (index >= 0) {
            extension = baseName.substring(index);
            baseName = baseName.substring(0, index);
        }
        else {
            extension = null;
        }
        
        return new FileNameParts(directory, baseName, extension);
    }
    
    public static FileNameParts getFileNameParts(Path path) {
        return getFileNameParts(path.toFile());
    }
    
    
    public static final String INVALID_FILE_NAME_CHARS = "\\/:*\"<>|.";
    public static boolean containsInvalidFileNameChars(String name) {
        final int length = name.length();
        for (int i = 0; i < length; ++i) {
            if (INVALID_FILE_NAME_CHARS.indexOf(name.codePointAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines if a string can represent a file name in both Windows, OsX and Linux.
     * @param name  The file name. This should not have a directory.
     * @return <code>true</code> if name is a valid file name under Windows, OsX, and Linux.
     */
    public static boolean isAcceptableFileName(String name) {
        File file = new File(name);
        FileNameParts fileNameParts = getFileNameParts(file);
        if ((fileNameParts.directory != null) && !fileNameParts.directory.isEmpty()) {
            return false;
        }
        
        if ((fileNameParts.baseName == null) || fileNameParts.baseName.isEmpty()) {
            return false;
        }
        if (containsInvalidFileNameChars(fileNameParts.baseName)) {
            return false;
        }
        
        if ((fileNameParts.extension != null) && !fileNameParts.extension.isEmpty()) {
            String extension = fileNameParts.extension.substring(1, fileNameParts.extension.length());
            if (containsInvalidFileNameChars(extension)) {
                return false;
            }
        }
        
        return true;
    }
    

    /**
     * Helper class that can be used to manage the renaming of an existing file as
     * a backup file once the main file has been successfully written into a temporary file.
     * The following is a use example:
     * <pre><code>
     *  boolean isSuccessful = false;
     *  File backupFile = FileUtil.createDatedBackupFileName(destFile);
     *  BackupGenerator generator = new BackupGenerator(destFile, backupFile);
     *  try {
     *      writeFile(generator.startWriteProcess());
     *      isSuccessful = true;
     *  }
     *  catch (IOException ex) {
     *      System.out.println("Uh-oh!");
     *  } finally {
     *      generator.finallyWriteProcess(isSuccessful);
     *  }
     * 
     * </code></pre>
     * 
     * The way it works is if desiredFile exists, and backupFile is not <code>null</code>
     * then a temporary file is created and returned by {@link BackupGenerator#startWriteProcess() }.
     * Then, when the file has been written successfully, {@link BackupGenerator#finallyWriteProcess(boolean) }
     * renames the original desiredFile to backupFile, replacing backupFile if it exists,
     * and then renames the temporary file returned by {@link BackupGenerator#startWriteProcess() }
     * to desiredFile.
     * If something failed and the isSuccess arg to {@link BackupGenerator#finallyWriteProcess(boolean) } is
     * false, then the temporary working file is deleted, and desiredFile and backupFile
     * are not modified.
     * <p>
     * On the other hand, if desiredFile does not exist, or backupFile is <code>null</code>
     * then {@link BackupGenerator#startWriteProcess() } just returns desiredFile, creating
     * it via {@link File#createNewFile() }..
     */
    public static class BackupGenerator {
        private final File desiredFile;
        private final File backupFile;
        private File workingFile;
        
        /**
         * Constructor.
         * @param desiredFile   The desired file, not <code>null</code>.
         * @param backupFile    The backup file, if <code>null</code> then no backup is created.
         */
        public BackupGenerator(File desiredFile, File backupFile) {
            this.desiredFile = desiredFile;
            this.backupFile = backupFile;
        }
        
        /**
         * Call to start the writing process and return a {@link File} to be written to.
         * @return  The file to be written to, it will have been created.
         * @throws IOException On errors.
         */
        public File startWriteProcess() throws IOException {
            if (desiredFile.exists() && (backupFile != null)) {
                FileNameParts fileParts = getFileNameParts(desiredFile);
                this.workingFile = File.createTempFile(fileParts.baseName, fileParts.extension, desiredFile.getParentFile());
                return this.workingFile;
            }
            
            this.desiredFile.createNewFile();
            return this.desiredFile;
        }
        
        /**
         * Call from a finally block around the startWriteProcess() call, this cleans up,
         * either performing the backup file on success renamings if necessary, or deleting
         * the working file on failure. Note that any streams that were opened on the file
         * returned by startWriteProcess() must be closed at this point.
         * @param isSuccess If <code>true</code> the writing was successful and a backup file
         * will be created if appropriate.
         * @throws IOException On errors.
         */
        public void finallyWriteProcess(boolean isSuccess) throws IOException {
            if (this.workingFile != null) {
                if (isSuccess) {
                    Files.move(this.desiredFile.toPath(), this.backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    Files.move(this.workingFile.toPath(), this.desiredFile.toPath());
                    
                    this.workingFile = null;
                }
                else {
                    this.workingFile.delete();
                    this.workingFile = null;
                }
            }
        }
    }
}
