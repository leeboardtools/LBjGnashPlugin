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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
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
    private ChoiceBox<ReportDefinition.Standard> reportStyleChoice;
    
    private Stage stage;
    private ReportDefinition definition;
    private Engine engine;
    
    private PeriodicDateGeneratorViewController periodicDateController;
    //private StartDateOffsetChooserController startDateOffsetController;
    
    private RangeChooserController rangeController;
    
    //private RangeDateOffsetChooserController rangeDateOffsetController;

    private AccountFilterViewController accountFilterViewController;
    private AccountFilter workingAccountFilter;
    
    
    private String netWorthText;
    private String incomeExpenseText;
    private String portfolioText;
    
    private StringConverter<ReportDefinition.Standard> reportStyleConverter = new StringConverter<ReportDefinition.Standard>() {
        @Override
        public String toString(ReportDefinition.Standard object) {
            switch (object) {
                case NET_WORTH :
                    return netWorthText;
                case INCOME_EXPENSE :
                    return incomeExpenseText;
                case PORTFOLIO :
                    return portfolioText;
            }
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public ReportDefinition.Standard fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    @FXML
    private VBox periodicDateVBox;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        try {
            netWorthText = ResourceSource.getString("Report.Title.NetWorth");
            incomeExpenseText = ResourceSource.getString("Report.Title.IncomeExpense");
            portfolioText = ResourceSource.getString("Report.Title.Portfolio");
            reportStyleChoice.getItems().addAll(ReportDefinition.Standard.values());
            reportStyleChoice.setConverter(reportStyleConverter);
            
            
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
        
        PeriodicDateGenerator dateGenerator = this.definition.getDateGenerator();
        this.periodicDateController.setupController(dateGenerator, stage);
        
        DateOffset.Basic rangeOffset = this.definition.getRangeDateOffset();
        this.rangeController.setupController(rangeOffset, stage);

        this.workingAccountFilter= new AccountFilter();
        this.workingAccountFilter.copyFrom(definition.getAccountFilter());
        this.accountFilterViewController.setupController(engine, workingAccountFilter);
    }

    @FXML
    private void onOK(ActionEvent event) {
        if (this.definition != null) {
            if (!this.periodicDateController.validate()) {
                return;
            }
            if (!this.rangeController.validate()) {
                return;
            }

            PeriodicDateGenerator dateGenerator = this.periodicDateController.getPeriodicDateGenerator();
            if (!dateGenerator.equals(this.definition.getDateGenerator())) {
                this.definition.setDateGenerator(dateGenerator);
            }
            
            DateOffset.Basic rangeOffset = this.rangeController.getRangeDateOffset();
            this.definition.setRangeDateOffset(rangeOffset);

            this.definition.setTitle(this.titleEdit.getText());
            
            this.definition.getAccountFilter().copyFrom(this.workingAccountFilter);
        }
        this.stage.close();
    }

    @FXML
    private void onCancel(ActionEvent event) {
        this.stage.close();
    }
    
    
}
