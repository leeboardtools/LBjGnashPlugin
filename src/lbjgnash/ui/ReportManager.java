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
package lbjgnash.ui;

import com.leeboardtools.dialog.PromptDialog;
import com.leeboardtools.json.InvalidContentException;
import com.leeboardtools.json.JSONLite;
import com.leeboardtools.json.JSONObject;
import com.leeboardtools.json.JSONReader;
import com.leeboardtools.json.JSONWriter;
import com.leeboardtools.json.ParsingException;
import com.leeboardtools.util.FileUtil;
import com.leeboardtools.util.ResourceSource;
import com.leeboardtools.util.Version;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * NOTE: Do NOT change the name of this class or the package, doing so will break the report
 * definition reading since they use the class canonical name to tag and version the JSON files.
 * @author Albert Santos
 */
public class ReportManager {
    
    public static final String DOT_FILE_EXTENSION = ".lbjgnashreport";
    private static Version.Simple CURRENT_VERSION = new Version.Simple(1);
    
    public static class ReportEntry {
        final String reportName;
        final Path path;
        ReportDefinition definition;
        
        public ReportEntry(String reportName, Path path, ReportDefinition definition) {
            this.reportName = reportName;
            this.path = path;
            this.definition = definition;
        }
        public ReportEntry(String reportName, Path path) {
            this(reportName, path, null);
        }
        
        public final String getReportName() {
            return reportName;
        }
        public final Path getPath() {
            return path;
        }
        public final ReportDefinition getDefinition() {
            return definition;
        }
    }
    
    private static final ObservableMap<String, ReportEntry> allReportEntries = FXCollections.observableMap(new TreeMap<String, ReportEntry>());
    private static final ObservableMap<String, ReportEntry> publicReportEntries = FXCollections.unmodifiableObservableMap(allReportEntries);
    public static ObservableMap<String, ReportEntry> getReportEntries() {
        return publicReportEntries;
    }
    private static boolean reportsChanged;
    
    private static Path reportsPath;
    
    
    public static String pathToReportName(Path path) {
        Objects.requireNonNull(path);
        FileUtil.FileNameParts fileNameParts = FileUtil.getFileNameParts(path.getFileName());
        return fileNameParts.baseName;
    }
    
    public static Path reportNameToPath(String reportName) {
        Objects.requireNonNull(reportName);
        return new File(reportsPath.toFile(), reportName + DOT_FILE_EXTENSION).toPath();
    }
    
    public static void updateReportEntries() {
        try {
            String homeFolder = System.getProperty("user.home");
            reportsPath = FileSystems.getDefault().getPath(homeFolder, "jGnash");
            
            TreeMap<String, ReportEntry> availableReportEntries = new TreeMap<>();
            
            reportsChanged = false;
            
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(reportsPath, "*" + DOT_FILE_EXTENSION);
            dirStream.forEach((path) -> {
                String reportName = pathToReportName(path);
                ReportEntry reportEntry = allReportEntries.get(reportName);
                if (reportEntry != null) {
                    availableReportEntries.put(reportName, reportEntry);
                }
                else {
                    availableReportEntries.put(reportName, new ReportEntry(reportName, path));
                    reportsChanged = true;
                }
            });
            
            if (reportsChanged || (allReportEntries.size() != availableReportEntries.size())) {
                allReportEntries.clear();
                allReportEntries.putAll(availableReportEntries);
            }
        } catch (IOException ex) {
            Logger.getLogger(ReportManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Set<String> getAvailableReportNames() {
        updateReportEntries();
        return Collections.unmodifiableSet(allReportEntries.keySet());
    }
    
    public static ReportDefinition createReportDefinition(String reportName, ReportDefinition.Style style) {
        Objects.requireNonNull(reportName);
        
        ReportDefinition definition = ReportDefinition.fromStyle(style);
        definition.setTitle(reportName);

        ReportEntry existingEntry = allReportEntries.get(reportName);
        if (existingEntry != null) {
            existingEntry.definition = definition;
            return definition;
        }
        
        // We need to create a new definition, generate the file so it appears
        // in our report list, and the return that new definition.
        Path path = reportNameToPath(reportName);
        ReportEntry reportEntry = new ReportEntry(reportName, path, definition);
        allReportEntries.put(reportName, reportEntry);
        
        saveReport(reportName);
        
        return definition;
    }
    
    public static ReportDefinition getReportDefinition(String reportName) {
        Objects.requireNonNull(reportName);

        ReportEntry existingEntry = allReportEntries.get(reportName);
        if (existingEntry == null) {
            String message = ResourceSource.getString("ReportManager.getReportNotExist", reportName);
            reportError(message, reportName);
            return null;
        }
        
        if (existingEntry.definition == null) {
            // Try to load the report.
            try {
                FileReader reader = new FileReader(existingEntry.path.toFile());
                JSONReader jsonReader = new JSONReader(reader);
                JSONObject jsonMasterObject = jsonReader.readJSONObject();
                
                Version.Simple version = Version.simpleFromJSON(jsonMasterObject.getValue(ReportManager.class.getCanonicalName()));
                if (!version.equals(CURRENT_VERSION)) {
                    
                    String message = ResourceSource.getString("ReportManager.getReportInvalidVersion", reportName, version.toString());
                    reportError(message, reportName);
                    return null;
                }
                
                JSONObject jsonObject = jsonMasterObject.getValue("ReportDefinition").getObjectValue();
                existingEntry.definition = ReportDefinition.fromJSON(jsonObject);
                
            } catch (IOException | ParsingException | InvalidContentException ex) {
                String message = ResourceSource.getString("ReportManager.getReportFailure", reportName, ex.getLocalizedMessage());
                reportError(message, reportName);
                return null;
            }

        }
        
        return existingEntry.definition;
    }
    
    public static boolean saveReport(String reportName) {
        Objects.requireNonNull(reportName);

        ReportEntry reportEntry = allReportEntries.get(reportName);
        if (reportEntry == null) {
            String message = ResourceSource.getString("ReportManager.saveReportNotFound", reportName);
            reportError(message, reportName);
            return false;
        }
        
        if (reportEntry.definition == null) {
            // Nothing to write out...
            return true;
        }
        
        try {
            JSONObject jsonObject = ReportDefinition.toJSONObject(reportEntry.definition);

            try (FileWriter writer = new FileWriter(reportEntry.path.toFile())) {
                JSONWriter jsonWriter = new JSONWriter(writer, 4);
                
                JSONObject jsonMasterObject = JSONLite.newJSONObject();
                jsonMasterObject.add(ReportManager.class.getCanonicalName(), Version.toJSONValue(CURRENT_VERSION));
                jsonMasterObject.add("ReportDefinition", jsonObject);
                
                jsonWriter.writeJSONObject(jsonMasterObject);
            }
        } catch (IOException ex) {
            Logger.getLogger(ReportManager.class.getName()).log(Level.SEVERE, null, ex);
            String message = ResourceSource.getString("ReportManager.saveReportFailure", reportName, ex.getLocalizedMessage());
            reportError(message, reportName);
            return false;
        }
        
        updateReportEntries();
        return true;
    }
    
    public static boolean deleteReport(String reportName) {
        Objects.requireNonNull(reportName);

        ReportEntry reportEntry = allReportEntries.get(reportName);
        if (reportEntry == null) {
            String message = ResourceSource.getString("ReportManager.deleteReportNotFound", reportName);
            reportError(message, reportName);
            return false;
        }
        
        try {
            Files.delete(reportEntry.path);
        } catch (IOException ex) {
            Logger.getLogger(ReportManager.class.getName()).log(Level.SEVERE, null, ex);
            String message = ResourceSource.getString("ReportManager.deleteReportFailed", reportName, ex.getLocalizedMessage());
            reportError(message, reportName);
            return false;
        }
        
        updateReportEntries();
        return true;
    }
    
    
    protected static void reportError(String message, String reportName) {
        String title = ResourceSource.getString("ReportManager.ErrorTitle");
        PromptDialog.showOKDialog(message, title);
    }
}
