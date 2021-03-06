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

import com.leeboardtools.util.CSVUtil;
import com.leeboardtools.util.ResourceSource;
import lbjgnash.ui.reportview.ReportDataView;
import com.leeboardtools.util.StringUtil;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jgnash.engine.Engine;

/**
 * TODO Add message channel listeners.
 * TODO Add a menu for setup, updating.
 * @author Albert Santos
 */
public class ReportView {
    private static List<ReportViewEntry> openReportViewEntries = new ArrayList<>();
    
    protected ReportViewEntry reportViewEntry;
    
    protected String reportLabel;
    protected Stage stage;
    protected ReportDefinition definition;
    protected Engine engine;
    
    protected Scene mainScene;
    protected VBox mainVBox;
    protected Label titleLabel;
    protected ReportDataView reportDataView;
    
    
    public static final String CLASS_TITLE_LABEL = "report-title-label";
    public static final String CLASS_TITLE_LABEL_CONTAINER = "report-title-label-container";
    
    
    ReportView() {
        stage = new Stage();
        stage.setOnCloseRequest((event) -> {
            close();
        });
        
    }
    
    public void requestFocus() {
        stage.requestFocus();
    }

    public void show() {
        stage.show();
    }
    
    public void close() {
        takeDownEngineListeners();
        stage.close();
        removeOpenReportView(this);
    }
    
    protected void setupReportView(String label, ReportDefinition definition, Engine engine, Stage primaryStage) {
        this.reportLabel = label;
        this.definition = definition;
        this.engine = engine;
        
        //this.stage.initOwner(primaryStage);
        this.stage.setTitle(label);
        
        this.mainVBox = new VBox();
        this.mainScene = new Scene(this.mainVBox);
        this.mainScene.getStylesheets().add("lbjgnash/ui/Styles.css");
        
        this.stage.setScene(this.mainScene);
        
        this.mainVBox.setPrefSize(1000, 600);
        
        setupEngineListeners();
        
        setupReportHeader();
        setupReportArea();
        
        refreshFromDefinition();
    }
    
    protected void setupEngineListeners() {
        
    }
    
    protected void takeDownEngineListeners() {
        if (this.reportDataView != null) {
            this.reportDataView.shutDownView();
        }
    }
    
    protected void setupReportHeader() {
        if (this.titleLabel == null) {
            BorderPane borderPaneHeader = new BorderPane();
            
            HBox hBoxTitle = new HBox();
            hBoxTitle.setAlignment(Pos.CENTER);
            hBoxTitle.getStyleClass().add(CLASS_TITLE_LABEL_CONTAINER);
            
            this.titleLabel = new Label();
            this.titleLabel.getStyleClass().add(CLASS_TITLE_LABEL);
            hBoxTitle.getChildren().add(this.titleLabel);
            
            borderPaneHeader.setCenter(hBoxTitle);
            
            
            HBox hBoxMenuButton = new HBox();
            hBoxMenuButton.setAlignment(Pos.CENTER_RIGHT);
            hBoxMenuButton.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            
            MenuButton menuButton = new MenuButton();
            menuButton.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            menuButton.setText(ResourceSource.getString("ReportView.Menu.Options"));
            
            setupReportMenu(menuButton);
            
            hBoxMenuButton.getChildren().add(menuButton);
            borderPaneHeader.setRight(hBoxMenuButton);
            
            this.mainVBox.getChildren().add(borderPaneHeader);
        }
    }
    
    protected void setupReportMenu(MenuButton menuButton) {
        MenuItem configureItem = new MenuItem(ResourceSource.getString("ReportView.MenuItem.Configure"));
        menuButton.getItems().add(configureItem);
        configureItem.setOnAction((event) -> {
            onConfigureReport();
        });

        MenuItem saveSetupItem = new MenuItem(ResourceSource.getString("ReportView.MenuItem.SaveSetup"));
        menuButton.getItems().add(saveSetupItem);        
        saveSetupItem.setOnAction((event) -> {
            onSaveReportSetup();
        });

        MenuItem exportItem = new MenuItem(ResourceSource.getString("ReportView.MenuItem.Export"));
        menuButton.getItems().add(exportItem);        
        exportItem.setOnAction((event) -> {
            onExportReport();
        });

        MenuItem printItem = new MenuItem(ResourceSource.getString("ReportView.MenuItem.Print"));
        menuButton.getItems().add(printItem);        
        printItem.setOnAction((event) -> {
            onPrintReport();
        });
        printItem.setDisable(true);
        
        
    }
    
    protected void setupReportArea() {
        if (this.reportDataView == null) {
            this.reportDataView = createReportTableView();
            Control control = this.reportDataView.getControl();
            control.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            
            VBox.setVgrow(control, Priority.ALWAYS);
            this.mainVBox.getChildren().add(control);
        }
        
        this.reportDataView.setupView(definition, engine);
    }
    
    protected ReportDataView createReportTableView() {
        return new ReportDataView();
    }
    
    protected void refreshFromDefinition() {
        refreshReportHeader();
        
        this.reportDataView.refreshFromReportDefinition();
    }
    
    protected void refreshReportHeader() {
        if (this.titleLabel != null) {
            String title = this.definition.getTitle();
            if (!StringUtil.isNonEmpty(title)) {
                title = this.reportLabel;
            }
            this.titleLabel.setText(title);
        }
        
        // TODO: Add date range.
    }
    
    protected void refreshReportArea() {
        this.reportDataView.refreshFromEngine();
    }
    
    
    protected void onConfigureReport() {
        if (ReportSetupView.showAndWait(reportLabel, definition, engine, stage)) {
            refreshFromDefinition();
        }
    }
    
    protected void onSaveReportSetup() {
        String reportName = ReportManagerView.promptNewReportName(stage, reportLabel);
        if (StringUtil.isNonEmpty(reportName)) {
            ReportDefinition newDefinition = ReportManager.createReportDefinition(reportName, definition.getStyle());
            newDefinition.copyFrom(definition);
            ReportManager.saveReport(reportName);
            
            if (reportViewEntry != null) {
                reportViewEntry.reportLabel = reportName;
            }
            
            reportLabel = reportName;
            stage.setTitle(reportLabel);
        }
    }
    
    
    protected static File exportInitialDirectory = null;
    
    protected void onExportReport() {
        if (exportInitialDirectory == null) {
            String homeFolder = System.getProperty("user.home");
            Path exportDirectory = FileSystems.getDefault().getPath(homeFolder, "jGnash");
            exportInitialDirectory = exportDirectory.toFile();
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(ResourceSource.getString("ReportView.ExportFileChooser.Title"));
        fileChooser.setInitialDirectory(exportInitialDirectory);
        fileChooser.setInitialFileName(reportLabel + CSVUtil.CSV_EXTENSION);
        
        ExtensionFilter csvFilter = new ExtensionFilter(ResourceSource.getString("ReportView.ExportFileChooser.CSVFiles"), CSVUtil.CSV_WILDCARD_EXTENSION);
        fileChooser.getExtensionFilters().add(csvFilter);
        
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile != null) {
            exportInitialDirectory = selectedFile.getParentFile();
            this.reportDataView.exportCSVFile(selectedFile);
        }
    }
    
    protected void onPrintReport() {
    }
    
    
    //
    // This is all report view manage stuff below...
    //
    public static class ReportViewEntry {
        private String reportLabel;
        private final ReportView reportView;
        
        public ReportViewEntry(String reportLabel, ReportView reportView) {
            this.reportLabel = reportLabel;
            this.reportView = reportView;
        }
        
        public final String getReportLabel() {
            return reportLabel;
        }
        public final ReportView getReportView() {
            return reportView;
        }
    }
    
    public static List<ReportViewEntry> getOpenReports() {
        return Collections.unmodifiableList(openReportViewEntries);
    }
    
    public static ReportViewEntry getOpenReportEntryWithLabel(String label) {
        for (ReportViewEntry entry : openReportViewEntries) {
            if (entry.getReportLabel().equals(label)) {
                return entry;
            }
        }
        return null;
    }
    
    static void removeOpenReportView(ReportView reportView) {
        for (ReportViewEntry entry : openReportViewEntries) {
            if (entry.getReportView() == reportView) {
                openReportViewEntries.remove(entry);
                entry.reportView.reportViewEntry = null;
                return;
            }
        }
    }
    

    public static ReportView openReportView(String reportName, Engine engine, Stage primaryStage) {
        ReportDefinition definition = ReportManager.getReportDefinition(reportName);
        ReportViewEntry entry = generateReportViewEntry(reportName, definition, engine, primaryStage);

        ReportView reportView = entry.getReportView();
        reportView.show();
        reportView.requestFocus();
        return reportView;
    }
    
    public static ReportView openReportView(ReportDefinition.Style style, Engine engine, Stage primaryStage) {
        ReportDefinition definition = ReportDefinition.fromStyle(style);
        ReportViewEntry entry = generateReportViewEntry(null, definition, engine, primaryStage);

        ReportView reportView = entry.getReportView();
        reportView.show();
        reportView.requestFocus();
        return reportView;
    }
    
    static String generateReportLabel(String reportName, ReportDefinition definition) {
        String label;
        if (StringUtil.isNonEmpty(reportName)) {
            label = reportName;
        }
        else {
            label = definition.getTitle();
            if (!StringUtil.isNonEmpty(label)) {
                label = definition.getStyle().toString();
            }
        }
        
        return StringUtil.getUniqueString(label, (s) -> { 
            return getOpenReportEntryWithLabel(s) == null;
        });
    }
    
    static ReportViewEntry generateReportViewEntry(String reportName, ReportDefinition definition, Engine engine, Stage primaryStage) {
        String label = generateReportLabel(reportName, definition);
        ReportView reportView = new ReportView();
        reportView.setupReportView(label, definition, engine, primaryStage);
        
        ReportViewEntry entry = new ReportViewEntry(label, reportView);
        reportView.reportViewEntry = entry;
        
        openReportViewEntries.add(entry);
        return entry;
    }
}
