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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
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
public class StartDateOffsetChooserController implements Initializable {

    @FXML
    private RadioButton dateRadio;
    @FXML
    private DatePicker datePicker;
    @FXML
    private RadioButton lastYearRadio;
    @FXML
    private RadioButton lastQuarterRadio;
    @FXML
    private RadioButton lastMonthRadio;
    @FXML
    private RadioButton lastWeekRadio;
    @FXML
    private RadioButton previousYearsRadio;
    @FXML
    private TextField previousYearsEdit;
    @FXML
    private RadioButton previousQuartersRadio;
    @FXML
    private TextField previousQuartersEdit;
    @FXML
    private RadioButton previousMonthRadio;
    @FXML
    private TextField previousMonthsEdit;
    @FXML
    private RadioButton previousWeeksRadio;
    @FXML
    private TextField previousWeeksEdit;
    @FXML
    private ToggleGroup mainToggleGroup;
    
    
    private Stage stage;
    private DateOffset.StandardDateOffset dateOffset;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        previousYearsEdit.setOnKeyTyped((event)-> { previousYearsRadio.setSelected(true);});
        previousQuartersEdit.setOnKeyTyped((event)-> { previousQuartersRadio.setSelected(true);});
        previousMonthsEdit.setOnKeyTyped((event)-> { previousMonthRadio.setSelected(true);});
        previousWeeksEdit.setOnKeyTyped((event)-> { previousWeeksRadio.setSelected(true);});
        datePicker.focusedProperty().addListener((property, oldValue, newValue) -> {
            if (newValue) {
                dateRadio.setSelected(true);
            }
        });
    }    
    
    
    /**
     * Sets up the controller.
     * @param dateOffset The date offset to be represented by the controls.
     */
    public void setupController(DateOffset.StandardDateOffset dateOffset, Stage stage) {
        this.dateOffset = dateOffset;
        this.stage = stage;
        
        this.datePicker.setValue(LocalDate.now());
        
        switch (dateOffset.getStandard()) {
        case NULL :
            setFromNullDaysOffset();
            break;
            
        case DAYS_OFFSET :
            setFromDaysOffset();
            break;
            
        case NTH_DAY_OF_WEEK :
            setFromNthDayOfWeekOffset();
            break;
            
        case WEEKS_OFFSET :
            setFromWeeksOffset();
            break;
            
        case MONTHS_OFFSET :
            setFromMonthsOffset();
            break;
            
        case QUARTERS_OFFSET :
            setFromQuartersOffset();
            break;
            
        case YEARS_OFFSET :
            setFromYearsOffset();
            break;
        }
    }
    
    void setFromNullDaysOffset() {
        dateRadio.setSelected(true);
        datePicker.setValue(LocalDate.now());
    }
    
    void setFromDaysOffset() {
        DateOffset.DaysOffset daysOffset = (DateOffset.DaysOffset)dateOffset;
        if (daysOffset != null) {
            int dayCount = daysOffset.getDayCount();
            dateRadio.setSelected(true);
            
            LocalDate targetDate = LocalDate.now().plusDays(dayCount);
            datePicker.setValue(targetDate);
        }
    }
    
    void setFromNthDayOfWeekOffset() {
        setFromNullDaysOffset();
    }
    
    void setFromWeeksOffset() {
        setFromBasicStandardOffset(lastWeekRadio, previousWeeksRadio, previousWeeksEdit);
    }
    
    void setFromMonthsOffset() {
        setFromBasicStandardOffset(lastMonthRadio, previousMonthRadio, previousMonthsEdit);
    }
    
    void setFromQuartersOffset() {
        setFromBasicStandardOffset(lastQuarterRadio, previousQuartersRadio, previousQuartersEdit);
    }
    
    void setFromYearsOffset() {
        setFromBasicStandardOffset(lastYearRadio, previousYearsRadio, previousYearsEdit);
    }
    
    void setFromBasicStandardOffset(RadioButton lastRadio, RadioButton previousRadio, TextField previousEdit) {
        DateOffset.AbstractStandardDateOffset standardOffset = (DateOffset.AbstractStandardDateOffset)dateOffset;
        if (standardOffset != null) {
            int baseOffset = standardOffset.getBaseOffset();
            if (baseOffset > 0) {
                previousEdit.setText(Integer.toString(baseOffset));
                previousRadio.setSelected(true);
            }
            else {
                lastRadio.setSelected(true);
            }
        }
    }
    
    
    /**
     * Validates the current state of the controls, putting up an error message
     * if something is not valid for the currently selected option.
     * @return <code>true</code> if everything is fine and {@link #getStartDateOffset() } can be called.
     */
    public boolean validate() {
        if (dateRadio.isSelected()) {
            // Do we need to do any validation here???
            StringConverter<LocalDate> converter = datePicker.getConverter();
            try {
                String editedText = datePicker.getEditor().getText().trim();
                if (!editedText.isEmpty()) {
                    converter.fromString(editedText);
                }
            } catch (DateTimeParseException ex) {
                Validation.reportError("LBTimeUI.StartDateOffsetChooser.InvalidDate", stage);
                datePicker.getEditor().requestFocus();
                return false;
            }
        }
        else if (previousYearsRadio.isSelected()) {
            return Validation.validateEditCount(previousYearsEdit, "LBTimeUI.StartDateOffsetChooser.InvalidYearsPreviouslyCount", stage);
        }
        else if (previousQuartersRadio.isSelected()) {
            return Validation.validateEditCount(previousQuartersEdit, "LBTimeUI.StartDateOffsetChooser.InvalidQuartersPreviouslyCount", stage);
        }
        else if (previousMonthRadio.isSelected()) {
            return Validation.validateEditCount(previousMonthsEdit, "LBTimeUI.StartDateOffsetChooser.InvalidMonthsPreviouslyCount", stage);
        }
        else if (previousWeeksRadio.isSelected()) {
            return Validation.validateEditCount(previousWeeksEdit, "LBTimeUI.StartDateOffsetChooser.InvalidWeeksPreviouslyCount", stage);
        }
        return true;
    }
    
    int getEditCount(TextField field) {
        return Integer.parseInt(field.getText());
    }
    
    
    /**
     * @return Retrieves a {@link DateOffset} representing the current state of the controls.
     */
    public DateOffset.StandardDateOffset getStartDateOffset() {
        if (dateRadio.isSelected()) {
            StringConverter<LocalDate> converter = datePicker.getConverter();
            LocalDate selectedDate = datePicker.getValue();
            String editedText = datePicker.getEditor().getText().trim();
            if (!editedText.isEmpty()) {
                try {
                    selectedDate = converter.fromString(editedText);
                } catch (DateTimeParseException ex) {
                }
            }

            int dayCount = (int)ChronoUnit.DAYS.between(LocalDate.now(), selectedDate);
            return new DateOffset.DaysOffset(dayCount);
        }
        else if (lastYearRadio.isSelected()) {
            return new DateOffset.YearsOffset(0, 0, DateOffset.OffsetReference.LAST_DAY, null);
        }
        else if (previousYearsRadio.isSelected()) {
            int count = getEditCount(previousYearsEdit);
            return new DateOffset.YearsOffset(count, 0, DateOffset.OffsetReference.LAST_DAY, null);
        }
        else if (lastQuarterRadio.isSelected()) {
            return new DateOffset.QuartersOffset(0, 0, DateOffset.OffsetReference.LAST_DAY, null);
        }
        else if (previousQuartersRadio.isSelected()) {
            int count = getEditCount(previousQuartersEdit);
            return new DateOffset.QuartersOffset(count, 0, DateOffset.OffsetReference.LAST_DAY, null);
        }
        else if (lastMonthRadio.isSelected()) {
            return new DateOffset.MonthsOffset(0, 0, DateOffset.OffsetReference.LAST_DAY, null);
        }
        else if (previousMonthRadio.isSelected()) {
            int count = getEditCount(previousMonthsEdit);
            return new DateOffset.MonthsOffset(count, 0, DateOffset.OffsetReference.LAST_DAY, null);
        }
        else if (lastWeekRadio.isSelected()) {
            return new DateOffset.WeeksOffset(DayOfWeek.SUNDAY, 0, 0, DateOffset.OffsetReference.LAST_DAY);
        }
        else if (previousWeeksRadio.isSelected()) {
            int count = getEditCount(previousWeeksEdit);
            return new DateOffset.WeeksOffset(DayOfWeek.SUNDAY, count, 0, DateOffset.OffsetReference.LAST_DAY);
        }
        
        return null;
    }
}
