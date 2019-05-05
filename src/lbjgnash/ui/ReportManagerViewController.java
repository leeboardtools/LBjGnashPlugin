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
import com.leeboardtools.util.FileUtil;
import com.leeboardtools.util.ResourceSource;
import com.leeboardtools.util.StringUtil;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import jgnash.engine.Engine;

/**
 * FXML Controller class
 *
 * @author Albert Santos
 */
public class ReportManagerViewController implements Initializable {
    @FXML
    private ListView<String> reportsListView;
    @FXML
    private Button openButton;
    @FXML
    private Button editButton;
    @FXML
    private Button newButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button doneButton;

    private Engine engine;
    private Stage stage;
    

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }    
    
    
    public void setupController(Engine engine, Stage stage) {
        this.engine = engine;
        this.stage = stage;
        
        loadReportDefinitions(null);
    }
    
    private void loadReportDefinitions(String reportToSelect) {
        reportsListView.getItems().clear();
        reportsListView.getItems().addAll(ReportManager.getAvailableReportNames());
        if (reportToSelect != null) {
            reportsListView.getSelectionModel().select(reportToSelect);
        }
    }

    @FXML
    private void onOpen(ActionEvent event) {
        String reportName = reportsListView.getSelectionModel().getSelectedItem();
        if (reportName != null) {
            ReportView.openReportView(reportName, engine, stage);
        }
    }

    @FXML
    private void onEdit(ActionEvent event) {
        String reportName = reportsListView.getSelectionModel().getSelectedItem();
        if (reportName != null) {
            ReportDefinition reportDefinition = ReportManager.getReportDefinition(reportName);
            if (reportDefinition != null) {
                ReportSetupView.showAndWait(reportName, reportDefinition, engine, stage);
                ReportManager.saveReport(reportName);
                loadReportDefinitions(reportName);
            }
        }
    }
    
    
    public static String promptNewReportName(Stage stage, String currentName) {
        PromptDialog dialog = new PromptDialog();
        dialog.setTitle(ResourceSource.getString("NewReport.Title"));
        String label = ResourceSource.getString("NewReport.Label");
        String id = "reportName";
        String promptText = ResourceSource.getString("NewReport.PromptText");
        
        String okText = ResourceSource.getString("LBDialog.OK");
        String cancelText = ResourceSource.getString("LBDialog.Cancel");
        
        dialog.addTextInput(label, id, currentName, promptText, true);
        dialog.addButton(okText, PromptDialog.BTN_OK);
        dialog.addButton(cancelText, PromptDialog.BTN_CANCEL);
        dialog.setDefaultButtonId(PromptDialog.BTN_OK);
        dialog.setCancelButtonId(PromptDialog.BTN_CANCEL);
        
        dialog.setButtonCloseCallback((chosenId) -> {
            if (chosenId == PromptDialog.BTN_CANCEL) {
                return true;
            }
            
            String reportName = dialog.getTextInputText(id).trim();
            
            if (!FileUtil.isAcceptableFileName(reportName)) {
                String invalidMessage = ResourceSource.getString("NewReport.InvalidName", reportName);
                String invalidTitle = ResourceSource.getString("NewReport.InvalidNameTitle");
                PromptDialog.showOKDialog(stage, invalidMessage, invalidTitle);
                return false;
            }
            
            if ((currentName == null) || !currentName.equals(reportName)) {
                if (ReportManager.getAvailableReportNames().contains(reportName)) {
                    String message = ResourceSource.getString("NewReport.DuplicateReportConfirm", reportName);
                    String title = ResourceSource.getString("NewReport.DuplicateReportTitle");
                    return PromptDialog.showOKCancelDialog(stage, message, title);
                }
            }
            
            return true;
        });
        
        if (dialog.showSimpleDialog(stage) == PromptDialog.BTN_OK) {
            String newReportName = dialog.getTextInputText(id).trim();
            return newReportName;
        }
        
        return null;
    }

    @FXML
    private void onNew(ActionEvent event) {
        String newReportName = promptNewReportName(stage, null);
        if (StringUtil.isNonEmpty(newReportName)) {
            ReportDefinition oldReportDefinition = null;
            if (this.reportsListView.getItems().contains(newReportName)) {
                oldReportDefinition = ReportManager.getReportDefinition(newReportName);
            }
            
            ReportDefinition reportDefinition = ReportManager.createReportDefinition(newReportName, ReportDefinition.Style.NET_WORTH);
            if (reportDefinition != null) {
                if (oldReportDefinition != null) {
                    reportDefinition.copyFrom(oldReportDefinition);
                }
                else {
                    String selectedReportName = reportsListView.getSelectionModel().getSelectedItem();
                    if (selectedReportName != null) {
                        ReportDefinition selectedReportDefinition = ReportManager.getReportDefinition(selectedReportName);
                        if (selectedReportDefinition != null) {
                            reportDefinition.copyFrom(selectedReportDefinition);
                        }
                    }
                }
                
                if (!ReportSetupView.showAndWait(newReportName, reportDefinition, engine, stage)) {
                    // Canceled, delete the report if it wasn't already existing..
                    if (oldReportDefinition == null) {
                        ReportManager.deleteReport(newReportName);
                    }
                    else {
                        // Restore the old report...
                        reportDefinition.copyFrom(oldReportDefinition);
                    }
                }
                else {
                    ReportManager.saveReport(newReportName);
                    loadReportDefinitions(newReportName);
                }
            }
        }
    }

    @FXML
    private void onDelete(ActionEvent event) {
        String reportName = reportsListView.getSelectionModel().getSelectedItem();
        if (reportName != null) {
            String message = ResourceSource.getString("DeleteReport.Confirm", reportName);
            String title = ResourceSource.getString("DeleteReport.Title");
            if (PromptDialog.showOKCancelDialog(stage, message, title)) {
                int index = reportsListView.getItems().indexOf(reportName);
                if (index < 0) {
                    index = 0;
                }
                
                String nextReport = (index < (reportsListView.getItems().size() - 1)) ? reportsListView.getItems().get(index) : null;
                
                ReportManager.deleteReport(reportName);
                
                loadReportDefinitions(nextReport);
            }
        }
    }

    @FXML
    private void onDone(ActionEvent event) {
        this.stage.close();
    }
    
}
