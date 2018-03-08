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
package lbjgnash.ui.reportview;

import com.leeboardtools.util.ResourceSource;
import com.leeboardtools.util.StringUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.Engine;
import lbjgnash.ui.AccountFilter;
import lbjgnash.ui.ReportDefinition;

/**
 * This is the guts of the control used to display the report's data.
 * @author Albert Santos
 */
public class ReportDataView {
    private final TreeTableView<RowEntry> treeTableView;
    private ReportDefinition definition;
    private Engine engine;
    
    // TODO: Move this to ReportDefinition.
    protected DateTimeFormatter columnDateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    protected String percentSuffix = "%";
    
    protected TreeTableColumn<RowEntry, String> headingColumn;
    
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    protected ReportOutput currentReportOutput;
    
    public static final String STYLE_CELL       = "report-cell";
    public static final String STYLE_SUBTOTAL   = "report-cell-subtotal";
    public static final String STYLE_SUMMARY    = "report-cell-summary";
    public static final String STYLE_BALANCE_VALUE      = "report-cell-balance-value";
    public static final String STYLE_GRAND_TOTAL    = "report-cell-grand-total";

    
    
    /**
     * Used as the class for the cells of the {@link TreeTableView} so we can
     * associate data accessible to individual {@link Cell}s.
     */
    protected static class CellEntry {
        final RowEntry rowEntry;
        final String value;
        
        protected CellEntry(RowEntry rowEntry, String value) {
            this.rowEntry = rowEntry;
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }        
    }
    
    /**
     * This an individual row for the {@link TreeTableView}.
     */
    protected static class RowEntry {
        final StringProperty rowTitle = new SimpleStringProperty(this, "rowTitle", null);
        final List<ObjectProperty<CellEntry>> expandedColumnCellProperties = new ArrayList<>();
        final List<ObjectProperty<CellEntry>> nonExpandedColumnCellProperties = new ArrayList<>();

        protected void setRowTitle(String title) {
            this.rowTitle.set(title);
        }
        protected StringProperty getRowTitle() {
            return this.rowTitle;
        }
        
        protected ObjectProperty<CellEntry> getColumnCellProperties(int index, TreeItem<RowEntry> entry) {
            List<ObjectProperty<CellEntry>> propertiesList = (entry.isExpanded()) 
                ? expandedColumnCellProperties
                : nonExpandedColumnCellProperties;
            if (index >= propertiesList.size()) {
                return null;
            }
            return propertiesList.get(index);
        }
        
        protected void setExpandedColumnCellValue(ColumnEntry columnEntry, CellEntry cellEntry) {
            setColumnCellValue(expandedColumnCellProperties, columnEntry.columnIndex, cellEntry);
        }
        protected void setNonExpandedColumnCellValue(ColumnEntry columnEntry, CellEntry cellEntry) {
            setColumnCellValue(nonExpandedColumnCellProperties, columnEntry.columnIndex, cellEntry);
        }
        
        protected void setColumnCellValue(List<ObjectProperty<CellEntry>> properties, int index, CellEntry cellEntry) {
            while (index >= properties.size()) {
                properties.add(null);
            }
            
            ObjectProperty<CellEntry> property = properties.get(index);
            if (property == null) {
                property = new SimpleObjectProperty();
                properties.set(index, property);
            }
            
            property.set(cellEntry);
        }
    }
    
    
    /**
     * This represents an individual {@link TreeTableColumn}.
     */
    protected static class ColumnEntry {
        protected TreeTableColumn<RowEntry, CellEntry> treeTableColumn = new TreeTableColumn<>();
        
        // This is set as the column entries of the DateEntries are added
        // to the ReportOutput's columnEntries list.
        int columnIndex;
        
        protected ColumnEntry() {
            treeTableColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RowEntry, CellEntry> param) -> {
                return param.getValue().getValue().getColumnCellProperties(columnIndex, param.getValue());
            });
        }
        
        protected void setExpandedRowValue(RowEntry rowEntry, CellEntry cellEntry) {
            rowEntry.setExpandedColumnCellValue(this, cellEntry);
        }
        
        protected void setNonExpandedRowValue(RowEntry rowEntry, CellEntry cellEntry) {
            rowEntry.setNonExpandedColumnCellValue(this, cellEntry);
        }
    }
    
    
    /**
     * This represents the full output of one report.
     */
    protected class ReportOutput {
        final List<AccountEntry> accountEntries = new ArrayList<>();
        final List<DateEntry> dateEntries = new ArrayList<>();
        final List<ColumnGenerator> columnGenerators = new ArrayList<>();
        
        // This list is built up from the column entries of the dateEntries.
        final List<ColumnEntry> columnEntries = new ArrayList<>();

        RowEntry grandTotalRowEntry;

        public String toMonetaryValueString(BigDecimal value, Account account) {
            return value.setScale(2).toPlainString();
        }

        public String toPercentString(BigDecimal numerator, BigDecimal denominator) {
            numerator = numerator.multiply(ReportDataView.ONE_HUNDRED).setScale(1);
            try {
                BigDecimal value = numerator.divide(denominator, RoundingMode.HALF_UP);
                return value.toPlainString() + percentSuffix;
            } catch (ArithmeticException ex) {
                return "-";
            }
        }
    }
    
    
    protected static enum AccountRowEntry {
        PRE_ACCOUNT,
        PRE_CHILD_ACCOUNT,
        POST_CHILD_ACCOUNT,
        POST_ACCOUNT,
    }
    

    /**
     * Encapsulates a single account, with a list of all the child accounts.
     */
    protected static class AccountEntry {
        final Account account;
        final int accountDepth;
        final List<AccountEntry> childAccountEntries = new ArrayList<>();
        final boolean isIncluded;
        final int includeDepth;
        RowEntry summaryRowEntry;
        final RowEntry [] accountRowEntries = new RowEntry[AccountRowEntry.values().length];
        
        AccountSecuritiesTracker accountSecuritiesTracker;
        
        protected AccountEntry(Account account, boolean isIncluded, AccountEntry parentAccountEntry) {
            this.account = account;
            this.accountDepth = account.getDepth();
            this.isIncluded = isIncluded;

            int myIncludeDepth = (parentAccountEntry != null) ? parentAccountEntry.includeDepth : 0;
            if (isIncluded) {
                ++myIncludeDepth;
                summaryRowEntry = new RowEntry();
                summaryRowEntry.setRowTitle(account.getName());
            }
            
            this.includeDepth = myIncludeDepth;
        }
        
        /**
         * This retrieves the {@link RowEntry} for a given {@link AccountRowEntry}, if
         * no one has called {@link #useAccountRowEntry(lbjgnash.ui.ReportDataView.AccountRowEntry) } for
         * it then <code>null</code> is returned.
         * @param accountRowEntry   The row entry of interest.
         * @return The row entry, <code>null</code> if it is not in use.
         */
        protected final RowEntry getAccountRowEntry(AccountRowEntry accountRowEntry) {
            return accountRowEntries[accountRowEntry.ordinal()];
        }
        
        /**
         * This retrieves a {@link RowEntry} for a given {@link AccountRowEntry}, allocating
         * the row entry if necessary.
         * @param accountRowEntry   The row entry of interest.
         * @return The row entry.
         */
        protected final RowEntry useAccountRowEntry(AccountRowEntry accountRowEntry) {
            if (accountRowEntries[accountRowEntry.ordinal()] == null) {
                RowEntry rowEntry = new RowEntry();
                accountRowEntries[accountRowEntry.ordinal()] = rowEntry;
                if ((accountRowEntry == AccountRowEntry.PRE_CHILD_ACCOUNT) 
                    || (accountRowEntry == AccountRowEntry.POST_CHILD_ACCOUNT)) {
                    rowEntry.setRowTitle(account.getName());
                }
            }
            return accountRowEntries[accountRowEntry.ordinal()];
        }
        
        protected final boolean isAnyChildAccountIncluded() {
            for (AccountEntry accountEntry : childAccountEntries) {
                if (accountEntry.isIncluded) {
                    return true;
                }
            }
            
            for (AccountEntry accountEntry : childAccountEntries) {
                if (accountEntry.isAnyChildAccountIncluded()) {
                    return true;
                }
            }
            return false;
        }
        

        protected final int getDeepestIncludedChildAccountDepth() {
            int deepestDepth = -1;
            for (AccountEntry accountEntry : childAccountEntries) {
                int depth = accountEntry.getDeepestIncludedChildAccountDepth();
                if (depth > deepestDepth) {
                    deepestDepth = depth;
                }
                else if (accountEntry.isIncluded) {
                    depth = accountEntry.accountDepth;
                    if (depth > deepestDepth) {
                        deepestDepth = depth;
                    }
                }
            }
            
            return deepestDepth;
        }
        
        protected final AccountSecuritiesTracker getAccountSecuritiesTracker() {
            if (accountSecuritiesTracker == null) {
                accountSecuritiesTracker = AccountSecuritiesTracker.createForAccount(account);
            }
            return accountSecuritiesTracker;
        }
    }
    
    
    /**
     * This manages the report column data for a specific date (which was generated by
     * the report's date generator.
     */
    protected static class DateEntry implements Comparable<DateEntry> {
        final LocalDate startDate;
        final LocalDate endDate;
        final int index;
        final List<ColumnEntry> columnEntries = new ArrayList<>();
        
        protected DateEntry(LocalDate dateStart, LocalDate dateEnd, int index) {
            this.startDate = dateStart;
            this.endDate = dateEnd;
            this.index = index;
        }

        protected ColumnEntry getColumnEntryAtIndex(int index) {
            while (index >= columnEntries.size()) {
                columnEntries.add(new ColumnEntry());
            }
            
            return columnEntries.get(index);
        }

        @Override
        public int compareTo(DateEntry o) {
            return endDate.compareTo(o.endDate);
        }
        
    }
    
    
    /**
     * The base abstract class for the objects responsible for generating {@link ColumnEntry}
     * objects.
     */
    protected static abstract class ColumnGenerator {
        
        protected void setupAccountEntryRows(ReportOutput reportOutput) {
            reportOutput.accountEntries.forEach((accountEntry) -> {
                setupAccountEntryRows(accountEntry, reportOutput);
            });
        }
        
        /**
         * This is normally overridden to handle the actual request for row entries for an account entry.
         * This implementation should normally be called from the overridden method because
         * this calls itself for all the child account entries.
         * @param accountEntry  The account entry to process.
         * @param reportOutput The report output.
         */
        protected void setupAccountEntryRows(AccountEntry accountEntry, ReportOutput reportOutput) {
            accountEntry.childAccountEntries.forEach((childAccountEntry) -> {
                setupAccountEntryRows(childAccountEntry, reportOutput);
            });
        }
        
        
        protected void setupDateEntryColumns(ReportOutput reportOutput) {
            reportOutput.dateEntries.forEach((dateEntry) -> {
                final int columnIndexBase = dateEntry.columnEntries.size();
                reportOutput.accountEntries.forEach((accountEntry) -> {
                    setupDateEntryColumns(dateEntry, accountEntry, reportOutput, columnIndexBase);
                });
            });
        }
        
        /**
         * This is normally overridden to handle the actual processing of an account entry for
         * a date entry. This implementation should normally be called from the overridden method
         * because it calls itself for all the child account entries.
         * @param dateEntry The date entry being processed.
         * @param accountEntry  The account entry this is for.
         * @param reportOutput  The report output this is for.
         * @param columnIndexBase   The index to add to any column entry requests.
         */
        protected void setupDateEntryColumns(DateEntry dateEntry, AccountEntry accountEntry, ReportOutput reportOutput, int columnIndexBase) {
            accountEntry.childAccountEntries.forEach((childAccountEntry)-> {
                setupDateEntryColumns(dateEntry, childAccountEntry, reportOutput, columnIndexBase);
            });
        }
        
        
        protected void updateCellValues(ReportOutput reportOutput) {
            reportOutput.dateEntries.forEach((dateEntry) -> {
                if (reportOutput.grandTotalRowEntry != null) {
                    updateDateEntryCellValues(dateEntry, reportOutput);
                    updateGrandTotalCellValue(dateEntry, reportOutput);
                }
            });
        }
        
        protected void updateDateEntryCellValues(DateEntry dateEntry, ReportOutput reportOutput) {
            reportOutput.accountEntries.forEach((accountEntry) -> {
                updateDateEntryCellValues(dateEntry, accountEntry, reportOutput);
            });
        }

        /**
         * This is normally overridden to handle the actual setting of the cell for a given
         * date entry and account entry. This implementation should normally be called from
         * the overridden method because it calls itself for all the child account entries.
         * @param dateEntry The date entry being processed.
         * @param accountEntry  The account entry this is for.
         * @param reportOutput The report output this is for.
         */
        protected void updateDateEntryCellValues(DateEntry dateEntry, AccountEntry accountEntry, ReportOutput reportOutput) {
            accountEntry.childAccountEntries.forEach((childAccountEntry)-> {
                updateDateEntryCellValues(dateEntry, childAccountEntry, reportOutput);
            });
        }
        
        
        protected void updateGrandTotalCellValue(DateEntry dateEntry, ReportOutput reportOutput) {
        }
    }
    

    public static enum ReferencePeriodType {
        PREVIOUS,
        OLDEST,
        NEWEST,
    }
    
    
    

    
    public ReportDataView() {
        treeTableView = new TreeTableView<>();
        treeTableView.setShowRoot(false);
        
        headingColumn = new TreeTableColumn<>();
        headingColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RowEntry, String> param) -> {
            return param.getValue().getValue().getRowTitle();
        });
    }
    

    public void setupView(ReportDefinition definition, Engine engine) {
        this.definition = definition;
        this.engine = engine;
    }
    

    public void shutDownView() {
        this.treeTableView.getRoot().getChildren().clear();
        this.treeTableView.getColumns().clear();
        
        this.definition = null;
        this.engine = null;
    }
    

    public final Control getControl() {
        return treeTableView;
    }
    
    

    public void refreshFromReportDefinition() {
        refreshFromEngine();
    }
    

    public void refreshFromEngine() {
        ReportOutput updatedReportOutput = new ReportOutput();
        updateReportOutput(updatedReportOutput);
        
        updateTreeTableView(updatedReportOutput);
    }
    
    
    protected void updateTreeTableView(ReportOutput reportOutput) {
        TreeItem<RowEntry> root = new TreeItem<>(new RowEntry());
        
        treeTableView.setRoot(root);
        treeTableView.getColumns().clear();
        
        
        treeTableView.getColumns().add(headingColumn);
        
        List<TreeTableColumn<RowEntry, CellEntry>> columnGroup = new ArrayList<>();
        reportOutput.dateEntries.forEach((dateEntry) -> {
            columnGroup.clear();
            dateEntry.columnEntries.forEach((columnEntry) -> {
                if (columnEntry.treeTableColumn != null) {
                    columnGroup.add(columnEntry.treeTableColumn);
                }
            });
            
            TreeTableColumn<RowEntry, CellEntry> dateColumn;
            if (columnGroup.size() == 1) {
                // Single column, it's going to be the date column.
                dateColumn = columnGroup.get(0);
            }
            else {
                dateColumn = new TreeTableColumn<>();
                dateColumn.getColumns().addAll(columnGroup);
            }
            
            dateColumn.setText(getDateColumnLabel(dateEntry));
            
            treeTableView.getColumns().add(dateColumn);
        });
        
        
        // Add the rows...
        reportOutput.accountEntries.forEach((accountEntry) -> {
            addRowsForAccountEntry(root, accountEntry);
        });
        
        if (reportOutput.grandTotalRowEntry != null) {
            TreeItem<RowEntry> treeItem = new TreeItem<>(reportOutput.grandTotalRowEntry);
            root.getChildren().add(treeItem);
        }
        
        this.currentReportOutput = reportOutput;
    }
    
    protected void addRowsForAccountEntry(TreeItem<RowEntry> parent, AccountEntry accountEntry) {
        if (accountEntry.summaryRowEntry != null) {
            TreeItem<RowEntry> treeItem = new TreeItem<>(accountEntry.summaryRowEntry);
            parent.getChildren().add(treeItem);
            parent = treeItem;
        }
        
        addRowEntryIfNotNull(parent, accountEntry, AccountRowEntry.PRE_ACCOUNT);
        addRowEntryIfNotNull(parent, accountEntry, AccountRowEntry.PRE_CHILD_ACCOUNT);
        
        final TreeItem<RowEntry> itemForChildren = parent;
        accountEntry.childAccountEntries.forEach((childAccountEntry) -> {
            addRowsForAccountEntry(itemForChildren, childAccountEntry);
        });

        addRowEntryIfNotNull(parent, accountEntry, AccountRowEntry.POST_CHILD_ACCOUNT);
        addRowEntryIfNotNull(parent, accountEntry, AccountRowEntry.POST_ACCOUNT);
    }
    
    protected void addRowEntryIfNotNull(TreeItem<RowEntry> parent, AccountEntry accountEntry, AccountRowEntry accountRowEntry) {
        RowEntry rowEntry = accountEntry.getAccountRowEntry(accountRowEntry);
        if (rowEntry != null) {
            parent.getChildren().add(new TreeItem<>(rowEntry));
        }
    }
    
    protected String getDateColumnLabel(DateEntry dateEntry) {
        String endDateString = dateEntry.endDate.format(columnDateTimeFormatter);
        if (dateEntry.startDate.equals(dateEntry.endDate)) {
            return endDateString;
        }

        String startDateString = dateEntry.startDate.format(columnDateTimeFormatter);
        return ResourceSource.getString("Report.ColumnHeading.StartEndDate", startDateString, endDateString);
    }

    
    protected void updateReportOutput(ReportOutput reportOutput) {
        createAccountEntries(reportOutput);
        createDateEntries(reportOutput);
        createColumnGenerators(reportOutput);
        
        processAccountEntries(reportOutput);
        processDateEntries(reportOutput);
    }
    
    
    protected void createAccountEntries(ReportOutput reportOutput) {
        final AccountFilter filter = (this.definition.getAccountFilter() != null) ? this.definition.getAccountFilter() : new AccountFilter();
        
        Map<AccountGroup, SortedSet<Account>> accountsByGroup = new HashMap<>();
        engine.getRootAccount().getChildren().forEach((account) -> {
            AccountGroup accountGroup = account.getAccountType().getAccountGroup();
            SortedSet<Account> accounts = accountsByGroup.get(accountGroup);
            if (accounts == null) {
                accounts = new TreeSet<>((Account o1, Account o2) -> o1.getPathName().compareTo(o2.getPathName()));
                accountsByGroup.put(accountGroup, accounts);
            }
            accounts.add(account);
        });
        
        processAccountEntries(AccountGroup.ASSET, accountsByGroup, filter, reportOutput);
        processAccountEntries(AccountGroup.INVEST, accountsByGroup, filter, reportOutput);
        processAccountEntries(AccountGroup.SIMPLEINVEST, accountsByGroup, filter, reportOutput);
        processAccountEntries(AccountGroup.LIABILITY, accountsByGroup, filter, reportOutput);
        processAccountEntries(AccountGroup.INCOME, accountsByGroup, filter, reportOutput);
        processAccountEntries(AccountGroup.EXPENSE, accountsByGroup, filter, reportOutput);
        
        // Just in case new account groups have been added.
        while (!accountsByGroup.isEmpty()) {
            AccountGroup accountGroup = accountsByGroup.keySet().iterator().next();
            processAccountEntries(accountGroup, accountsByGroup, filter, reportOutput);
        }
        
        String grandTotalText = this.definition.getGrandTotalText();
        if (StringUtil.isNonEmpty(grandTotalText)) {
            reportOutput.grandTotalRowEntry = new RowEntry();
            reportOutput.grandTotalRowEntry.setRowTitle(grandTotalText);
        }
    }
    
    
    protected void processAccountEntries(AccountGroup accountGroup, Map<AccountGroup, SortedSet<Account>> accountsByGroup, 
            AccountFilter filter, ReportOutput reportOutput) {
        SortedSet<Account> accounts = accountsByGroup.get(accountGroup);
        if (accounts == null) {
            return;
        }
        accountsByGroup.remove(accountGroup);
        
        accounts.forEach((account) -> {
            boolean isIncluded = filter.isIncludeAccount(account);
            AccountEntry accountEntry = new AccountEntry(account, isIncluded, null);
            reportOutput.accountEntries.add(accountEntry);
            
            addChildAccountEntries(accountEntry, filter, reportOutput);
        });
    }
    
    
    protected void addChildAccountEntries(AccountEntry accountEntry, AccountFilter filter, ReportOutput reportOutput) {
        accountEntry.account.getChildren().forEach((account) -> {
            boolean isIncluded = filter.isIncludeAccount(account);
            AccountEntry childAccountEntry = new AccountEntry(account, isIncluded, accountEntry);
            accountEntry.childAccountEntries.add(childAccountEntry);
            
            addChildAccountEntries(childAccountEntry, filter, reportOutput);
        });
    }
    
    protected void createDateEntries(ReportOutput reportOutput) {
        TreeSet<LocalDate> sortedDates = new TreeSet<>();
        this.definition.getDateGenerator().getPeriodicDates(LocalDate.now(), sortedDates);
        Iterator<LocalDate> dateIterator = sortedDates.iterator();
        while (dateIterator.hasNext()) {
            LocalDate endDate = dateIterator.next();
            LocalDate startDate = endDate;
            if (this.definition.getRangeDateOffset() != null) {
                startDate = this.definition.getRangeDateOffset().getOffsetDate(endDate);
            }
            
            DateEntry dateEntry = new DateEntry(startDate, endDate, reportOutput.dateEntries.size());
            reportOutput.dateEntries.add(dateEntry);
        }
    }
    
    
    protected void createColumnGenerators(ReportOutput reportOutput) {
        this.definition.getColumnTypes().forEach((columnType) -> {
            ColumnGenerator generator = columnGeneratorFromColumnType(columnType);
            if (generator != null) {
                reportOutput.columnGenerators.add(generator);
            }
        });
    }
    

    protected ColumnGenerator columnGeneratorFromColumnType(ReportDefinition.ColumnType columnType) {
        switch (columnType) {
            case VALUE:
                return new ValueColumnGenerator();
                
            case DELTA_PREVIOUS_PERIOD:
                return new DeltaPeriodColumnGenerator(ReferencePeriodType.PREVIOUS);
                
            case DELTA_OLDEST_PERIOD:
                return new DeltaPeriodColumnGenerator(ReferencePeriodType.OLDEST);
                
            case PERCENT_DELTA_PREVIOUS_PERIOD:
                return new PercentDeltaPeriodColumnGenerator(ReferencePeriodType.PREVIOUS);
                
            case PERCENT_DELTA_OLDEST_PERIOD:
                return new PercentDeltaPeriodColumnGenerator(ReferencePeriodType.OLDEST);
                
            case COST_BASIS:
                break;
                
            case GAIN:
                break;
                
            case QUANTITY:
                break;
                
            case PRICE:
                break;
                
            case PERCENT_PORTFOLIO :
                
            default:
                throw new AssertionError(columnType.name());
            
        }
        return null;
    }

    //
    // COST_BASIS:
    // GAIN:
    // QUANTITY:
    // PRICE:
    //
    // They are all based upon an investment account.
    // For each investement account they will have rows listing
    // the individual securities.
    // At the end, have sub-totals for the different columns.
    //
    
    private void processAccountEntries(ReportOutput reportOutput) {
        reportOutput.columnGenerators.forEach((generator) -> {
            generator.setupAccountEntryRows(reportOutput);
        });
    }

    
    private void processDateEntries(ReportOutput reportOutput) {
        reportOutput.columnEntries.clear();

        reportOutput.columnGenerators.forEach((generator) -> {
            generator.setupDateEntryColumns(reportOutput);
        });
        
        reportOutput.dateEntries.forEach((dateEntry)-> {
            dateEntry.columnEntries.forEach((columnEntry) -> {
                columnEntry.columnIndex = reportOutput.columnEntries.size();
                reportOutput.columnEntries.add(columnEntry);
            });
        });
        
        reportOutput.columnGenerators.forEach((generator) -> {
            generator.updateCellValues(reportOutput);
        });
    }
}
