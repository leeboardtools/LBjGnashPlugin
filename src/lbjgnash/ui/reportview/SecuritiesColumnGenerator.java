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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.SecurityNode;
import lbjgnash.ui.ReportDefinition;
import org.hsqldb.lib.StringUtil;

/**
 *
 * @author Albert Santos
 */
abstract class SecuritiesColumnGenerator extends ColumnGenerator {
    protected final Map<AccountEntry, AccountEntryInfo> accountEntryInfos = new HashMap<>();
    protected final Map<DateEntry, DateEntryInfo> dateEntryInfos = new HashMap<>();
    
    protected static final String CASH_SYMBOL = "_Cash_";
    
    protected static class SecurityRowEntry {
        final SecurityTransactionTracker transactionTracker;
        final AccountEntry accountEntry;
        final RowEntry rowEntry;
        
        protected SecurityRowEntry(SecurityTransactionTracker transactionTracker, AccountEntry accountEntry, RowEntry rowEntry) {
            this.transactionTracker = transactionTracker;
            this.accountEntry = accountEntry;
            this.rowEntry = rowEntry;
        }
    }
    
    
    protected static class AccountEntryInfo {
        protected final AccountEntry accountEntry;
        protected final Set<SecurityRowEntry> securityRowEntries = new HashSet<>();
        
        protected AccountEntryInfo(AccountEntry accountEntry) {
            this.accountEntry = accountEntry;
        }
    }
    
    
    protected static class DatedSummaryEntryInfo {
        protected final ColumnEntry columnEntry;
        protected final AccountEntryInfo reportingAccountEntryInfo;
        
        protected BigDecimal totalCashIn = BigDecimal.ZERO;
        protected BigDecimal cashInYearAgoValueSum = BigDecimal.ZERO;
        
        protected BigDecimal totalCostBasis = BigDecimal.ZERO;
        protected BigDecimal totalMarketValue = BigDecimal.ZERO;
        protected BigDecimal yearAgoValueSum = BigDecimal.ZERO;
        protected BigDecimal totalQuantity = null;
        protected BigDecimal price = null;
        
        protected DatedSummaryEntryInfo(ColumnEntry columnEntry, AccountEntryInfo reportingAccountEntryInfo) {
            this.columnEntry = columnEntry;
            this.reportingAccountEntryInfo = reportingAccountEntryInfo;
        }
        
        protected BigDecimal getNetGain() {
            return totalMarketValue.subtract(totalCashIn);
        }
    }
    
    
    protected static class DatedSecurityEntryInfo {
        protected final SecurityRowEntry securityRowEntry;
        protected final SecurityTransactionTracker.DateEntry trackerDateEntry;
        protected final ColumnEntry columnEntry;
        
        protected DatedSecurityEntryInfo(SecurityRowEntry securityRowEntry, SecurityTransactionTracker.DateEntry trackerDateEntry,
                ColumnEntry columnEntry) {
            this.securityRowEntry = securityRowEntry;
            this.trackerDateEntry = trackerDateEntry;
            this.columnEntry = columnEntry;
        }
        
        protected BigDecimal getNetGain(LocalDate endDate) {
            return trackerDateEntry.getMarketValue(endDate).subtract(trackerDateEntry.getTotalCashIn());
        }
        protected BigDecimal getTotalCashIn() {
            return trackerDateEntry.getTotalCashIn();
        }
        protected BigDecimal getCashInYearAgoValueSum(LocalDate endDate, int minDays) {
            return trackerDateEntry.getCashInYearAgoValueSum(endDate, minDays);
        }
    }
    
    
    protected static class DateEntryInfo {
        protected final DateEntry dateEntry;
        protected final Map<SecurityRowEntry, DatedSecurityEntryInfo> datedSecurityEntryInfos = new HashMap<>();
        protected final Map<AccountEntry, DatedSummaryEntryInfo> accountDatedSummaryEntryInfos = new HashMap<>();
        protected final Map<String, DatedSummaryEntryInfo> securityDatedSummaryEntryInfos = new HashMap<>();
        protected final ColumnEntry columnEntry;
        
        protected BigDecimal totalCashIn = BigDecimal.ZERO;
        protected BigDecimal cashInYearAgoValueSum = BigDecimal.ZERO;
        
        protected BigDecimal totalCostBasis = BigDecimal.ZERO;
        protected BigDecimal totalMarketValue = BigDecimal.ZERO;
        protected BigDecimal annualPercentRateOfReturn = BigDecimal.ZERO;
        protected BigDecimal yearAgoValueSum = BigDecimal.ZERO;
        
        protected DateEntryInfo(DateEntry dateEntry, ColumnEntry columnEntry) {
            this.dateEntry = dateEntry;
            this.columnEntry = columnEntry;
        }
        
        protected BigDecimal getNetGain() {
            return totalMarketValue.subtract(totalCashIn);
        }
    }
    
    
    protected static class SecurityCellEntry extends CellEntry {
        protected final DatedSecurityEntryInfo datedSecurityEntryInfo;
        protected final DatedSummaryEntryInfo datedSummaryEntryInfo;
        String basicStyle = ReportDataView.STYLE_BALANCE_VALUE;
        
        protected SecurityCellEntry(DatedSecurityEntryInfo datedSecurityEntryInfo, RowEntry rowEntry, String value) {
            super(rowEntry, value);
            this.datedSecurityEntryInfo = datedSecurityEntryInfo;
            this.datedSummaryEntryInfo = null;
        }
        
        protected SecurityCellEntry(DatedSummaryEntryInfo datedSummaryEntryInfo, RowEntry rowEntry, String value) {
            super(rowEntry, value);
            this.datedSecurityEntryInfo = null;
            this.datedSummaryEntryInfo = datedSummaryEntryInfo;
        }
        
        protected SecurityCellEntry(RowEntry rowEntry, String value) {
            super(rowEntry, value);
            this.datedSecurityEntryInfo = null;
            this.datedSummaryEntryInfo = null;
        }
    }
    
    
    /**
     * Used to set the CSS styles for the individual cells.
     */
    protected static class SecurityTreeCell extends TextFieldTreeTableCell<RowEntry, CellEntry> {
        final SecuritiesColumnGenerator generator;
        final DateEntryInfo dateEntryInfo;
        
        SecurityTreeCell(SecuritiesColumnGenerator generator, DateEntryInfo dateEntryInfo) {
            this.generator = generator;
            this.dateEntryInfo = dateEntryInfo;
        }

        @Override
        public void updateItem(CellEntry item, boolean empty) {
            super.updateItem(item, empty);            
            
            if ((item instanceof SecurityCellEntry) && !empty) {
                SecurityCellEntry securityCellEntry = (SecurityCellEntry)item;
                
                getStyleClass().add(ReportDataView.STYLE_CELL);
                getStyleClass().remove(ReportDataView.STYLE_SUBTOTAL);
                getStyleClass().remove(ReportDataView.STYLE_SUMMARY);
                getStyleClass().remove(ReportDataView.STYLE_GRAND_TOTAL);
                
                getStyleClass().add(securityCellEntry.basicStyle);
                
/*                if (securityCellEntry.accountEntryInfo != null) {
                    if (securityCellEntry.rowEntry == securityCellEntry.accountEntryInfo.accountEntry.summaryRowEntry) {
                        getStyleClass().add(ReportDataView.STYLE_SUMMARY);
                    }
                    else if (securityCellEntry.rowEntry == securityCellEntry.accountEntryInfo.rowEntry) {
                        getStyleClass().add(ReportDataView.STYLE_SUBTOTAL);
                    }
                }
                else {
                    getStyleClass().add(ReportDataView.STYLE_GRAND_TOTAL);
                }
*/
            }
        }
    }
    
    boolean usesNamedRowEntries(ReportDataView.ReportOutput reportOutput) {
        return reportOutput.getDefinition().getStyle() == ReportDefinition.Style.SECURITIES;
    }
    
    

    @Override
    protected void setupRowsForAccountEntry(AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, AccountEntry parentAccountEntry) {
        
        boolean isPossibleCashRow = true;
        
        if (accountEntry.isIncluded) {
            AccountSecuritiesTracker accountTracker = accountEntry.getAccountSecuritiesTracker();
            if ((accountTracker != null) && !accountTracker.getTransactionTrackers().isEmpty()) {

                AccountEntryInfo accountEntryInfo;
                if (accountEntry.account.getAccountType().getAccountGroup().equals(AccountGroup.INVEST)
                        && (accountTracker.getTransactionTrackers().size() == 1)) {
                    // If it's a single security, and the account is an investment account, let's treat the
                    // account as a security row of the parent.
                    accountEntryInfo = accountEntryInfos.get(parentAccountEntry);
                    if (accountEntryInfo == null) {
                        accountEntryInfo = new AccountEntryInfo(parentAccountEntry);
                        accountEntryInfos.put(parentAccountEntry, accountEntryInfo);
                        if (!usesNamedRowEntries(reportOutput)) {
                            parentAccountEntry.useSummaryRowEntry();
                        }
                    }
                }
                else {
                    accountEntryInfo = new AccountEntryInfo(accountEntry);
                    accountEntryInfos.put(accountEntry, accountEntryInfo);
                    if (!usesNamedRowEntries(reportOutput)) {
                        accountEntry.useSummaryRowEntry();
                    }
                }

                for (Map.Entry<SecurityNode, SecurityTransactionTracker> entry : accountTracker.getTransactionTrackers().entrySet()) {
                    SecurityTransactionTracker transactionTracker = entry.getValue();
                    addSecurityRowEntry(reportOutput, transactionTracker, accountEntryInfo);
                }
                
                isPossibleCashRow = false;
            }
        }
        
        super.setupRowsForAccountEntry(accountEntry, reportOutput, parentAccountEntry);
        
        if (isPossibleCashRow) {
            AccountEntryInfo accountEntryInfo = accountEntryInfos.get(accountEntry);
            if (accountEntryInfo != null) {
                AccountSecuritiesTracker accountTracker = accountEntry.getAccountSecuritiesTracker();
                if (accountTracker != null) {
                    //addCashRowEntry(accountEntryInfo);
                    if (!usesNamedRowEntries(reportOutput)) {
                        accountEntry.useSummaryRowEntry();
                    }
                    
                    for (Map.Entry<SecurityNode, SecurityTransactionTracker> entry : accountTracker.getTransactionTrackers().entrySet()) {
                        SecurityTransactionTracker transactionTracker = entry.getValue();
                        addSecurityRowEntry(reportOutput, transactionTracker, accountEntryInfo);
                    }
                }
                else {
                    addCashRowEntry(reportOutput, accountEntryInfo);
                }
            }
        }
    }
    
    protected void addSecurityRowEntry(ReportDataView.ReportOutput reportOutput, 
            SecurityTransactionTracker transactionTracker, AccountEntryInfo accountEntryInfo) {
        SecurityNode securityNode = transactionTracker.getSecurityNode();
        String symbol = getNameForSecurity(reportOutput, securityNode);
        RowEntry rowEntry = useRowEntry(reportOutput, accountEntryInfo, symbol);

        rowEntry.setRowTitle(symbol);
        AccountEntry accountEntry = accountEntryInfo.accountEntry;
        SecurityRowEntry securityRowEntry = new SecurityRowEntry(transactionTracker, accountEntry, rowEntry);
        accountEntryInfo.securityRowEntries.add(securityRowEntry);
    }
    
    protected void addCashRowEntry(ReportDataView.ReportOutput reportOutput, AccountEntryInfo accountEntryInfo) {
        RowEntry rowEntry = useRowEntry(reportOutput, accountEntryInfo, CASH_SYMBOL);
        
        rowEntry.setRowTitle(ResourceSource.getString("Report.CashRow"));
        SecurityRowEntry cashRowEntry = new SecurityRowEntry(null, accountEntryInfo.accountEntry, rowEntry);
        accountEntryInfo.securityRowEntries.add(cashRowEntry);
    }
    
    protected String getNameForSecurity(ReportDataView.ReportOutput reportOutput, SecurityNode securityNode) {
        String cusId = securityNode.getISIN();
        if ("Cash".equals(cusId)) {
            return "Cash";
        }
        return securityNode.getSymbol();
    }
    
    protected RowEntry useRowEntry(ReportDataView.ReportOutput reportOutput, AccountEntryInfo accountEntryInfo, String name) {
        if (usesNamedRowEntries(reportOutput)) {
            RowEntry rowEntry = reportOutput.namedRowEntries.get(name);
            if (rowEntry == null) {
                rowEntry = new RowEntry();
                reportOutput.namedRowEntries.put(name, rowEntry);
            }
            return rowEntry;
        }
        else {
            int rowIndex = accountEntryInfo.securityRowEntries.size();
            AccountEntry accountEntry = accountEntryInfo.accountEntry;
            return accountEntry.usePostChildAccountRowEntry(rowIndex);
        }
    }
    

    @Override
    protected void setupColumnsForDateEntry(DateEntry dateEntry, AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, 
            int columnIndexBase) {
        AccountEntryInfo accountEntryInfo = accountEntryInfos.get(accountEntry);
        if (accountEntryInfo != null) {
            ColumnEntry columnEntry = dateEntry.getColumnEntryAtIndex(columnIndexBase);
            
            DateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
            if (dateEntryInfo == null) {
                dateEntryInfo = createDateEntryInfo(accountEntryInfo, dateEntry, columnEntry, reportOutput, columnIndexBase);
                dateEntryInfos.put(dateEntry, dateEntryInfo);
                
                columnEntry.treeTableColumn.setText(getColumnTitle(accountEntry, dateEntry, reportOutput));
                final DateEntryInfo finalDateEntryInfo = dateEntryInfo;
                columnEntry.treeTableColumn.setCellFactory((TreeTableColumn<RowEntry, CellEntry> column) -> {
                    return new SecurityTreeCell(this, finalDateEntryInfo);
                });
            }
            
            // Summary...
            DatedSummaryEntryInfo datedSummaryEntryInfo = null;
            if (!usesNamedRowEntries(reportOutput)) {
                datedSummaryEntryInfo = createDatedSummaryEntryInfo(accountEntryInfo, dateEntryInfo, 
                        columnEntry, reportOutput, columnIndexBase);
                dateEntryInfo.accountDatedSummaryEntryInfos.put(accountEntry, datedSummaryEntryInfo);
            }

            for (SecurityRowEntry securityRowEntry : accountEntryInfo.securityRowEntries) {
                DatedSecurityEntryInfo securityEntryInfo = createDatedSecurityEntryInfo(securityRowEntry, dateEntryInfo, columnEntry, 
                    reportOutput, columnIndexBase);
                if ((securityEntryInfo != null) && (securityRowEntry.transactionTracker != null)) {
                    if (usesNamedRowEntries(reportOutput)) {
                        String securityName = getNameForSecurity(reportOutput, securityRowEntry.transactionTracker.getSecurityNode());
                        datedSummaryEntryInfo = dateEntryInfo.securityDatedSummaryEntryInfos.get(securityName);
                        if (datedSummaryEntryInfo == null) {
                            datedSummaryEntryInfo = createDatedSummaryEntryInfo(securityName, accountEntryInfo, dateEntryInfo, columnEntry, 
                                reportOutput, columnIndexBase);
                            dateEntryInfo.securityDatedSummaryEntryInfos.put(securityName, datedSummaryEntryInfo);
                        }
                    }
                    dateEntryInfo.datedSecurityEntryInfos.put(securityRowEntry, securityEntryInfo);

                    if (securityEntryInfo.trackerDateEntry != null) {
                        updateDatedSummaryEntryInfo(datedSummaryEntryInfo, securityEntryInfo, dateEntryInfo, reportOutput);
                    }
                    else {
                        updateDatedSummaryEntryInfoFromCash(datedSummaryEntryInfo, securityEntryInfo, dateEntryInfo, reportOutput);
                    }
                }
            }
        }
        else {
            super.setupColumnsForDateEntry(dateEntry, accountEntry, reportOutput, columnIndexBase);
        }
    }

    
    protected DateEntryInfo createDateEntryInfo(AccountEntryInfo accountEntryInfo, DateEntry dateEntry, 
            ColumnEntry columnEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        return new DateEntryInfo(dateEntry, columnEntry);
    }
    
    
    protected DatedSecurityEntryInfo createDatedSecurityEntryInfo(SecurityRowEntry securityRowEntry, DateEntryInfo dateEntryInfo,
            ColumnEntry columnEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        if (securityRowEntry.transactionTracker != null) {
            SecurityTransactionTracker.DateEntry trackerDateEntry = securityRowEntry.transactionTracker.getDateEntry(dateEntryInfo.dateEntry.endDate);
            if (trackerDateEntry == null) {
                return null;
            }
            return new DatedSecurityEntryInfo(securityRowEntry, trackerDateEntry, columnEntry);
        }
        else {
            // TODO: Treat cash as a security in the security tracking stuff!
            // Cash entry...
            return new DatedSecurityEntryInfo(securityRowEntry, null, columnEntry);
        }
    }
    
    
    protected DatedSummaryEntryInfo createDatedSummaryEntryInfo(AccountEntryInfo accountEntryInfo, DateEntryInfo dateEntryInfo,
            ColumnEntry columnEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        DatedSummaryEntryInfo datedSummaryEntryInfo = new DatedSummaryEntryInfo(columnEntry, accountEntryInfo);
        return datedSummaryEntryInfo;
    }
    
    protected DatedSummaryEntryInfo createDatedSummaryEntryInfo(String securityName, AccountEntryInfo accountEntryInfo, 
            DateEntryInfo dateEntryInfo, ColumnEntry columnEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        DatedSummaryEntryInfo datedSummaryEntryInfo = new DatedSummaryEntryInfo(columnEntry, accountEntryInfo);
        return datedSummaryEntryInfo;
    }
    
    
    protected void updateDatedSummaryEntryInfo(DatedSummaryEntryInfo datedSummaryEntryInfo, DatedSecurityEntryInfo datedSecurityEntryInfo,
            DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        LocalDate date = dateEntryInfo.dateEntry.endDate;
        
        Account account = datedSecurityEntryInfo.securityRowEntry.accountEntry.account;

        BigDecimal costBasis = datedSecurityEntryInfo.trackerDateEntry.getCostBasis();
        datedSummaryEntryInfo.totalCostBasis = datedSummaryEntryInfo.totalCostBasis.add(costBasis);
        dateEntryInfo.totalCostBasis = dateEntryInfo.totalCostBasis.add(costBasis);
        
        BigDecimal marketValue = datedSecurityEntryInfo.trackerDateEntry.getMarketValue(date);
        marketValue = reportOutput.toMonetaryValue(marketValue, account);
        
        datedSummaryEntryInfo.totalMarketValue = datedSummaryEntryInfo.totalMarketValue.add(marketValue);
        dateEntryInfo.totalMarketValue = dateEntryInfo.totalMarketValue.add(marketValue);
        
        BigDecimal yearAgoValueSum = datedSecurityEntryInfo.trackerDateEntry.getYearAgoValueSum(date, 
                reportOutput.getMinDaysForRateOfReturn());
        datedSummaryEntryInfo.yearAgoValueSum = datedSummaryEntryInfo.yearAgoValueSum.add(yearAgoValueSum);
        dateEntryInfo.yearAgoValueSum = dateEntryInfo.yearAgoValueSum.add(yearAgoValueSum);
        
        BigDecimal totalCashIn = datedSecurityEntryInfo.trackerDateEntry.getTotalCashIn();
        datedSummaryEntryInfo.totalCashIn = datedSummaryEntryInfo.totalCashIn.add(totalCashIn);
        dateEntryInfo.totalCashIn = dateEntryInfo.totalCashIn.add(totalCashIn);
        
        BigDecimal cashInYearAgoValueSum = datedSecurityEntryInfo.getCashInYearAgoValueSum(date, 
                reportOutput.getMinDaysForRateOfReturn());
        datedSummaryEntryInfo.cashInYearAgoValueSum = datedSummaryEntryInfo.cashInYearAgoValueSum.add(cashInYearAgoValueSum);
        dateEntryInfo.cashInYearAgoValueSum = dateEntryInfo.cashInYearAgoValueSum.add(cashInYearAgoValueSum);
        
        if (usesNamedRowEntries(reportOutput)) {
            
            BigDecimal quantity = datedSecurityEntryInfo.trackerDateEntry.getTotalShares();
            if (datedSummaryEntryInfo.totalQuantity == null) {
                datedSummaryEntryInfo.totalQuantity = quantity;
            }
            else {
                datedSummaryEntryInfo.totalQuantity = datedSummaryEntryInfo.totalQuantity.add(quantity);
            }
            
            datedSummaryEntryInfo.price =  datedSecurityEntryInfo.trackerDateEntry.getMarketPrice(date);
        }
    }

    
    protected void updateDatedSummaryEntryInfoFromCash(DatedSummaryEntryInfo datedSummaryEntryInfo, DatedSecurityEntryInfo datedSecurityEntryInfo,
            DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        Account account = datedSecurityEntryInfo.securityRowEntry.accountEntry.account;

        BigDecimal marketValue = account.getBalance(dateEntryInfo.dateEntry.endDate);
        marketValue = reportOutput.toMonetaryValue(marketValue, account);

        datedSummaryEntryInfo.totalMarketValue = datedSummaryEntryInfo.totalMarketValue.add(marketValue);
        dateEntryInfo.totalMarketValue = dateEntryInfo.totalMarketValue.add(marketValue);
    }
    
    
    @Override
    protected void updateCellValuesForDateEntryAccountEntry(DateEntry dateEntry, AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput) {
        DateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
        if (dateEntryInfo != null) {
            if (!dateEntryInfo.securityDatedSummaryEntryInfos.isEmpty()) {
                dateEntryInfo.securityDatedSummaryEntryInfos.forEach((securityName, datedSummaryEntryInfo) -> {
                    // We're just summarizing all the accounts...
                    RowEntry rowEntry = reportOutput.namedRowEntries.get(securityName);
                    if (rowEntry != null) {
                        String cellValue = getSummaryEntryCellValue(datedSummaryEntryInfo, dateEntryInfo, reportOutput);
                        if (cellValue != null) {
                            ColumnEntry columnEntry = datedSummaryEntryInfo.columnEntry;
                            SecurityCellEntry cellEntry = new SecurityCellEntry(datedSummaryEntryInfo, rowEntry, cellValue);
                            rowEntry.setExpandedColumnCellValue(columnEntry, cellEntry);
                            rowEntry.setNonExpandedColumnCellValue(columnEntry, cellEntry);
                        }
                    }
                });
            }
            else {
                AccountEntryInfo accountEntryInfo = accountEntryInfos.get(accountEntry);
                if (accountEntryInfo != null) {
                    DatedSummaryEntryInfo datedSummaryEntryInfo = dateEntryInfo.accountDatedSummaryEntryInfos.get(accountEntry);
                    if (datedSummaryEntryInfo != null) {
                        // Summary...
                        String cellValue = getSummaryEntryCellValue(datedSummaryEntryInfo, dateEntryInfo, reportOutput);
                        if (cellValue != null) {
                            ColumnEntry columnEntry = datedSummaryEntryInfo.columnEntry;
                            if (accountEntry.summaryRowEntry != null) {
                                SecurityCellEntry cellEntry = new SecurityCellEntry(datedSummaryEntryInfo, accountEntry.summaryRowEntry, cellValue);
                                accountEntry.summaryRowEntry.setExpandedColumnCellValue(columnEntry, cellEntry);
                                accountEntry.summaryRowEntry.setNonExpandedColumnCellValue(columnEntry, cellEntry);
                            }
                        }
                    }

                    final DatedSummaryEntryInfo finalDatedSummaryEntryInfo = datedSummaryEntryInfo;
                    accountEntryInfo.securityRowEntries.forEach((SecurityRowEntry securityRowEntry) -> {
                        DatedSecurityEntryInfo datedSecurityEntryInfo = dateEntryInfo.datedSecurityEntryInfos.get(securityRowEntry);
                        if (datedSecurityEntryInfo != null) {
                            ColumnEntry columnEntry = datedSecurityEntryInfo.columnEntry;
                            String cellValue;
                            if (datedSecurityEntryInfo.trackerDateEntry == null) {
                                cellValue = getCashEntryCellValue(datedSecurityEntryInfo, finalDatedSummaryEntryInfo, dateEntryInfo, reportOutput);
                            }
                            else {
                                cellValue = getSecurityEntryCellValue(datedSecurityEntryInfo, dateEntryInfo, reportOutput);
                            }

                            if ((cellValue != null) && (securityRowEntry.rowEntry != null)) {
                                SecurityCellEntry cellEntry = new SecurityCellEntry(datedSecurityEntryInfo, securityRowEntry.rowEntry, cellValue);
                                securityRowEntry.rowEntry.setExpandedColumnCellValue(columnEntry, cellEntry);
                                securityRowEntry.rowEntry.setNonExpandedColumnCellValue(columnEntry, cellEntry);
                            }
                        }
                    });
                }
            }
        }
        
        super.updateCellValuesForDateEntryAccountEntry(dateEntry, accountEntry, reportOutput);
    }
    

    @Override
    protected void updateGrandTotalCellValue(DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        DateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
        if (dateEntryInfo != null) {
            String value = getGrandTotalCellValue(dateEntryInfo, reportOutput);
            if (!StringUtil.isEmpty(value)) {
                ColumnEntry columnEntry = dateEntryInfo.columnEntry;
                CellEntry cellEntry = new SecurityCellEntry(reportOutput.grandTotalRowEntry, value);
                reportOutput.grandTotalRowEntry.setNonExpandedColumnCellValue(columnEntry, cellEntry);
            }
        }
        super.updateGrandTotalCellValue(dateEntry, reportOutput); //To change body of generated methods, choose Tools | Templates.
    }

    
    protected abstract String getColumnTitle(AccountEntry accountEntry, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput);

    
    protected String getCashEntryCellValue(DatedSecurityEntryInfo securityEntryInfo, DatedSummaryEntryInfo datedSummaryEntryInfo, 
            DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        return null;
    }

    
    protected abstract String getSecurityEntryCellValue(DatedSecurityEntryInfo securityEntryInfo, DateEntryInfo dateEntryInfo, 
            ReportDataView.ReportOutput reportOutput);
    
    
    protected String getSummaryEntryCellValue(DatedSummaryEntryInfo datedSummaryEntryInfo, DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        return null;
    }
    
    protected String getGrandTotalCellValue(DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        return null;
    }
}
