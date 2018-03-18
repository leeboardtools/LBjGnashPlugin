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

import com.leeboardtools.dialog.PromptDialog;
import com.leeboardtools.util.CSVUtil;
import com.leeboardtools.util.ResourceSource;
import com.leeboardtools.util.StringUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Control;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.Engine;
import jgnash.engine.MathConstants;
import jgnash.engine.SecurityNode;
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
     * This represents the full output of one report.
     */
    protected class ReportOutput {
        final List<AccountEntry> accountEntries = new ArrayList<>();
        final List<DateEntry> dateEntries = new ArrayList<>();
        final List<ColumnGenerator> columnGenerators = new ArrayList<>();
        
        // This list is built up from the column entries of the dateEntries.
        final List<ColumnEntry> columnEntries = new ArrayList<>();

        RowEntry grandTotalRowEntry;
        
        public BigDecimal toMonetaryValue(BigDecimal value, Account account) {
            return value.setScale(2, MathConstants.roundingMode);
        }

        public String toMonetaryValueString(BigDecimal value, Account account) {
            if (definition.getStyle() == ReportDefinition.Style.INCOME_EXPENSE) {
                if (account == null) {
                    // Presume this is a sub-total...
                    value = value.negate();
                }
                else {
                    AccountGroup accountGroup = account.getAccountType().getAccountGroup();
                    if ((accountGroup == AccountGroup.INCOME) || (accountGroup == AccountGroup.EXPENSE)) {
                        value = value.negate();
                    }
                }
            }
            return value.setScale(2, MathConstants.roundingMode).toPlainString();
        }

        public String toPercentString(BigDecimal numerator, BigDecimal denominator) {
            numerator = numerator.multiply(ReportDataView.ONE_HUNDRED).setScale(1, MathConstants.roundingMode);
            try {
                BigDecimal value = numerator.divide(denominator, MathConstants.roundingMode);
                return value.toPlainString() + percentSuffix;
            } catch (ArithmeticException ex) {
                return "-";
            }
        }
        
        public String toSharesQuantity(BigDecimal value) {
            return value.setScale(4, MathConstants.roundingMode).toPlainString();
        }
        
        public String toSecurityPrice(BigDecimal value, SecurityNode securityNode) {
            return value.setScale(2, MathConstants.roundingMode).toPlainString();
        }
        
        public int getMinDaysForRateOfReturn() {
            return 5;
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
            if (param.getValue().getValue() == null) {
                return null;
            }
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
        
        final TreeItem<RowEntry> itemForChildren = parent;
        accountEntry.childAccountEntries.forEach((childAccountEntry) -> {
            addRowsForAccountEntry(itemForChildren, childAccountEntry);
        });
        
        if (accountEntry.postChildAccountRowEntries != null) {
            final TreeItem<RowEntry> finalParent = parent;
            accountEntry.postChildAccountRowEntries.forEach((rowEntry) -> {
                finalParent.getChildren().add(new TreeItem<>(rowEntry));
            });
        }

        addRowEntryIfNotNull(parent, accountEntry, accountEntry.postChildRowEntry);
    }
    
    protected void addRowEntryIfNotNull(TreeItem<RowEntry> parent, AccountEntry accountEntry, RowEntry rowEntry) {
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
                if (startDate.isBefore(endDate)) {
                    startDate = startDate.plusDays(1);
                }
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
                return new CostBasisColumnGenerator();
                
            case GAIN:
                return new GainColumnGenerator();
                
            case PERCENT_GAIN :
                return new PercentGainColumnGenerator();
                
            case QUANTITY:
                return new QuantityColumnGenerator();
                
            case PRICE:
                return new PriceColumnGenerator();
                
            case PERCENT_PORTFOLIO :
                return new PercentPortfolioColumnGenerator();
                
            case ANNUAL_RATE_OF_RETURN :
                return new AnnualRateOfReturnColumnGenerator();
                
            case MARKET_VALUE :
                return new MarketValueColumnGenerator();
                
            default:
                throw new AssertionError(columnType.name());
            
        }
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
    
    
    void handleExportFailure(String contextId, File file, IOException ex) {
        String title = ResourceSource.getString("ReportView.Export.ErrorTitle");
        String message = ResourceSource.getString(contextId, file.getName(), ex.getLocalizedMessage());
        PromptDialog.showOKDialog(message, title);
    }
    
    public boolean exportCSVFile(File file) {
        FileWriter writer;
        try {
            writer = new FileWriter(file);
        } catch (IOException ex) {
            Logger.getLogger(ReportDataView.class.getName()).log(Level.SEVERE, null, ex);
            handleExportFailure("ReportView.ExportCSV.CreateError", file, ex);
            return false;
        }
        
        try {
            CSVUtil.treeTableViewToCSV(treeTableView, writer);
        } catch (IOException ex) {
            Logger.getLogger(ReportDataView.class.getName()).log(Level.SEVERE, null, ex);
            handleExportFailure("ReportView.ExportCSV.WriteError", file, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(ReportDataView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return true;
    }
}
