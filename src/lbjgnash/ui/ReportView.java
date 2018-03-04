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

import lbjgnash.ui.reportview.ReportDataView;
import com.leeboardtools.util.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jgnash.engine.Engine;

/**
 * TODO Add message channel listeners.
 * TODO Add a menu for setup, updating.
 * @author Albert Santos
 */
public class ReportView {
    private static List<ReportViewEntry> openReportViewEntries = new ArrayList<>();
    
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
        
        this.stage.initOwner(primaryStage);
        this.stage.setTitle(label);
        
        this.mainVBox = new VBox();
        this.mainScene = new Scene(this.mainVBox);
        this.mainScene.getStylesheets().add("lbjgnash/ui/Styles.css");
        
        this.stage.setScene(this.mainScene);
        
        this.mainVBox.setPrefSize(800, 600);
        
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
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER);
            hBox.getStyleClass().add(CLASS_TITLE_LABEL_CONTAINER);
            
            this.titleLabel = new Label();
            this.titleLabel.getStyleClass().add(CLASS_TITLE_LABEL);
            hBox.getChildren().add(this.titleLabel);
            
            this.mainVBox.getChildren().add(hBox);
        }
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
    
    
    public static class ReportViewEntry {
        private final String reportLabel;
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
        openReportViewEntries.add(entry);
        return entry;
    }
}
