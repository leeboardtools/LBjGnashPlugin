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

import com.leeboardtools.time.DateOffset;
import com.leeboardtools.time.PeriodicDateGenerator;
import com.leeboardtools.time.ui.PeriodicDateGeneratorViewController;
import com.leeboardtools.time.ui.RangeChooserController;
import com.leeboardtools.util.ResourceSource;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import jgnash.engine.Engine;

/**
 * FXML Controller class
 *
 * @author Albert Santos
 */
public class ReportSetupViewController implements Initializable {

    @FXML
    private TextField titleEdit;
    @FXML
    private VBox timeRangeVBox;
    @FXML
    private VBox accountsVBox;
    @FXML
    private VBox columnsVBox;
    @FXML
    private ChoiceBox<ReportDefinition.Style> reportStyleChoice;
    
    private Stage stage;
    private ReportDefinition definition;
    private Engine engine;
    
    private PeriodicDateGeneratorViewController periodicDateController;
    
    private Parent rangeChooserRoot;
    private RangeChooserController rangeController;

    private AccountFilterViewController accountFilterViewController;
    private AccountFilter workingAccountFilter;
    
    
    @FXML
    private VBox periodicDateVBox;
    @FXML
    private ListView<ReportDefinition.ColumnType> availableColumnsListView;
    @FXML
    private Button useColumnButton;
    @FXML
    private Button dontUseColumnButton;
    @FXML
    private Button upColumnButton;
    @FXML
    private Button downColumnButton;
    @FXML
    private ListView<ReportDefinition.ColumnType> usedColumnsListView;
    
    
    public static enum CloseReason {
        OK,
        CANCEL,
    }
    
    private CloseReason closeReason = null;
    
    public final CloseReason getCloseReason() {
        return closeReason;
    }
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            reportStyleChoice.getItems().addAll(ReportDefinition.Style.values());
            reportStyleChoice.setConverter(ReportDefinition.STYLE_STRING_CONVERTER);
            reportStyleChoice.valueProperty().addListener((prop, oldValue, newValue) -> {
                boolean isDisable = !newValue.usesRangeDateOffset();
                rangeChooserRoot.setDisable(isDisable);
            });
            
            
            URL location;
            FXMLLoader fxmlLoader;
            Parent root;
            
            location = PeriodicDateGeneratorViewController.class.getResource("PeriodicDateGeneratorView.fxml");
            fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            root = fxmlLoader.load();
            periodicDateController = (PeriodicDateGeneratorViewController)fxmlLoader.getController();
            periodicDateVBox.getChildren().add(root);
            
            location = RangeChooserController.class.getResource("RangeChooser.fxml");
            fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            root = fxmlLoader.load();
            rangeChooserRoot = root;
            rangeController = (RangeChooserController)fxmlLoader.getController();
            timeRangeVBox.getChildren().add(root);
            
            
            location = AccountFilterViewController.class.getResource("AccountFilterView.fxml");
            fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            root = fxmlLoader.load();
            
            accountFilterViewController = (AccountFilterViewController)fxmlLoader.getController();
            VBox.setVgrow(root, Priority.ALWAYS);
            accountsVBox.getChildren().add(root);
            
            
            availableColumnsListView.setCellFactory((ListView<ReportDefinition.ColumnType> list) -> {
                return new TextFieldListCell(ReportDefinition.COLUMN_TYPE_STRING_CONVERTER);
            });
            availableColumnsListView.getSelectionModel().selectedItemProperty().addListener((prop, oldValue, newValue) -> {
                updateColumnButtons();
            });

            usedColumnsListView.setCellFactory((ListView<ReportDefinition.ColumnType> list) -> {
                return new TextFieldListCell(ReportDefinition.COLUMN_TYPE_STRING_CONVERTER);
            });
            usedColumnsListView.getSelectionModel().selectedItemProperty().addListener((prop, oldValue, newValue) -> {
                updateColumnButtons();
            });

        } catch (IOException ex) {
            Logger.getLogger(ReportSetupViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    public void setupController(ReportDefinition definition, Engine engine, Stage stage) {
        this.stage = stage;
        this.definition = definition;
        this.engine = engine;
        
        this.titleEdit.setText(definition.getTitle());
        
        this.reportStyleChoice.setValue(definition.getStyle());
        
        PeriodicDateGenerator dateGenerator = this.definition.getDateGenerator();
        if (dateGenerator == null) {
            dateGenerator = new PeriodicDateGenerator(DateOffset.SAME_DAY, DateOffset.END_OF_LAST_YEAR, 0);
        }
        this.periodicDateController.setupController(dateGenerator, stage);
        
        DateOffset.Basic rangeOffset = this.definition.getRangeDateOffset();
        this.rangeController.setupController(rangeOffset, stage);

        this.workingAccountFilter= new AccountFilter();
        this.workingAccountFilter.copyFrom(definition.getAccountFilter());
        this.accountFilterViewController.setupController(engine, workingAccountFilter);
        
        
        this.availableColumnsListView.getItems().clear();
        for (ReportDefinition.ColumnType columnType : ReportDefinition.ColumnType.values()) {
            if (!definition.getColumnTypes().contains(columnType)) {
                this.availableColumnsListView.getItems().add(columnType);
            }
        }
        
        this.usedColumnsListView.getItems().clear();
        this.usedColumnsListView.getItems().addAll(definition.getColumnTypes());
        
        updateColumnButtons();
        
        this.closeReason = null;
    }

    @FXML
    private void onOK(ActionEvent event) {
        if (this.definition != null) {
            ReportDefinition.Style style = this.reportStyleChoice.getValue();
            
            if (!this.periodicDateController.validate()) {
                return;
            }
            
            DateOffset.Basic rangeOffset;
            if (style.usesRangeDateOffset()) {
                if (!this.rangeController.validate()) {
                    return;
                }
                rangeOffset = this.rangeController.getRangeDateOffset();
            }
            else {
                rangeOffset = null;
            }

            PeriodicDateGenerator dateGenerator = this.periodicDateController.getPeriodicDateGenerator();
            if (!dateGenerator.equals(this.definition.getDateGenerator())) {
                this.definition.setDateGenerator(dateGenerator);
            }
            
            this.definition.setRangeDateOffset(rangeOffset);

            this.definition.setTitle(this.titleEdit.getText());
            this.definition.setStyle(style);
            this.definition.getAccountFilter().copyFrom(this.workingAccountFilter);
            
            this.definition.getColumnTypes().clear();
            this.definition.getColumnTypes().addAll(this.usedColumnsListView.getItems());
        }
        this.closeReason = CloseReason.OK;
        this.stage.close();
    }

    @FXML
    private void onCancel(ActionEvent event) {
        this.closeReason = CloseReason.CANCEL;
        this.stage.close();
    }

    @FXML
    private void onUseColumn(ActionEvent event) {
        ReportDefinition.ColumnType columnType = this.availableColumnsListView.getSelectionModel().getSelectedItem();
        if (columnType != null) {
            int itemIndex = this.availableColumnsListView.getSelectionModel().getSelectedIndex();
            this.availableColumnsListView.getItems().remove(columnType);
            this.availableColumnsListView.getSelectionModel().select(itemIndex);
            this.usedColumnsListView.getItems().add(columnType);
            this.usedColumnsListView.getSelectionModel().select(columnType);
        }
    }

    @FXML
    private void onDontUseColumn(ActionEvent event) {
        ReportDefinition.ColumnType columnType = this.usedColumnsListView.getSelectionModel().getSelectedItem();
        if (columnType != null) {
            int itemIndex = this.usedColumnsListView.getSelectionModel().getSelectedIndex();
            this.usedColumnsListView.getItems().remove(columnType);
            this.usedColumnsListView.getSelectionModel().select(itemIndex);
            
            int count = this.availableColumnsListView.getItems().size();
            int insertBefore;
            for (insertBefore = 0; insertBefore < count; ++insertBefore) {
                if (this.availableColumnsListView.getItems().get(insertBefore).ordinal() > columnType.ordinal()) {
                    break;
                }
            }
            this.availableColumnsListView.getItems().add(insertBefore, columnType);
            this.availableColumnsListView.getSelectionModel().select(columnType);
        }
    }

    @FXML
    private void onUpColumn(ActionEvent event) {
        int itemIndex = this.usedColumnsListView.getSelectionModel().getSelectedIndex();
        if (itemIndex > 0) {
            ReportDefinition.ColumnType columnType = this.usedColumnsListView.getItems().remove(itemIndex);
            this.usedColumnsListView.getItems().add(itemIndex - 1, columnType);
            this.usedColumnsListView.getSelectionModel().select(columnType);
        }
    }

    @FXML
    private void onDownColumn(ActionEvent event) {
        int itemIndex = this.usedColumnsListView.getSelectionModel().getSelectedIndex();
        if ((itemIndex >= 0) && ((itemIndex + 1) < this.usedColumnsListView.getItems().size())) {
            ReportDefinition.ColumnType columnType = this.usedColumnsListView.getItems().remove(itemIndex);
            this.usedColumnsListView.getItems().add(itemIndex + 1, columnType);
            this.usedColumnsListView.getSelectionModel().select(columnType);
        }
    }
    
    private void updateColumnButtons() {
        if (this.availableColumnsListView.getSelectionModel().getSelectedItem() == null) {
            this.useColumnButton.setDisable(true);
        }
        else {
            this.useColumnButton.setDisable(false);
        }
        
        if (this.usedColumnsListView.getSelectionModel().getSelectedItem() == null) {
            this.dontUseColumnButton.setDisable(true);
            this.upColumnButton.setDisable(true);
            this.downColumnButton.setDisable(true);
        }
        else {
            this.dontUseColumnButton.setDisable(false);

            if (this.usedColumnsListView.getSelectionModel().getSelectedIndex() == 0) {
                this.upColumnButton.setDisable(true);
            }
            else {
                this.upColumnButton.setDisable(false);
            }
            if ((this.usedColumnsListView.getSelectionModel().getSelectedIndex() + 1) >= this.usedColumnsListView.getItems().size()) {
                this.downColumnButton.setDisable(true);
            }
            else {
                this.downColumnButton.setDisable(false);
            }
        }
        
    }
    
}
