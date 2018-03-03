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

import com.leeboardtools.util.ResourceSource;
import com.leeboardtools.util.Similarable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.Engine;

/**
 * This is the guts of the control used to display the report's data.
 * @author Albert Santos
 */
public class ReportDataView {
    private final TableView<RowCellValues> tableView;
    private ReportDefinition definition;
    private Engine engine;
    
    // TODO: Move this to ReportDefinition.
    protected DateTimeFormatter columnDateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    
    protected TableColumn<RowCellValues, String> headingColumn;
    
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    protected ReportOutput currentReportOutput;

    //
    // TODO
    // Gotta figure out how to handle the investment account settings.
    // If we're set up with 1 to 1 security to account, then we're sort of set as far as rows go.
    // However, we still need to be able to figure out the cost basis and quantities.
    // So we need a filter for investment transactions for a specific security.
    // This will track the shares and cost basis. Would be nice to also be able to
    // track lots for cost basis purposes.
    // SecurityTransactionTracker...
    //      Track lots, cost basis.
    //      Provide summary information.
    //
    // For the Report purposes, for each account, build a list of the securities.
    // If the securities are not 1 to 1 then we'll add extra rows, otherwise we'll
    // just use the base account row.
    // If we're adding extra rows, the first row is the account, and it will not have
    // any column values.
    // The subsequent rows will be the individual securities, with their appropriate
    // information.
    // 
    
    
    
    
    /**
     * This holds the value properties for the cell values of the {@link TableView}.
     */
    protected class RowCellValues {
        private final ArrayList<StringProperty> columnValues = new ArrayList<>();
        private final StringProperty headingText = new SimpleStringProperty(this, "headingText");
        
        protected RowCellValues(String headingText) {
            this.headingText.set(headingText);
        }
        
        protected final StringProperty getColumnValueProperty(int index) {
            if (index >= columnValues.size()) {
                return null;
            }
            return columnValues.get(index);
        }
        
        protected final void setColumnValue(int index, String value) {
            while (index >= columnValues.size()) {
                columnValues.add(null);
            }
            StringProperty property = columnValues.get(index);
            if (property == null) {
                property = new SimpleStringProperty(this, "column" + index, value);
                columnValues.set(index, property);
            }
            else {
                columnValues.get(index).set(value);
            }
        }
    }
    
    /**
     * This represents an individual {@link TableColumn}.
     */
    protected class ColumnEntry implements Similarable<ColumnEntry> {
        protected TableColumn<RowCellValues, String> tableColumn = new TableColumn<>();
        protected final List<String> rowValues = new ArrayList<>();
        
        // This is set as the column entries of the DateEntries are added
        // to the ReportOutput's columnEntries list.
        int columnIndex;
        
        protected ColumnEntry() {
            tableColumn.setCellValueFactory((TableColumn.CellDataFeatures<RowCellValues, String> param) -> {
                return param.getValue().getColumnValueProperty(columnIndex);
            });
        }

        @Override
        public boolean isSimilar(ColumnEntry other) {
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            if (columnIndex != other.columnIndex) {
                return false;
            }
            return true;
        }
        
        
        protected void setRowValue(RowEntry rowEntry, String value) {
            while (rowEntry.rowIndex >= rowValues.size()) {
                rowValues.add(null);
            }
            
            rowValues.set(rowEntry.rowIndex, value);
        }
    }
    
    
    /**
     * This is used to manage individual rows within a {@link ReportOutput}.
     * It's separate from {@link RowCellValues} so we can generate this separately
     * and then re-use the row cell values as needed.
     */
    protected class RowEntry implements Similarable<RowEntry> {
        // This is set as the row entries of the AccountEntries are added to the
        // ReportOtuput's rowEntries list. This happens before the column generators
        // are called for the DateEntries.
        int rowIndex;
        
        String rowTitle;

        @Override
        public boolean isSimilar(RowEntry other) {
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            return true;
        }
    }
    
    
    /**
     * This represents the full output of one report.
     */
    protected class ReportOutput implements Similarable<ReportOutput> {
        final List<AccountEntry> accountEntries = new ArrayList<>();
        final List<DateEntry> dateEntries = new ArrayList<>();
        final List<ColumnGenerator> columnGenerators = new ArrayList<>();
        
        // This list is built up from the row enries of the accountEntries.
        final List<RowEntry> rowEntries = new ArrayList<>();
        
        // This list is built up from the column entries of the dateEntries.
        final List<ColumnEntry> columnEntries = new ArrayList<>();
        

        @Override
        public boolean isSimilar(ReportOutput other) {
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            if (!Similarable.areSimilar(accountEntries, other.accountEntries)) {
                return false;
            }
            if (!Similarable.areSimilar(dateEntries, other.dateEntries)) {
                return false;
            }
            if (!Similarable.areSimilar(columnGenerators, other.columnGenerators)) {
                return false;
            }
            if (!Similarable.areSimilar(columnEntries, other.columnEntries)) {
                return false;
            }
            if (!Similarable.areSimilar(rowEntries, other.rowEntries)) {
                return false;
            }
            
            return true;
        }
        
        void addRowEntryIfNotNull(RowEntry rowEntry) {
            if (rowEntry != null) {
                rowEntry.rowIndex = rowEntries.size();
                rowEntries.add(rowEntry);
            }
        }
        
        void addColumnEntryIfNotNull(ColumnEntry columnEntry) {
            if (columnEntry != null) {
                columnEntry.columnIndex = columnEntries.size();
                columnEntries.add(columnEntry);
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
    protected class AccountEntry implements Similarable<AccountEntry> {
        final Account account;
        final int accountDepth;
        final List<AccountEntry> childAccountEntries = new ArrayList<>();
        final boolean isIncluded;
        final RowEntry [] accountRowEntries = new RowEntry[AccountRowEntry.values().length];
        
        protected AccountEntry(Account account, boolean isIncluded) {
            this.account = account;
            this.accountDepth = account.getDepth();
            this.isIncluded = isIncluded;
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
                    rowEntry.rowTitle = account.getPathName();
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
        
        @Override
        public boolean isSimilar(AccountEntry other) {
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            if (account != other.account) {
                return false;
            }
            // Don't care about accountDepth, as that is associated with the account.
            
            if (isIncluded != other.isIncluded) {
                return false;
            }
            
            if (!Similarable.areSimilar(accountRowEntries, other.accountRowEntries)) {
                return false;
            }
            
            if (!Similarable.areSimilar(childAccountEntries, other.childAccountEntries)) {
                return false;
            }
            
            return true;
        }
    }
    
    
    /**
     * This manages the report column data for a specific date (which was generated by
     * the report's date generator.
     */
    protected class DateEntry implements Similarable<DateEntry> {
        final LocalDate dateStart;
        final LocalDate dateEnd;
        final int index;
        final List<ColumnEntry> columnEntries = new ArrayList<>();
        
        protected DateEntry(LocalDate dateStart, LocalDate dateEnd, int index) {
            this.dateStart = dateStart;
            this.dateEnd = dateEnd;
            this.index = index;
        }

        
        @Override
        public boolean isSimilar(DateEntry other) {
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            
            if (!dateStart.equals(other.dateStart)) {
                return false;
            }
            if (!dateEnd.equals(other.dateEnd)) {
                return false;
            }
            if (index != other.index) {
                return false;
            }
            return true;
        }
        
        
        protected ColumnEntry getColumnEntryAtIndex(int index) {
            while (index >= columnEntries.size()) {
                columnEntries.add(new ColumnEntry());
            }
            
            return columnEntries.get(index);
        }
        
    }
    
    
    /**
     * This abstract class is the base class for the objects that manage the TableColumn
     * entries for the report column types.
     */
    protected abstract class ColumnGenerator implements Similarable<ColumnGenerator> {
        protected final List<ColumnEntry> columnEntries = new ArrayList<>();
        

        @Override
        public boolean isSimilar(ColumnGenerator other) {
            if (other == null) {
                return false;
            }
            if (getClass() != other.getClass()) {
                return false;
            }
            if (!Similarable.areSimilar(columnEntries, other.columnEntries)) {
                return false;
            }
            
            return true;
        }
        
        
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

    }
    
    
    static enum BalanceRowType {
        VALUE,
        SUB_TOTAL_INCLUDED,
        SUB_TOTAL_EXCLUDED,
        NOT_USED,
    };
    static boolean isSubTotal(BalanceRowType type) {
        return (type == BalanceRowType.SUB_TOTAL_EXCLUDED) || (type == BalanceRowType.SUB_TOTAL_INCLUDED);
    }
    
    /**
     * Abstract class for the report columns that work off the standard account balance.
     */
    protected abstract class BalanceColumnGenerator extends ColumnGenerator {
        int maxIncludedAccountDepth;
        int minIncludedAccountDepth;
        BigDecimal subTotal;
        
        BalanceRowType getRowType(AccountEntry accountEntry) {
            boolean isSubTotal = accountEntry.isAnyChildAccountIncluded();
            if (accountEntry.isIncluded) {
                return (isSubTotal) ? BalanceRowType.SUB_TOTAL_INCLUDED : BalanceRowType.VALUE;
            }
            return (isSubTotal) ? BalanceRowType.SUB_TOTAL_EXCLUDED : BalanceRowType.NOT_USED;
        }

        @Override
        protected void setupAccountEntryRows(ReportOutput reportOutput) {
            maxIncludedAccountDepth = 0;
            minIncludedAccountDepth = Integer.MAX_VALUE;
            
            super.setupAccountEntryRows(reportOutput);
        }
        

        @Override
        protected void setupAccountEntryRows(AccountEntry accountEntry, ReportOutput reportOutput) {
            if (accountEntry.isIncluded) {
                accountEntry.useAccountRowEntry(AccountRowEntry.POST_CHILD_ACCOUNT);

                boolean isSubTotal = accountEntry.isAnyChildAccountIncluded();
                if (isSubTotal) {
                    // The spacer row after the sub-total.
                    accountEntry.useAccountRowEntry(AccountRowEntry.POST_ACCOUNT);
                }
                
                if (accountEntry.accountDepth > maxIncludedAccountDepth) {
                    maxIncludedAccountDepth = accountEntry.accountDepth;
                }
                if (accountEntry.accountDepth < minIncludedAccountDepth) {
                    minIncludedAccountDepth = accountEntry.accountDepth;
                }
            }

            super.setupAccountEntryRows(accountEntry, reportOutput);
        }

        @Override
        protected void setupDateEntryColumns(DateEntry dateEntry, AccountEntry accountEntry, ReportOutput reportOutput, int columnIndexBase) {
            if (!accountEntry.isIncluded) {
                super.setupDateEntryColumns(dateEntry, accountEntry, reportOutput, columnIndexBase);
                return;
            }
            
            BigDecimal previousSubTotal = subTotal;
            subTotal = BigDecimal.ZERO;
            try {
                int columnOffset = maxIncludedAccountDepth - accountEntry.accountDepth;
                final ColumnEntry columnEntry = dateEntry.getColumnEntryAtIndex(columnOffset + columnIndexBase);
                columnEntry.tableColumn.setText(getColumnTitle(columnOffset, accountEntry, dateEntry, reportOutput));

                super.setupDateEntryColumns(dateEntry, accountEntry, reportOutput, columnIndexBase);

                final RowEntry rowEntry = accountEntry.getAccountRowEntry(AccountRowEntry.POST_CHILD_ACCOUNT);

                BigDecimal balance = getInternalAccountBalance(rowEntry, columnEntry, accountEntry, dateEntry, reportOutput);
                subTotal = subTotal.add(balance);
                
                updateAccountRowValue(subTotal, rowEntry, columnEntry, accountEntry, dateEntry, reportOutput);
                
            } finally {
                if (previousSubTotal != null) {
                    previousSubTotal = previousSubTotal.add(subTotal);
                }
                subTotal = previousSubTotal;
            }
        }
        
        
        protected abstract String getColumnTitle(int columnOffset, AccountEntry accountEntry, DateEntry dateEntry, ReportOutput reportOutput);
        
        
        protected BigDecimal getInternalAccountBalance(RowEntry rowEntry, ColumnEntry columnEntry, 
                AccountEntry accountEntry, DateEntry dateEntry, ReportOutput reportOutput) {
            return accountEntry.account.getBalance(dateEntry.dateEnd);
        }
        
        
        protected abstract void updateAccountRowValue(BigDecimal balance, RowEntry rowEntry, ColumnEntry columnEntry, 
                AccountEntry accountEntry, DateEntry dateEntry, ReportOutput reportOutput);
    }
    
    
    /**
     * Reports the current balance as of the end date as the cell's value.
     */
    protected class ValueColumnGenerator extends BalanceColumnGenerator {

        @Override
        protected String getColumnTitle(int columnOffset, AccountEntry accountEntry, DateEntry dateEntry, ReportOutput reportOutput) {
            if (columnOffset == 0) {
                return ResourceSource.getString("Report.ColumnHeading.Value");
            }
            return "";
        }

        @Override
        protected void updateAccountRowValue(BigDecimal balance, RowEntry rowEntry, ColumnEntry columnEntry, AccountEntry accountEntry, DateEntry dateEntry, ReportOutput reportOutput) {
            String value = toMonetaryValueString(balance);
            columnEntry.setRowValue(rowEntry, value);
        }
        
    }
    
    

    
    public ReportDataView() {
        tableView = new TableView<>();
        headingColumn = new TableColumn<>();
        headingColumn.setCellValueFactory((TableColumn.CellDataFeatures<RowCellValues, String> param) -> {
            return param.getValue().headingText;
        });
    }
    

    public void setupView(ReportDefinition definition, Engine engine) {
        this.definition = definition;
        this.engine = engine;
    }
    

    public void shutDownTableView() {
        this.tableView.getItems().clear();
        this.tableView.getColumns().clear();
        
        this.definition = null;
        this.engine = null;
    }
    

    public final Control getControl() {
        return tableView;
    }
    
    public String toMonetaryValueString(BigDecimal value) {
        return value.setScale(2).toPlainString();
    }
    
    public String toPercentString(BigDecimal numerator, BigDecimal denominator) {
        numerator = numerator.multiply(ONE_HUNDRED).setScale(1);
        try {
            BigDecimal value = numerator.divide(denominator, RoundingMode.HALF_UP);
            return value.toPlainString();
        } catch (ArithmeticException ex) {
            return "-";
        }
    }
    

    public void refreshFromReportDefinition() {
        refreshFromEngine();
    }
    

    public void refreshFromEngine() {
        ReportOutput updatedReportOutput = new ReportOutput();
        updateReportOutput(updatedReportOutput);
        
        if (!updatedReportOutput.isSimilar(currentReportOutput)) {
            // Need to replace the columns and rows in the table view.
            replaceAllTableCells(updatedReportOutput);
        }
        else {
            // Just need to update all the cell values...
            updateTableCellValues(updatedReportOutput);
        }
    }
    
    
    protected void replaceAllTableCells(ReportOutput reportOutput) {
        ObservableList<RowCellValues> rowValues = FXCollections.observableArrayList();
        reportOutput.rowEntries.forEach((rowEntry) -> {
            rowValues.add(new RowCellValues(rowEntry.rowTitle));
        });
        
        tableView.getItems().clear();
        tableView.getColumns().clear();
        
        tableView.getColumns().add(headingColumn);
        
        List<TableColumn<RowCellValues, String>> columnGroup = new ArrayList<>();
        reportOutput.dateEntries.forEach((dateEntry) -> {
            columnGroup.clear();
            dateEntry.columnEntries.forEach((columnEntry) -> {
                if (columnEntry.tableColumn != null) {
                    columnGroup.add(columnEntry.tableColumn);
                }
            });
            
            TableColumn<RowCellValues, String> dateColumn;
            if (columnGroup.size() == 1) {
                // Single column, it's going to be the date column.
                dateColumn = columnGroup.get(0);
            }
            else {
                dateColumn = new TableColumn<>();
                dateColumn.getColumns().addAll(columnGroup);
            }
            
            dateColumn.setText(getDateColumnLabel(dateEntry));
            
            tableView.getColumns().add(dateColumn);
        });
        
        tableView.setItems(rowValues);
        
        updateTableCellValues(reportOutput);
    }
    

    protected String getDateColumnLabel(DateEntry dateEntry) {
        String endDateString = dateEntry.dateEnd.format(columnDateTimeFormatter);
        if (dateEntry.dateStart.equals(dateEntry.dateEnd)) {
            return endDateString;
        }

        String startDateString = dateEntry.dateStart.format(columnDateTimeFormatter);
        return ResourceSource.getString("Report.ColumnHeading.StartEndDate", startDateString, endDateString);
    }

    
    protected void updateTableCellValues(ReportOutput reportOutput) {
        reportOutput.columnEntries.forEach((columnEntry) -> {
            final int rowCount = columnEntry.rowValues.size();
            for (int i = 0; i < rowCount; ++i) {
                String value = columnEntry.rowValues.get(i);
                if (value != null) {
                    tableView.getItems().get(i).setColumnValue(columnEntry.columnIndex, value);
                }
            }
        });
        
        this.currentReportOutput = reportOutput;
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
            AccountEntry accountEntry = new AccountEntry(account, isIncluded);
            reportOutput.accountEntries.add(accountEntry);
            
            addChildAccountEntries(accountEntry, filter, reportOutput);
        });
    }
    
    
    protected void addChildAccountEntries(AccountEntry accountEntry, AccountFilter filter, ReportOutput reportOutput) {
        accountEntry.account.getChildren().forEach((account) -> {
            boolean isIncluded = filter.isIncludeAccount(account);
            AccountEntry childAccountEntry = new AccountEntry(account, isIncluded);
            accountEntry.childAccountEntries.add(childAccountEntry);
            
            addChildAccountEntries(childAccountEntry, filter, reportOutput);
        });
    }
    
    protected void createDateEntries(ReportOutput reportOutput) {
        Iterator<LocalDate> dateIterator = this.definition.getDateGenerator().getIterator(LocalDate.now());
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
                break;
            case DELTA_OLDEST_PERIOD:
                break;
            case PERCENT_DELTA_PREVIOUS_PERIOD:
                break;
            case PERCENT_DELTA_OLDEST_PERIOD:
                break;
            case COST_BASIS:
                break;
            case GAIN:
                break;
            case QUANTITY:
                break;
            case PRICE:
                break;
            default:
                throw new AssertionError(columnType.name());
            
        }
        return null;
    }

    
    private void processAccountEntries(ReportOutput reportOutput) {
        reportOutput.rowEntries.clear();
        
        reportOutput.columnGenerators.forEach((generator) -> {
            generator.setupAccountEntryRows(reportOutput);
        });
        
        reportOutput.accountEntries.forEach((acountEntry) -> {
            addAccountEntryRowsToReportOutput(acountEntry, reportOutput);
        });
    }
    
    
    private void addAccountEntryRowsToReportOutput(AccountEntry accountEntry, ReportOutput reportOutput) {
        reportOutput.addRowEntryIfNotNull(accountEntry.getAccountRowEntry(AccountRowEntry.PRE_ACCOUNT));
        reportOutput.addRowEntryIfNotNull(accountEntry.getAccountRowEntry(AccountRowEntry.PRE_CHILD_ACCOUNT));
        
        accountEntry.childAccountEntries.forEach((childAccountEntry) -> {
            addAccountEntryRowsToReportOutput(childAccountEntry, reportOutput);
        });

        reportOutput.addRowEntryIfNotNull(accountEntry.getAccountRowEntry(AccountRowEntry.POST_CHILD_ACCOUNT));
        reportOutput.addRowEntryIfNotNull(accountEntry.getAccountRowEntry(AccountRowEntry.POST_ACCOUNT));
    }

    
    private void processDateEntries(ReportOutput reportOutput) {
        reportOutput.columnEntries.clear();

        reportOutput.columnGenerators.forEach((generator) -> {
            generator.setupDateEntryColumns(reportOutput);
        });
        
        reportOutput.dateEntries.forEach((dateEntry) -> {
            dateEntry.columnEntries.forEach((columnEntry) -> {
                reportOutput.addColumnEntryIfNotNull(columnEntry);
            });
        });
    }
}
