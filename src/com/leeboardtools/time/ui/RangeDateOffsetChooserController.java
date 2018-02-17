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
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Albert Santos
 */
public class RangeDateOffsetChooserController implements Initializable {

    @FXML
    private RadioButton startEndRadio;
    @FXML
    private ChoiceBox<DateOffset.OffsetReference> startEndChoice;
    @FXML
    private ChoiceBox<DateOffset.Standard> startEndPeriodChoice;
    @FXML
    private RadioButton offsetRadio;
    @FXML
    private TextField daysEdit;
    @FXML
    private ChoiceBox<DateOffset.OffsetReference> offsetStartEndChoice;
    @FXML
    private ChoiceBox<DateOffset.Standard> offsetPeriodChoice;
    @FXML
    private RadioButton dayOfWeekRadio;
    @FXML
    private TextField dayOfWeekCountEdit;
    @FXML
    private ChoiceBox<DayOfWeek> dayOfWeekChoice;
    @FXML
    private ChoiceBox<DateOffset.OffsetReference> dayOfWeekStartEndChoice;
    @FXML
    private ChoiceBox<DateOffset.Standard> dayOfWeekPeriodChoice;
    
    private String firstDayText;
    private String lastDayText;
    
    private StringConverter<DateOffset.OffsetReference> startEndConverter = new StringConverter<DateOffset.OffsetReference>() {
        @Override
        public String toString(DateOffset.OffsetReference object) {
            switch(object) {
                case FIRST_DAY :
                    return firstDayText;
                case LAST_DAY :
                    return lastDayText;
            }
            return null;
        }

        @Override
        public DateOffset.OffsetReference fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    
    
    private String yearsText;
    private String quartersText;
    private String monthsText;
    private String weeksText;
    
    private StringConverter<DateOffset.Standard> periodConverter = new StringConverter<DateOffset.Standard>() {
        @Override
        public String toString(DateOffset.Standard object) {
            switch(object) {
                case YEARS_OFFSET :
                    return yearsText;
                case QUARTERS_OFFSET :
                    return quartersText;
                case MONTHS_OFFSET :
                    return monthsText;
                case WEEKS_OFFSET :
                    return weeksText;
            }
            return null;
        }

        @Override
        public DateOffset.Standard fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    
    private StringConverter<DayOfWeek> dayOfWeekConverter = new StringConverter<DayOfWeek>() {
        @Override
        public String toString(DayOfWeek object) {
            return object.getDisplayName(TextStyle.FULL, Locale.getDefault());
        }

        @Override
        public DayOfWeek fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };
    @FXML
    private ToggleGroup mainToggleGroup;

    private DateOffset.StandardDateOffset dateOffset;
    private Stage stage;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        firstDayText = ResourceSource.getString("LBTimeUI.RangeDateOffsetChooser.FirstDay");
        lastDayText = ResourceSource.getString("LBTimeUI.RangeDateOffsetChooser.LastDay");
        
        yearsText = ResourceSource.getString("LBTimeUI.RangeDateOffsetChooser.Year");
        quartersText = ResourceSource.getString("LBTimeUI.RangeDateOffsetChooser.Quarter");
        monthsText = ResourceSource.getString("LBTimeUI.RangeDateOffsetChooser.Month");
        weeksText = ResourceSource.getString("LBTimeUI.RangeDateOffsetChooser.Week");
        
        setupStartEndChoice(startEndChoice);
        setupPeriodChoice(startEndPeriodChoice);
        
        setupStartEndChoice(offsetStartEndChoice);
        setupPeriodChoice(offsetPeriodChoice);
        
        setupStartEndChoice(dayOfWeekStartEndChoice);
        setupPeriodChoice(dayOfWeekPeriodChoice);
        
        setupDayOfWeekChoice(dayOfWeekChoice);
    }    
    
    void setupStartEndChoice(ChoiceBox<DateOffset.OffsetReference> choiceBox) {
        choiceBox.getItems().addAll(
                DateOffset.OffsetReference.FIRST_DAY, 
                DateOffset.OffsetReference.LAST_DAY);
        choiceBox.setConverter(startEndConverter);
    }
    
    void setupPeriodChoice(ChoiceBox<DateOffset.Standard> choiceBox) {
        choiceBox.getItems().addAll(
                DateOffset.Standard.YEARS_OFFSET, 
                DateOffset.Standard.QUARTERS_OFFSET,
                DateOffset.Standard.MONTHS_OFFSET,
                DateOffset.Standard.WEEKS_OFFSET);
        choiceBox.setConverter(periodConverter);
    }
    
    void setupDayOfWeekChoice(ChoiceBox<DayOfWeek> choiceBox) {
        choiceBox.getItems().addAll(
                DayOfWeek.SUNDAY,
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY);
        choiceBox.setConverter(dayOfWeekConverter);
    }
    
    public void setupController(DateOffset.StandardDateOffset dateOffset, Stage stage) {
        if (dateOffset == null) {
            dateOffset = new DateOffset.Null();
        }
        
        this.dateOffset = dateOffset;
        this.stage = stage;
        
        switch (dateOffset.getStandard()) {
        case NULL :
        case DAYS_OFFSET :
            setFromNullDaysOffset();
            break;
            
        case NTH_DAY_OF_WEEK :
            setFromNthDayOfWeekOffset();
            break;
            
        case WEEKS_OFFSET :
        case MONTHS_OFFSET :
        case QUARTERS_OFFSET :
        case YEARS_OFFSET :
            setFromOffset(dateOffset.getStandard());
            break;
        }
    }
    
    private void setFromNullDaysOffset() {
        startEndRadio.setSelected(true);
        startEndChoice.setValue(DateOffset.OffsetReference.LAST_DAY);
        startEndPeriodChoice.setValue(DateOffset.Standard.YEARS_OFFSET);
    }

    private void setFromNthDayOfWeekOffset() {
        DateOffset.NthDayOfWeek nthDayOfWeek = (DateOffset.NthDayOfWeek)this.dateOffset;
        if (nthDayOfWeek != null) {
            dayOfWeekRadio.setSelected(true);
            dayOfWeekCountEdit.setText(Integer.toString(nthDayOfWeek.getOccurrence()));
            dayOfWeekChoice.setValue(nthDayOfWeek.getDayOfWeek());
        }
    }

    private void setFromOffset(DateOffset.Standard standard) {
        DateOffset.AbstractStandardDateOffset standardDateOffset = (DateOffset.AbstractStandardDateOffset)this.dateOffset;
        if (standardDateOffset != null) {
            int dayCount = standardDateOffset.getDaysOffset();
            if (dayCount == 0) {
                startEndRadio.setSelected(true);
                startEndChoice.setValue(standardDateOffset.getOffsetReference());
                startEndPeriodChoice.setValue(standard);
            }
            else {
                offsetRadio.setSelected(true);
                daysEdit.setText(Integer.toString(dayCount));
                offsetStartEndChoice.setValue(standardDateOffset.getOffsetReference());
                offsetPeriodChoice.setValue(standard);
            }
        }
    }
    
    public boolean validate() {
        if (startEndRadio.isSelected()) {
        }
        else if (offsetRadio.isSelected()) {
            return Validation.validateEditCount(daysEdit, "LBTimeUI.RangeDateOffsetChooser.InvalidDaysCount", this.stage);
        }
        else if (dayOfWeekRadio.isSelected()) {
            return Validation.validateEditCount(dayOfWeekCountEdit, "LBTimeUI.RangeDateOffsetChooser.InvalidWeeksCount", this.stage);
        }
        return true;
    }
    
    public DateOffset.StandardDateOffset getRangeDateOffset() {
        return null;
    }

}
