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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.SecurityNode;

/**
 *
 * @author Albert Santos
 */
abstract class SecuritiesColumnGenerator extends ColumnGenerator {
    protected final Map<AccountEntry, AccountEntryInfo> accountEntryInfos = new HashMap<>();
    protected final Map<DateEntry, DateEntryInfo> dateEntryInfos = new HashMap<>();
    
    
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
        protected BigDecimal totalCostBasis = BigDecimal.ZERO;
        protected BigDecimal totalMarketValue = BigDecimal.ZERO;
        
        protected DatedSummaryEntryInfo(ColumnEntry columnEntry) {
            this.columnEntry = columnEntry;
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
    }
    
    
    protected static class DateEntryInfo {
        protected final DateEntry dateEntry;
        protected final Map<SecurityRowEntry, DatedSecurityEntryInfo> datedSecurityEntryInfos = new HashMap<>();
        protected final Map<AccountEntry, DatedSummaryEntryInfo> datedSummaryEntryInfos = new HashMap<>();
        
        protected BigDecimal totalCostBasis = BigDecimal.ZERO;
        protected BigDecimal totalMarketValue = BigDecimal.ZERO;
        protected BigDecimal annualPercentRateOfReturn = BigDecimal.ZERO;
        
        protected DateEntryInfo(DateEntry dateEntry) {
            this.dateEntry = dateEntry;
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
                        parentAccountEntry.useSummaryRowEntry();
                    }
                }
                else {
                    accountEntryInfo = new AccountEntryInfo(accountEntry);
                    accountEntryInfos.put(accountEntry, accountEntryInfo);
                }

                for (Map.Entry<SecurityNode, SecurityTransactionTracker> entry : accountTracker.getTransactionTrackers().entrySet()) {
                    SecurityTransactionTracker transactionTracker = entry.getValue();
                    addSecurityRowEntry(transactionTracker, accountEntryInfo);
                }
                
                if (accountEntryInfo.accountEntry == accountEntry) {
                    addCashRowEntry(accountEntryInfo);
                }
                
                isPossibleCashRow = false;
            }
        }
        
        super.setupRowsForAccountEntry(accountEntry, reportOutput, parentAccountEntry);
        
        if (isPossibleCashRow && (accountEntry.summaryRowEntry != null)) {
            AccountEntryInfo accountEntryInfo = accountEntryInfos.get(accountEntry);
            if (accountEntryInfo != null) {
                addCashRowEntry(accountEntryInfo);
            }
        }
    }
    
    protected void addSecurityRowEntry(SecurityTransactionTracker transactionTracker, AccountEntryInfo accountEntryInfo) {
        int rowIndex = accountEntryInfo.securityRowEntries.size();
        AccountEntry accountEntry = accountEntryInfo.accountEntry;
        RowEntry rowEntry = accountEntry.usePostChildAccountRowEntry(rowIndex);

        rowEntry.setRowTitle(transactionTracker.getSecurityNode().getSymbol());
        SecurityRowEntry securityRowEntry = new SecurityRowEntry(transactionTracker, accountEntry, rowEntry);
        accountEntryInfo.securityRowEntries.add(securityRowEntry);
    }
    
    protected void addCashRowEntry(AccountEntryInfo accountEntryInfo) {
        int rowIndex = accountEntryInfo.securityRowEntries.size();
        AccountEntry accountEntry = accountEntryInfo.accountEntry;
        RowEntry rowEntry = accountEntry.usePostChildAccountRowEntry(rowIndex);
        
        rowEntry.setRowTitle(ResourceSource.getString("Report.CashRow"));
        SecurityRowEntry cashRowEntry = new SecurityRowEntry(null, accountEntryInfo.accountEntry, rowEntry);
        accountEntryInfo.securityRowEntries.add(cashRowEntry);
    }
    

    @Override
    protected void setupColumnsForDateEntry(DateEntry dateEntry, AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
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
            DatedSummaryEntryInfo datedSummaryEntryInfo = createDatedSummaryEntryInfo(accountEntryInfo, dateEntryInfo, 
                    columnEntry, reportOutput, columnIndexBase);
            dateEntryInfo.datedSummaryEntryInfos.put(accountEntry, datedSummaryEntryInfo);

            for (SecurityRowEntry securityRowEntry : accountEntryInfo.securityRowEntries) {
                DatedSecurityEntryInfo securityEntryInfo = createDatedSecurityEntryInfo(securityRowEntry, dateEntryInfo, columnEntry, reportOutput, columnIndexBase);
                if (securityEntryInfo != null) {
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

    /*
                                    Total?
        COST_BASIS                  Yes
        GAIN                        Yes
        QUANTITY                    -
        PRICE                       -
        PERCENT_PORTFOLIO           Yes
        ANNUAL_RATE_OF_RETURN       Yes
        MARKET_VALUE                Yes
    
    */
    
    protected DateEntryInfo createDateEntryInfo(AccountEntryInfo accountEntryInfo, DateEntry dateEntry, 
            ColumnEntry columnEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        return new DateEntryInfo(dateEntry);
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
            // Cash entry...
            return new DatedSecurityEntryInfo(securityRowEntry, null, columnEntry);
        }
    }
    
    
    protected DatedSummaryEntryInfo createDatedSummaryEntryInfo(AccountEntryInfo accountEntryInfo, DateEntryInfo dateEntryInfo,
            ColumnEntry columnEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        DatedSummaryEntryInfo datedSummaryEntryInfo = new DatedSummaryEntryInfo(columnEntry);
        return datedSummaryEntryInfo;
    }
    
    
    protected void updateDatedSummaryEntryInfo(DatedSummaryEntryInfo datedSummaryEntryInfo, DatedSecurityEntryInfo datedSecurityEntryInfo,
            DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        Account account = datedSecurityEntryInfo.securityRowEntry.accountEntry.account;

        BigDecimal costBasis = datedSecurityEntryInfo.trackerDateEntry.getCostBasis();
        datedSummaryEntryInfo.totalCostBasis = datedSummaryEntryInfo.totalCostBasis.add(costBasis);
        dateEntryInfo.totalCostBasis = dateEntryInfo.totalCostBasis.add(costBasis);
        
        BigDecimal marketValue = datedSecurityEntryInfo.trackerDateEntry.getMarketValue(dateEntryInfo.dateEntry.endDate);
        marketValue = reportOutput.toMonetaryValue(marketValue, account);
        
        datedSummaryEntryInfo.totalMarketValue = datedSummaryEntryInfo.totalMarketValue.add(marketValue);
        dateEntryInfo.totalMarketValue = dateEntryInfo.totalMarketValue.add(marketValue);
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
        AccountEntryInfo accountEntryInfo = accountEntryInfos.get(accountEntry);
        DateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
        if ((accountEntryInfo != null) && (dateEntryInfo != null)) {
            DatedSummaryEntryInfo datedSummaryEntryInfo = dateEntryInfo.datedSummaryEntryInfos.get(accountEntry);
            if (datedSummaryEntryInfo != null) {
                // Summary...
                String cellValue = getSummaryEntryCellValue(datedSummaryEntryInfo, accountEntryInfo, dateEntryInfo, reportOutput);
                if (cellValue != null) {
                    ColumnEntry columnEntry = datedSummaryEntryInfo.columnEntry;
                    SecurityCellEntry cellEntry = new SecurityCellEntry(datedSummaryEntryInfo, accountEntry.summaryRowEntry, cellValue);
                    accountEntry.summaryRowEntry.setExpandedColumnCellValue(columnEntry, cellEntry);
                    accountEntry.summaryRowEntry.setNonExpandedColumnCellValue(columnEntry, cellEntry);
                }
            }

            final DatedSummaryEntryInfo finalDatedSummarEntryInfo = datedSummaryEntryInfo;
            accountEntryInfo.securityRowEntries.forEach((SecurityRowEntry securityRowEntry) -> {
                DatedSecurityEntryInfo datedSecurityEntryInfo = dateEntryInfo.datedSecurityEntryInfos.get(securityRowEntry);
                if (datedSecurityEntryInfo != null) {
                    ColumnEntry columnEntry = datedSecurityEntryInfo.columnEntry;
                    String cellValue;
                    if (datedSecurityEntryInfo.trackerDateEntry == null) {
                        cellValue = getCashEntryCellValue(datedSecurityEntryInfo, finalDatedSummarEntryInfo, dateEntryInfo, reportOutput);
                    }
                    else {
                        cellValue = getSecurityEntryCellValue(datedSecurityEntryInfo, dateEntryInfo, reportOutput);
                    }

                    if (cellValue != null) {
                        SecurityCellEntry cellEntry = new SecurityCellEntry(datedSecurityEntryInfo, securityRowEntry.rowEntry, cellValue);
                        securityRowEntry.rowEntry.setExpandedColumnCellValue(columnEntry, cellEntry);
                        securityRowEntry.rowEntry.setNonExpandedColumnCellValue(columnEntry, cellEntry);
                    }
                }
            });
        }
        
        super.updateCellValuesForDateEntryAccountEntry(dateEntry, accountEntry, reportOutput);
    }
    

    @Override
    protected void updateGrandTotalCellValue(DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        super.updateGrandTotalCellValue(dateEntry, reportOutput); //To change body of generated methods, choose Tools | Templates.
    }

    
    protected abstract String getColumnTitle(AccountEntry accountEntry, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput);

    
    protected String getCashEntryCellValue(DatedSecurityEntryInfo securityEntryInfo, DatedSummaryEntryInfo datedSummaryEntryInfo, 
            DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        Account account = securityEntryInfo.securityRowEntry.accountEntry.account;
        BigDecimal balance = account.getBalance(dateEntryInfo.dateEntry.endDate);
        return reportOutput.toMonetaryValueString(balance, account);
    }

    
    protected abstract String getSecurityEntryCellValue(DatedSecurityEntryInfo securityEntryInfo, DateEntryInfo dateEntryInfo, 
            ReportDataView.ReportOutput reportOutput);
    
    
    protected String getSummaryEntryCellValue(DatedSummaryEntryInfo datedSummaryEntryInfo, 
            AccountEntryInfo accountEntryInfo, DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        return null;
    }
}
