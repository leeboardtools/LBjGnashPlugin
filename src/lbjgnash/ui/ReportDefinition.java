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

import com.leeboardtools.time.DatePeriods;
import com.leeboardtools.time.DateRange;
import com.leeboardtools.time.PeriodUtil;
import com.leeboardtools.util.CompositeObservable;
import com.leeboardtools.util.ResourceSource;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jgnash.engine.AccountGroup;
import jgnash.engine.AccountType;

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
    
    
    private final ObjectProperty<DatePeriods> dateSettings = new SimpleObjectProperty<>(this, "dateSettings", null);
    public final ObjectProperty<DatePeriods> dateSettingsProperty() {
        return dateSettings;
    }
    public final DatePeriods getDateSettings() {
        return dateSettings.get();
    }
    public final void setDateSettings(DatePeriods value) {
        dateSettings.set(value);
    }
    
    
    private final AccountFilter accountFilter = new AccountFilter();
    public final AccountFilter getAccountFilter() {
        return accountFilter;
    }
    
    
    protected void markModified() {
        fireInvalidationListeners();
    }
    
    
    public ReportDefinition() {
        title.addListener((property, oldValue, newValue) -> {
            markModified();
        });
        dateSettings.addListener((property, oldValue, newValue) -> {
            markModified();
        });
        accountFilter.addListener((change)-> {
            markModified();
        });
    }
    
    
    public void copyFrom(ReportDefinition other) {
        if (this != other) {
            this.setTitle(other.getTitle());
            this.setDateSettings(other.getDateSettings());
        }
    }
    
    
    public static enum Standard {
        NET_WORTH,
        INCOME_EXPENSE,
        PORTFOLIO,
    }
    
    public static ReportDefinition fromStandard(Standard standard) {
        switch (standard) {
            case NET_WORTH :
                return standardNetWorthDefintion();
                
            case INCOME_EXPENSE :
                return standardIncomeExpenseDefinition();
                
            case PORTFOLIO :
                return standardPortfolioDefinition();
                
            default :
                return null;
        }
    }
    
    public static ReportDefinition standardNetWorthDefintion() {
        ReportDefinition definition = new ReportDefinition();
        definition.setTitle(ResourceSource.getString("Report.Title.NetWorth"));
        
        definition.setDateSettings(new DatePeriods(PeriodUtil.fromStandard(PeriodUtil.Standard.YEAR, 0),
                0,
                null));
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.ASSET);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.LIABILITY);
        
        return definition;
    }
    
    public static ReportDefinition standardIncomeExpenseDefinition() {
        ReportDefinition definition = new ReportDefinition();
        definition.setTitle(ResourceSource.getString("Report.Title.IncomeExpense"));

        definition.setDateSettings(new DatePeriods(PeriodUtil.fromStandard(PeriodUtil.Standard.YEAR, 0),
                0,
                null,
                new DateRange.PreceedingMonths(12),
                null));
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.INCOME);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.EXPENSE);
        
        return definition;
    }
    
    public static ReportDefinition standardPortfolioDefinition() {
        ReportDefinition definition = new ReportDefinition();
        definition.setTitle(ResourceSource.getString("Report.Title.Portfolio"));

        definition.setDateSettings(new DatePeriods(PeriodUtil.fromStandard(PeriodUtil.Standard.YEAR, 0),
                0,
                null));
        definition.getAccountFilter().getAccountTypesToInclude().add(AccountType.CASH);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.INVEST);
        
        return definition;
    }
}
