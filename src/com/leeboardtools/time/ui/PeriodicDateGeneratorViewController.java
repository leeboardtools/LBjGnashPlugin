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
import com.leeboardtools.time.DateUtil;
import com.leeboardtools.time.PeriodicDateGenerator;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Albert Santos
 */
public class PeriodicDateGeneratorViewController implements Initializable {

    @FXML
    private RadioButton specificDateRadio;
    @FXML
    private DatePicker specificDatePicker;
    @FXML
    private RadioButton currentIntervalRadio;
    @FXML
    private ChoiceBox<DateOffset.Interval> currentIntervalChoice;
    @FXML
    private RadioButton previousRadio;
    @FXML
    private ChoiceBox<DateOffset.IntervalRelation> previousRelationChoice;
    @FXML
    private ChoiceBox<DateOffset.Interval> previousIntervalChoice;
    @FXML
    private TextField agoCountEdit;
    @FXML
    private ChoiceBox<DateOffset.Interval> agoIntervalChoice;
    @FXML
    private RadioButton repeatCountRadio;
    @FXML
    private TextField repeatCountEdit;
    @FXML
    private RadioButton repeatUntilRadio;
    @FXML
    private DatePicker repeatUntilDatePicker;
    @FXML
    private TextField everyCountEdit;
    @FXML
    private ChoiceBox<DateOffset.Interval> everyIntervalChoice;
    @FXML
    private ToggleGroup startToggleGroup;
    @FXML
    private ToggleGroup repeatToggleGroup;
    @FXML
    private RadioButton agoRadio;
    
    
    private Stage stage;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        specificDatePicker.focusedProperty().addListener((prop, oldValue, newValue) -> {
            if (newValue) {
                specificDateRadio.setSelected(true);
            }
        });
        
        currentIntervalChoice.getItems().addAll(DateOffset.Interval.valuesNoDay());
        currentIntervalChoice.setConverter(DateOffset.INTERVAL_STRING_CONVERTER);
        currentIntervalChoice.focusedProperty().addListener((prop, oldValue, newValue) -> {
            if (newValue) {
                currentIntervalRadio.setSelected(true);
            }
        });

        previousIntervalChoice.getItems().addAll(DateOffset.Interval.valuesNoDay());
        previousIntervalChoice.setConverter(DateOffset.INTERVAL_STRING_CONVERTER);
        previousIntervalChoice.focusedProperty().addListener((prop, oldValue, newValue) -> {
            if (newValue) {
                previousRadio.setSelected(true);
            }
        });

        previousRelationChoice.getItems().addAll(DateOffset.IntervalRelation.valuesNoCurrentDay());
        previousRelationChoice.setConverter(DateOffset.INTERVAL_RELATION_STRING_CONVERTER);
        previousRelationChoice.focusedProperty().addListener((prop, oldValue, newValue) -> {
            if (newValue) {
                previousRadio.setSelected(true);
            }
        });

        agoIntervalChoice.getItems().addAll(DateOffset.Interval.valuesNoDay());
        agoIntervalChoice.setConverter(DateOffset.INTERVAL_STRING_CONVERTER);
        agoIntervalChoice.focusedProperty().addListener((prop, oldValue, newValue) -> {
            if (newValue) {
                agoRadio.setSelected(true);
            }
        });
        agoCountEdit.setOnKeyTyped((event)-> {
            agoRadio.setSelected(true);
        });
        
        repeatCountEdit.setOnKeyTyped((event) -> {
            repeatCountRadio.setSelected(true);
        });
        repeatUntilDatePicker.focusedProperty().addListener((prop, oldValue, newValue) -> {
            if (newValue) {
                repeatUntilRadio.setSelected(true);
            }
        });

        everyIntervalChoice.getItems().addAll(DateOffset.Interval.values());
        everyIntervalChoice.setConverter(DateOffset.INTERVAL_STRING_CONVERTER);
    }    
    
    
    public void setupController(PeriodicDateGenerator generator, Stage stage) {
        this.stage = stage;
        
        // Make sure something's selected, or else if you click on the choice box the drop-down
        // appears somewhere else...
        specificDatePicker.setValue(LocalDate.now());
        currentIntervalChoice.setValue(DateOffset.Interval.MONTH);
        previousIntervalChoice.setValue(DateOffset.Interval.YEAR);
        previousRelationChoice.setValue(DateOffset.IntervalRelation.LAST_DAY);
        agoIntervalChoice.setValue(DateOffset.Interval.YEAR);
        repeatUntilDatePicker.setValue(DateOffset.TWELVE_MONTHS_PRIOR.getOffsetDate(LocalDate.now()));
        everyIntervalChoice.setValue(DateOffset.Interval.YEAR);
        
        DateOffset.Basic startDateOffset;
        if (!(generator.getStartDateOffset() instanceof DateOffset.Basic)) {
            startDateOffset = DateOffset.SAME_DAY;
        }
        else {
            startDateOffset = (DateOffset.Basic)generator.getStartDateOffset();
        }
        setupStartDateOffset(startDateOffset);
        
        setupRepeatUntil(generator);
        setupRepeatPeriod(generator);
    }
    
    public boolean validate() {
        if (agoRadio.isSelected()) {
            if (!Validation.validateEditCount(agoCountEdit, "LBTimeUI.PeriodicDateGeneratorView.InvalidAgoCount", this.stage)) {
                return false;
            }
        }
        if (repeatCountRadio.isSelected()) {
            if (!Validation.validateEditCount(repeatCountEdit, "LBTimeUI.PeriodicDateGeneratorView.InvalidRepeatCount", this.stage)) {
                return false;
            }
        }
        
        if (!Validation.validateEditCount(everyCountEdit, "LBTimeUI.PeriodicDateGeneratorView.InvalidEveryCount", this.stage)) {
            return false;
        }
        return true;
    }
    
    public PeriodicDateGenerator getPeriodicDateGenerator() {
        DateOffset.Basic startDateOffset;
        if (specificDateRadio.isSelected()) {
            LocalDate date = specificDatePicker.getValue();
            int deltaDays = (int)DateUtil.daysTo(LocalDate.now(), date);
            startDateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, deltaDays, DateOffset.IntervalRelation.CURRENT_DAY);
        }
        else if (currentIntervalRadio.isSelected()) {
            DateOffset.Interval interval = currentIntervalChoice.getValue();
            startDateOffset = new DateOffset.Basic(interval, 0, DateOffset.IntervalRelation.FIRST_DAY);
        }
        else if (previousRadio.isSelected()) {
            DateOffset.Interval interval = previousIntervalChoice.getValue();
            DateOffset.IntervalRelation intervalRelation = previousRelationChoice.getValue();
            int delta = (intervalRelation == DateOffset.IntervalRelation.LAST_DAY) ? 1 : -1;
            startDateOffset = new DateOffset.Basic(interval, delta, intervalRelation);
        }
        else {
            int count = Integer.parseInt(agoCountEdit.getText());
            DateOffset.Interval interval = agoIntervalChoice.getValue();
            startDateOffset = new DateOffset.Basic(interval, -count, DateOffset.IntervalRelation.CURRENT_DAY);
        }
        
        int periodCount;
        DateOffset endDateOffset;
        if (repeatCountRadio.isSelected()) {
            periodCount = Integer.parseInt(repeatCountEdit.getText());
            endDateOffset = null;
        }
        else {
            periodCount = 0;
            LocalDate date = repeatUntilDatePicker.getValue();
            int deltaDays = (int)DateUtil.daysTo(LocalDate.now(), date);
            endDateOffset = new DateOffset.Basic(DateOffset.Interval.DAY, deltaDays, DateOffset.IntervalRelation.CURRENT_DAY);
        }

        int periodIncrementOffset = -Integer.parseInt(everyCountEdit.getText());
        DateOffset.Interval periodInterval = everyIntervalChoice.getValue();
        DateOffset.Basic periodDateOffset = new DateOffset.Basic(periodInterval, periodIncrementOffset, DateOffset.IntervalRelation.CURRENT_DAY);
        
        return new PeriodicDateGenerator(startDateOffset, periodDateOffset, periodCount, endDateOffset);
    }

    private void setupStartDateOffset(DateOffset.Basic startDateOffset) {
        if (startDateOffset.getInterval() == DateOffset.Interval.DAY) {
            specificDateRadio.setSelected(true);
            
            LocalDate date = startDateOffset.getOffsetDate(LocalDate.now());
            specificDatePicker.setValue(date);
        }
        else if ((startDateOffset.getIntervalOffset() == 0) 
                && (startDateOffset.getIntervalRelation() == DateOffset.IntervalRelation.FIRST_DAY)) {
            currentIntervalRadio.setSelected(true);
            currentIntervalChoice.setValue(startDateOffset.getInterval());
        }
        else if (((startDateOffset.getIntervalOffset() == -1) && (startDateOffset.getIntervalRelation() == DateOffset.IntervalRelation.FIRST_DAY))
              || ((startDateOffset.getIntervalOffset() == 1) && (startDateOffset.getIntervalRelation() == DateOffset.IntervalRelation.LAST_DAY))) {
            previousRadio.setSelected(true);
            previousIntervalChoice.setValue(startDateOffset.getInterval());
            previousRelationChoice.setValue(startDateOffset.getIntervalRelation());
        }
        else {
            agoRadio.setSelected(true);
            agoCountEdit.setText(Integer.toString(startDateOffset.getIntervalOffset()));
            agoIntervalChoice.setValue(startDateOffset.getInterval());
        }
    }

    private void setupRepeatUntil(PeriodicDateGenerator generator) {
        DateOffset endDateOffset = generator.getEndDateOffset();
        if (endDateOffset != null) {
            repeatUntilRadio.setSelected(true);
            LocalDate date = endDateOffset.getOffsetDate(LocalDate.now());
            repeatUntilDatePicker.setValue(date);
        }
        else {
            repeatCountRadio.setSelected(true);
            repeatCountEdit.setText(Integer.toString(generator.getPeriodCount()));
        }
    }

    private void setupRepeatPeriod(PeriodicDateGenerator generator) {
        DateOffset.Basic periodDateOffset = generator.getPeriodDateOffset();
        everyCountEdit.setText(Integer.toString(-periodDateOffset.getIntervalOffset()));
        everyIntervalChoice.setValue(periodDateOffset.getInterval());
    }

}
