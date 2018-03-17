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

import com.leeboardtools.json.JSONLite;
import com.leeboardtools.json.JSONObject;
import com.leeboardtools.json.JSONValue;
import com.leeboardtools.time.DateOffset;
import com.leeboardtools.time.PeriodicDateGenerator;
import com.leeboardtools.util.CompositeObservable;
import com.leeboardtools.util.EnumStringConverter;
import com.leeboardtools.util.ResourceSource;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import jgnash.engine.AccountGroup;

/**
 *
 * @author Albert Santos
 */
public class ReportDefinition extends CompositeObservable {
    
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    public final StringProperty titleProperty() {
        return title;
    }
    public final String getTitle() {
        return title.get();
    }
    public final void setTitle(String value) {
        title.set(value);
    }
    
    
    private final ObjectProperty<Style> style = new SimpleObjectProperty<>(this, "style", Style.CUSTOM);
    public final ObjectProperty<Style> styleProperty() {
        return style;
    }
    public final Style getStyle() {
        return style.get();
    }
    public final void setStyle(Style value) {
        style.set(value);
    }
    
    
    private final ObjectProperty<PeriodicDateGenerator> dateGenerator = new SimpleObjectProperty<>(this, "dateGenerator", null);
    public final ObjectProperty<PeriodicDateGenerator> dateGeneratorProperty() {
        return dateGenerator;
    }
    public final PeriodicDateGenerator getDateGenerator() {
        return dateGenerator.get();
    }
    public final void setDateGenerator(PeriodicDateGenerator value) {
        dateGenerator.set(value);
    }
    
    
    private final ObjectProperty<DateOffset.Basic> rangeDateOffset = new SimpleObjectProperty<>(this, "rangeDateOffset", null);
    public final ObjectProperty<DateOffset.Basic> rangeDateOffsetProperty() {
        return rangeDateOffset;
    }
    public final DateOffset.Basic getRangeDateOffset() {
        return rangeDateOffset.get();
    }
    public final void setRangeDateOffset(DateOffset.Basic value) {
        rangeDateOffset.set(value);
    }
    
    
    private final AccountFilter accountFilter = new AccountFilter();
    public final AccountFilter getAccountFilter() {
        return accountFilter;
    }
    
    
    private final ObservableList<ColumnType> columnTypes = FXCollections.observableArrayList();
    public final ObservableList<ColumnType> getColumnTypes() {
        return columnTypes;
    }
    
    
    private final StringProperty grandTotalText = new SimpleStringProperty(this, "grandTotalText");
    public final StringProperty grandTotalTextProperty() {
        return grandTotalText;
    }
    public final String getGrandTotalText() {
        return grandTotalText.get();
    }
    public final void setGrandTotalText(String text) {
        grandTotalText.set(text);
    }
    
    
    protected void markModified() {
        fireInvalidationListeners();
    }
    
    
    public ReportDefinition() {
        title.addListener((property, oldValue, newValue) -> {
            markModified();
        });
        style.addListener((property, oldValue, newValue) -> {
            markModified();
        });
        dateGenerator.addListener((property, oldValue, newValue) -> {
            markModified();
        });
        rangeDateOffset.addListener((property, oldValue, newValue) -> {
            markModified();
        });
        accountFilter.addListener((change)-> {
            markModified();
        });
        columnTypes.addListener((ListChangeListener.Change<? extends ColumnType> c) -> {
            markModified();
        });
        grandTotalText.addListener((property, oldValue, newValue) -> {
            markModified();
        });
    }
    
    
    public void copyFrom(ReportDefinition other) {
        if (this != other) {
            this.setTitle(other.getTitle());
            
            this.setStyle(other.getStyle());
            
            this.setDateGenerator(other.getDateGenerator());
            
            this.setRangeDateOffset(other.getRangeDateOffset());
            
            this.getAccountFilter().copyFrom(other.getAccountFilter());
            
            this.getColumnTypes().clear();
            this.getColumnTypes().addAll(other.getColumnTypes());
            
            this.setGrandTotalText(other.getGrandTotalText());
        }
    }
    
    
    public static enum Style {
        CUSTOM("ReportDefinition.Style.Custom", true),
        NET_WORTH("ReportDefinition.Style.NetWorth", false),
        INCOME_EXPENSE("ReportDefinition.Style.IncomeExpense", true),
        PORTFOLIO("ReportDefinition.Style.Portfolio", false),
        ;
        
        private final String stringResourceId;
        private final boolean usesRangeDateOffset;
        private Style(String stringResourceId, boolean usesRangeDateOffset) {
            this.stringResourceId = stringResourceId;
            this.usesRangeDateOffset = usesRangeDateOffset;
        }
        public final String getStringResourceId() {
            return this.stringResourceId;
        }
        public final boolean usesRangeDateOffset() {
            return this.usesRangeDateOffset;
        }
    }
    
    public static final StringConverter<Style> STYLE_STRING_CONVERTER = new EnumStringConverter<Style> () {
        @Override
        protected Style[] getEnumValues() {
            return Style.values();
        }

        @Override
        protected String getEnumStringResourceId(Style enumValue) {
            return enumValue.getStringResourceId();
        }
    };
    
    
    
    // TODO Columns to add:
    // annual rate of return
    // percent total portfolio
    
    public static enum ColumnType {
        VALUE("ReportDefinition.ColumnType.Value"),
        DELTA_PREVIOUS_PERIOD("ReportDefinition.ColumnType.DeltaPreviousPeriod"),
        DELTA_OLDEST_PERIOD("ReportDefinition.ColumnType.DeltaOldestPeriod"),
        PERCENT_DELTA_PREVIOUS_PERIOD("ReportDefinition.ColumnType.PercentDeltaPreviousPeriod"),
        PERCENT_DELTA_OLDEST_PERIOD("ReportDefinition.ColumnType.PercentDeltaOldesPeriod"),
        COST_BASIS("ReportDefinition.ColumnType.CostBasis"),
        GAIN("ReportDefinition.ColumnType.Gain"),
        PERCENT_GAIN("ReportDefinition.ColumnType.PercentGain"),
        QUANTITY("ReportDefinition.ColumnType.Quantity"),
        PRICE("ReportDefinition.ColumnType.Price"),
        PERCENT_PORTFOLIO("ReportDefinition.ColumnType.PercentPortfolio"),
        ANNUAL_RATE_OF_RETURN("ReportDefinition.ColumnType.AnnualRateOfReturn"),
        MARKET_VALUE("ReportDefinition.ColumnType.MarketValue"),
        ;
        
        private final String stringResourceId;
        private ColumnType(String stringResourceId) {
            this.stringResourceId = stringResourceId;
        }
        
        public final String getStringResourceId() {
            return stringResourceId;
        }
        
    }
    
    public static final StringConverter<ColumnType> COLUMN_TYPE_STRING_CONVERTER = new EnumStringConverter<ColumnType> () {
        @Override
        protected ColumnType[] getEnumValues() {
            return ColumnType.values();
        }

        @Override
        protected String getEnumStringResourceId(ColumnType enumValue) {
            return enumValue.getStringResourceId();
        }
    };
    
    
    public static ReportDefinition fromStyle(Style standard) {
        if (standard == null) {
            return new ReportDefinition();
        }
        
        switch (standard) {
            case NET_WORTH :
                return standardNetWorthDefintion();
                
            case INCOME_EXPENSE :
                return standardIncomeExpenseDefinition();
                
            case PORTFOLIO :
                return standardPortfolioDefinition();
                
            default :
                return new ReportDefinition();
        }
    }
    
    
    public static ReportDefinition standardNetWorthDefintion() {
        ReportDefinition definition = new ReportDefinition();
        definition.setTitle(ResourceSource.getString("Report.Title.NetWorth"));
        definition.setStyle(Style.NET_WORTH);
        
        definition.setDateGenerator(new PeriodicDateGenerator(DateOffset.SAME_DAY, DateOffset.END_OF_LAST_YEAR, 0));
        definition.setRangeDateOffset(null);

        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.ASSET);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.INVEST);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.SIMPLEINVEST);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.LIABILITY);

        definition.getColumnTypes().add(ColumnType.VALUE);
        
        definition.setGrandTotalText(ResourceSource.getString("Report.GrandTotal.NetWorth"));
        
        return definition;
    }
    
    
    public static ReportDefinition standardIncomeExpenseDefinition() {
        ReportDefinition definition = new ReportDefinition();
        definition.setTitle(ResourceSource.getString("Report.Title.IncomeExpense"));
        definition.setStyle(Style.INCOME_EXPENSE);
        
        definition.setDateGenerator(new PeriodicDateGenerator(DateOffset.SAME_DAY, DateOffset.END_OF_LAST_YEAR, 0));
        definition.setRangeDateOffset(new DateOffset.Basic(DateOffset.Interval.YEAR, 1, DateOffset.IntervalRelation.FIRST_DAY));

        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.INCOME);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.EXPENSE);

        definition.getColumnTypes().add(ColumnType.VALUE);
        
        definition.setGrandTotalText(ResourceSource.getString("Report.GrandTotal.IncomeExpense"));
        return definition;
    }
    
    
    public static ReportDefinition standardPortfolioDefinition() {
        ReportDefinition definition = new ReportDefinition();
        definition.setTitle(ResourceSource.getString("Report.Title.Portfolio"));
        definition.setStyle(Style.PORTFOLIO);

        definition.setDateGenerator(new PeriodicDateGenerator(DateOffset.SAME_DAY, DateOffset.END_OF_LAST_YEAR, 0));
        definition.setRangeDateOffset(null);

        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.INVEST);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.SIMPLEINVEST);
        
        definition.getColumnTypes().add(ColumnType.QUANTITY);
        definition.getColumnTypes().add(ColumnType.PRICE);
        definition.getColumnTypes().add(ColumnType.COST_BASIS);
        definition.getColumnTypes().add(ColumnType.MARKET_VALUE);
        definition.getColumnTypes().add(ColumnType.GAIN);
        definition.getColumnTypes().add(ColumnType.PERCENT_GAIN);
        definition.getColumnTypes().add(ColumnType.PERCENT_PORTFOLIO);
        definition.getColumnTypes().add(ColumnType.ANNUAL_RATE_OF_RETURN);

        definition.setGrandTotalText(ResourceSource.getString("Report.GrandTotal.Portfolio"));
        
        return definition;
    }
    
    
    public static JSONObject toJSONObject(ReportDefinition definition) {
        if (definition == null) {
            return null;
        }
        
        JSONObject jsonObject = JSONLite.newJSONObject();
        jsonObject.putClassName(ReportDefinition.class);
        jsonObject.add("title", definition.getTitle());
        jsonObject.add("style", definition.getStyle());
        jsonObject.add("dateGenerator", PeriodicDateGenerator.toJSONObject(definition.getDateGenerator()));
        jsonObject.add("rangeDateOffset", DateOffset.toJSONObject(definition.getRangeDateOffset()));
        jsonObject.add("accountFilter", AccountFilter.toJSONObject(definition.getAccountFilter()));
        jsonObject.add("columnTypes", JSONLite.toJSONValue(definition.getColumnTypes(), (item) -> {
            return new JSONValue(item);
        }));
        jsonObject.add("grandTotalText", definition.getGrandTotalText());
        return jsonObject;
    }
    
    public static ReportDefinition fromJSON(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        
        jsonObject.verifyClass(ReportDefinition.class);
        
        ReportDefinition definition = new ReportDefinition();
        jsonObject.callIfValue("title", (jsonValue) -> { definition.setTitle(jsonValue.getStringValue()); });
        jsonObject.callIfValue("style", (jsonValue) -> { definition.setStyle(jsonValue.getEnumValue(Style.values())); });
        jsonObject.callIfValue("dateGenerator", (jsonValue) -> { definition.setDateGenerator(PeriodicDateGenerator.fromJSON(jsonValue)); });
        jsonObject.callIfValue("rangeDateOffset", (jsonValue) -> { definition.setRangeDateOffset(DateOffset.basicFromJSON(jsonValue)); });
        
        AccountFilter filter = AccountFilter.fromJSON(jsonObject.getValue("accountFilter"));
        if (filter != null) {
            definition.getAccountFilter().copyFrom(filter);
        }
        
        JSONLite.fillFromJSONValue(jsonObject.getValue("columnTypes"), definition.getColumnTypes(), (jsonValue) -> {
            return jsonValue.getEnumValue(ColumnType.values());
        });
        
        jsonObject.callIfValue("grandTotalText", (jsonValue) -> { definition.setGrandTotalText(jsonValue.getStringValue()); });
        
        return definition;
    }
    
    public static ReportDefinition fromJSON(JSONValue jsonValue) {
        if (jsonValue == null) {
            return null;
        }
        return fromJSON(jsonValue.getObjectValue());
    }
}
