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
import com.leeboardtools.time.DatePeriods;
import com.leeboardtools.time.DateRange;
import com.leeboardtools.time.ui.RangeChooserController;
import com.leeboardtools.time.ui.RangeDateOffsetChooserController;
import com.leeboardtools.time.ui.StartDateOffsetChooserController;
import com.leeboardtools.util.ResourceSource;
import java.io.IOException;
import java.net.URL;
import java.time.Period;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
    private VBox startDateOffsetVBox;
    @FXML
    private VBox timeRangeVBox;
    @FXML
    private VBox rangeDateOffsetVBox;
    @FXML
    private VBox accountsVBox;
    @FXML
    private VBox columnsVBox;
    
    private Stage stage;
    private ReportDefinition definition;
    private Engine engine;
    
    private StartDateOffsetChooserController startDateOffsetController;
    
    private RangeChooserController rangeController;
    
    private RangeDateOffsetChooserController rangeDateOffsetController;

    private AccountFilterViewController accountFilterViewController;
    private AccountFilter workingAccountFilter;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        try {
            URL location;
            FXMLLoader fxmlLoader;
            Parent root;
            
            location = StartDateOffsetChooserController.class.getResource("StartDateOffsetChooser.fxml");
            fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            root = fxmlLoader.load();
            startDateOffsetController = (StartDateOffsetChooserController)fxmlLoader.getController();
            startDateOffsetVBox.getChildren().add(root);
            
            
            location = RangeDateOffsetChooserController.class.getResource("RangeDateOffsetChooser.fxml");
            fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            root = fxmlLoader.load();
            rangeDateOffsetController = (RangeDateOffsetChooserController)fxmlLoader.getController();
            rangeDateOffsetVBox.getChildren().add(root);
            
            
            location = RangeChooserController.class.getResource("RangeChooser.fxml");
            fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            root = fxmlLoader.load();
            rangeController = (RangeChooserController)fxmlLoader.getController();
            timeRangeVBox.getChildren().add(root);
            
            
            location = AccountFilterViewController.class.getResource("AccountFilterView.fxml");
            fxmlLoader = new FXMLLoader(location, ResourceSource.getBundle());
            root = fxmlLoader.load();
            
            accountFilterViewController = (AccountFilterViewController)fxmlLoader.getController();
            VBox.setVgrow(root, Priority.ALWAYS);
            accountsVBox.getChildren().add(root);

        } catch (IOException ex) {
            Logger.getLogger(ReportSetupViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    public void setupController(ReportDefinition definition, Engine engine, Stage stage) {
        this.stage = stage;
        this.definition = definition;
        this.engine = engine;
        
        this.titleEdit.setText(definition.getTitle());
        
        DatePeriods dateSettings = this.definition.getDateSettings();
        this.startDateOffsetController.setupController((DateOffset.StandardDateOffset)dateSettings.getStartDateOffset(), stage);
        this.rangeDateOffsetController.setupController((DateOffset.StandardDateOffset)dateSettings.getRangeOffset(), stage);
        
        this.workingAccountFilter= new AccountFilter();
        this.workingAccountFilter.copyFrom(definition.getAccountFilter());
        this.accountFilterViewController.setupController(engine, workingAccountFilter);
    }

    @FXML
    private void onOK(ActionEvent event) {
        if (this.definition != null) {
            if (!this.startDateOffsetController.validate()) {
                return;
            }
            if (!this.rangeDateOffsetController.validate()) {
                return;
            }
            
            DatePeriods dateSettings = this.definition.getDateSettings();
            Period period = dateSettings.getPeriod();
            int periodCount = dateSettings.getPeriodCount();
            DateOffset startDateOffset = this.startDateOffsetController.getStartDateOffset();
            DateRange.Generator rangeGenerator = dateSettings.getRangeGenerator();
            DateOffset rangeOffset = this.rangeDateOffsetController.getRangeDateOffset();
            
            dateSettings = new DatePeriods(period, periodCount, startDateOffset, rangeGenerator, rangeOffset);
            this.definition.setDateSettings(dateSettings);

            this.definition.setTitle(this.titleEdit.getText());
            
            this.definition.getAccountFilter().copyFrom(this.workingAccountFilter);
        }
        this.stage.close();
    }

    @FXML
    private void onCancel(ActionEvent event) {
        this.stage.close();
    }
    
    
    @FXML
    private void onNetWorth(ActionEvent event) {
    }

    @FXML
    private void onIncomeExpense(ActionEvent event) {
    }

    @FXML
    private void onPortfolio(ActionEvent event) {
    }
}
