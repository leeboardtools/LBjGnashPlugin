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
import java.net.URL;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    private ChoiceBox<DateOffset.IntervalRelation> startEndChoice;
    @FXML
    private ChoiceBox<DateOffset.Interval> startEndPeriodChoice;
    @FXML
    private RadioButton offsetRadio;
    @FXML
    private TextField daysEdit;
    @FXML
    private ChoiceBox<DateOffset.IntervalRelation> offsetStartEndChoice;
    @FXML
    private ChoiceBox<DateOffset.Interval> offsetPeriodChoice;
    @FXML
    private RadioButton dayOfWeekRadio;
    @FXML
    private TextField dayOfWeekCountEdit;
    @FXML
    private ChoiceBox<DayOfWeek> dayOfWeekChoice;
    @FXML
    private ChoiceBox<DateOffset.IntervalRelation> dayOfWeekStartEndChoice;
    @FXML
    private ChoiceBox<DateOffset.Interval> dayOfWeekPeriodChoice;
    
    private StringConverter<DateOffset.IntervalRelation> startEndConverter = DateOffset.INTERVAL_RELATION_STRING_CONVERTER;
    private StringConverter<DateOffset.Interval> periodConverter = DateOffset.INTERVAL_STRING_CONVERTER;
    private StringConverter<DayOfWeek> dayOfWeekConverter = new DateUtil.DayOfWeekStringConverter(TextStyle.FULL);
    
    @FXML
    private ToggleGroup mainToggleGroup;

    private DateOffset.Basic dateOffset;
    private Stage stage;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupStartEndChoice(startEndChoice);
        setupPeriodChoice(startEndPeriodChoice);
        startEndChoice.focusedProperty().addListener((property, oldValue, newValue) -> {
            if (newValue) {
                startEndRadio.setSelected(true);
            }
        });
        startEndPeriodChoice.focusedProperty().addListener((property, oldValue, newValue) -> {
            if (newValue) {
                startEndRadio.setSelected(true);
            }
        });
        
        setupStartEndChoice(offsetStartEndChoice);
        setupPeriodChoice(offsetPeriodChoice);
        offsetStartEndChoice.focusedProperty().addListener((property, oldValue, newValue) -> {
            if (newValue) {
                offsetRadio.setSelected(true);
            }
        });
        offsetPeriodChoice.focusedProperty().addListener((property, oldValue, newValue) -> {
            if (newValue) {
                offsetRadio.setSelected(true);
            }
        });
        daysEdit.setOnKeyPressed((event) -> {
            offsetRadio.setSelected(true);
        });
        daysEdit.setText("0");
        
        setupStartEndChoice(dayOfWeekStartEndChoice);
        setupPeriodChoice(dayOfWeekPeriodChoice);
        setupDayOfWeekChoice(dayOfWeekChoice);
        dayOfWeekStartEndChoice.focusedProperty().addListener((property, oldValue, newValue) -> {
            if (newValue) {
                dayOfWeekRadio.setSelected(true);
            }
        });
        dayOfWeekPeriodChoice.focusedProperty().addListener((property, oldValue, newValue) -> {
            if (newValue) {
                dayOfWeekRadio.setSelected(true);
            }
        });
        dayOfWeekChoice.focusedProperty().addListener((property, oldValue, newValue) -> {
            if (newValue) {
                dayOfWeekRadio.setSelected(true);
            }
        });
        dayOfWeekCountEdit.setOnKeyTyped((event)-> {
            dayOfWeekRadio.setSelected(true);
        });
        dayOfWeekCountEdit.setText("1");
    }    
    
    void setupStartEndChoice(ChoiceBox<DateOffset.IntervalRelation> choiceBox) {
        choiceBox.getItems().addAll(
                DateOffset.IntervalRelation.FIRST_DAY, 
                DateOffset.IntervalRelation.LAST_DAY);
        choiceBox.setConverter(startEndConverter);
        choiceBox.setValue(DateOffset.IntervalRelation.FIRST_DAY);
    }
    
    void setupPeriodChoice(ChoiceBox<DateOffset.Interval> choiceBox) {
        choiceBox.getItems().addAll(
                DateOffset.Interval.YEAR, 
                DateOffset.Interval.QUARTER,
                DateOffset.Interval.MONTH,
                DateOffset.Interval.WEEK);
        choiceBox.setConverter(periodConverter);
        choiceBox.setValue(DateOffset.Interval.YEAR);
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
        choiceBox.setValue(DayOfWeek.MONDAY);
    }
    
    public void setupController(DateOffset.Basic dateOffset, Stage stage) {
        if (dateOffset == null) {
            dateOffset = new DateOffset.Basic(DateOffset.Interval.MONTH, 0, DateOffset.IntervalRelation.LAST_DAY);
        }
        
        this.dateOffset = dateOffset;
        this.stage = stage;
        
        DateOffset.SubIntervalOffset subIntervalOffset = dateOffset.getSubIntervalOffset();
        if (subIntervalOffset instanceof DateOffset.NthDayOfWeekOffset) {
            setupFromNthDayOfWeek();
        }
        else if (subIntervalOffset instanceof DateOffset.DayOffset) {
            setupFromDayOffset();
        }
        else {
            setupFromInterval();
        }
    }
    
    private void setupFromNthDayOfWeek() {
        DateOffset.NthDayOfWeekOffset subIntervalOffset = (DateOffset.NthDayOfWeekOffset)dateOffset.getSubIntervalOffset();
        dayOfWeekRadio.setSelected(true);
        dayOfWeekCountEdit.setText(Integer.toString(subIntervalOffset.getOccurrence()));
        dayOfWeekChoice.setValue(subIntervalOffset.getDayOfWeek());
    }
    
    private void setupFromDayOffset() {
        DateOffset.DayOffset subIntervalOffset = (DateOffset.DayOffset)dateOffset.getSubIntervalOffset();
        int dayCount = subIntervalOffset.getDayCount();
        if (dayCount != 0) {
            offsetRadio.setSelected(true);
            daysEdit.setText(Integer.toString(dayCount));
            offsetStartEndChoice.setValue(dateOffset.getIntervalRelation());
            offsetPeriodChoice.setValue(dateOffset.getInterval());
        }
        else {
            setupFromInterval();
        }
    }
    
    private void setupFromInterval() {
        startEndRadio.setSelected(true);
        startEndChoice.setValue(dateOffset.getIntervalRelation());
        startEndPeriodChoice.setValue(dateOffset.getInterval());
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
    
    public DateOffset.Basic getRangeDateOffset() {
        if (startEndRadio.isSelected()) {
            DateOffset.IntervalRelation intervalRelation = startEndChoice.getValue();
            DateOffset.Interval interval = startEndPeriodChoice.getValue();
            return new DateOffset.Basic(interval, 0, intervalRelation);
        }
        else if (offsetRadio.isSelected()) {
            int count = Integer.parseInt(daysEdit.getText());
            DateOffset.IntervalRelation intervalRelation = offsetStartEndChoice.getValue();
            DateOffset.Interval interval = offsetPeriodChoice.getValue();
            return new DateOffset.Basic(interval, 0, intervalRelation, new DateOffset.DayOffset(count), null);
        }
        else if (dayOfWeekRadio.isSelected()) {
            int count = Integer.parseInt(dayOfWeekCountEdit.getText());
            DayOfWeek dayOfWeek = dayOfWeekChoice.getValue();
            DateOffset.IntervalRelation intervalRelation = dayOfWeekStartEndChoice.getValue();
            DateOffset.Interval interval = dayOfWeekPeriodChoice.getValue();
            return new DateOffset.Basic(interval, 0, intervalRelation, new DateOffset.NthDayOfWeekOffset(dayOfWeek, count), null);
        }
        return null;
    }

}
