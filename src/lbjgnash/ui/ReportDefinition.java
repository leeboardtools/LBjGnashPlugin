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
    
    
    private final ObjectProperty<DateOffset> rangeDateOffset = new SimpleObjectProperty<>(this, "rangeDateOffset", null);
    public final ObjectProperty<DateOffset> rangeDateOffsetProperty() {
        return rangeDateOffset;
    }
    public final DateOffset getRangeDateOffset() {
        return rangeDateOffset.get();
    }
    public final void setRangeDateOffset(DateOffset value) {
        rangeDateOffset.set(value);
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
        dateGenerator.addListener((property, oldValue, newValue) -> {
            markModified();
        });
        accountFilter.addListener((change)-> {
            markModified();
        });
    }
    
    
    public void copyFrom(ReportDefinition other) {
        if (this != other) {
            this.setTitle(other.getTitle());
            this.setDateGenerator(other.getDateGenerator());
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
        
        definition.setDateGenerator(new PeriodicDateGenerator(DateOffset.SAME_DAY, DateOffset.END_OF_LAST_YEAR, 0));
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.ASSET);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.LIABILITY);
        
        return definition;
    }
    
    public static ReportDefinition standardIncomeExpenseDefinition() {
        ReportDefinition definition = new ReportDefinition();
        definition.setTitle(ResourceSource.getString("Report.Title.IncomeExpense"));

        
        definition.setDateGenerator(new PeriodicDateGenerator(DateOffset.SAME_DAY, DateOffset.END_OF_LAST_YEAR, 0));
        definition.setRangeDateOffset(new DateOffset.Basic(DateOffset.Interval.YEAR, 1, DateOffset.IntervalRelation.FIRST_DAY));
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.INCOME);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.EXPENSE);
        
        return definition;
    }
    
    public static ReportDefinition standardPortfolioDefinition() {
        ReportDefinition definition = new ReportDefinition();
        definition.setTitle(ResourceSource.getString("Report.Title.Portfolio"));

        definition.setDateGenerator(new PeriodicDateGenerator(DateOffset.SAME_DAY, DateOffset.END_OF_LAST_YEAR, 0));
        definition.getAccountFilter().getAccountTypesToInclude().add(AccountType.CASH);
        definition.getAccountFilter().getAccountGroupsToInclude().add(AccountGroup.INVEST);
        
        return definition;
    }
}
