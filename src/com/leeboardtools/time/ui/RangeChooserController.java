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
package com.leeboardtools.time.ui;

import com.leeboardtools.dialog.Validation;
import com.leeboardtools.time.DateOffset;
import com.leeboardtools.util.ResourceSource;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Albert Santos
 */
public class RangeChooserController implements Initializable {

    @FXML
    private TextField countEdit;
    @FXML
    private ChoiceBox<DateOffset.Interval> periodChoice;
    
    private Stage stage;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        periodChoice.getItems().addAll(DateOffset.Interval.values());
        periodChoice.setConverter(DateOffset.INTERVAL_STRING_CONVERTER);
    }    
    
    
    public void setupController(DateOffset.Basic dateOffset, Stage stage) {
        if (dateOffset == null) {
            countEdit.setText("1");
            periodChoice.setValue(DateOffset.Interval.MONTH);
        }
        else {
            countEdit.setText(Integer.toString(dateOffset.getIntervalOffset()));
            periodChoice.setValue(dateOffset.getInterval());
        }
    }
    
    public boolean validate() {
        if (!Validation.validateEditCount(countEdit, ResourceSource.getString("LBTimeUI.RangeChooser.InvalidCount"), this.stage)) {
            return false;
        }
/*        int intervalOffset = Integer.parseInt(countEdit.getText());
        if (intervalOffset <= 0) {
            Validation.reportError(ResourceSource.getString("LBTimeUI.RangeChooser.InvalidCount"), this.stage);
            return false;
        }
*/        
        return true;
    }
    
    public DateOffset.Basic getRangeDateOffset() {
        int intervalOffset = Integer.parseInt(countEdit.getText());
        DateOffset.Interval interval = periodChoice.getValue();
        return new DateOffset.Basic(interval, intervalOffset, DateOffset.IntervalRelation.CURRENT_DAY);
    }
    
}
