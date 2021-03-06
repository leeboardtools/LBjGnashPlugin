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

import com.leeboardtools.util.StringUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javafx.scene.control.cell.TextFieldTreeTableCell;

/**
 * Abstract class for the report columns that work off the standard account balance.
 */
abstract class BalanceColumnGenerator extends ColumnGenerator {

    int maxIncludedAccountDepth;
    int minIncludedAccountDepth;
    final TreeMap<DateEntry, BalanceDateEntryInfo> dateEntryInfos = new TreeMap<>();
    BigDecimal subTotal;

    
    protected static class BalanceAccountEntryInfo {
        final AccountEntry accountEntry;
        final RowEntry rowEntry;
        final ColumnEntry columnEntry;
        final BigDecimal balance;
        
        protected BalanceAccountEntryInfo(AccountEntry accountEntry, RowEntry rowEntry, ColumnEntry columnEntry, BigDecimal balance) {
            this.accountEntry = accountEntry;
            this.rowEntry = rowEntry;
            this.columnEntry = columnEntry;
            this.balance = balance;
        }
    }
    
    
    /**
     * Tracks the balance total for a date entry.
     */
    protected static class BalanceDateEntryInfo {
        final Map<AccountEntry, BalanceAccountEntryInfo> accountEntryInfos = new HashMap<>();
        BigDecimal totalBalance = BigDecimal.ZERO;
        final int columnIndexBase;

        public BalanceDateEntryInfo(int columnIndexBase) {
            this.columnIndexBase = columnIndexBase;
        }
    }
    
    
    protected static class BalanceCellEntry extends CellEntry {
        final BalanceAccountEntryInfo accountEntryInfo;
        String basicStyle = ReportDataView.STYLE_BALANCE_VALUE;

        protected BalanceCellEntry(BalanceAccountEntryInfo accountEntryInfo,
            RowEntry rowEntry, String value) {
            super(rowEntry, value);
            this.accountEntryInfo = accountEntryInfo;
        }
    }
    
    
    /**
     * Used to set the CSS styles for the individual cells.
     */
    protected static class BalanceTreeCell extends TextFieldTreeTableCell<RowEntry, CellEntry> {
        final BalanceColumnGenerator generator;
        final BalanceDateEntryInfo dateEntryInfo;
        
        BalanceTreeCell(BalanceColumnGenerator generator, BalanceDateEntryInfo dateEntryInfo) {
            this.generator = generator;
            this.dateEntryInfo = dateEntryInfo;
        }

        @Override
        public void updateItem(CellEntry item, boolean empty) {
            super.updateItem(item, empty);            
            
            if ((item instanceof BalanceCellEntry) && !empty) {
                BalanceCellEntry balanceCellEntry = (BalanceCellEntry)item;
                
                getStyleClass().add(ReportDataView.STYLE_CELL);
                getStyleClass().remove(ReportDataView.STYLE_SUBTOTAL);
                getStyleClass().remove(ReportDataView.STYLE_SUMMARY);
                getStyleClass().remove(ReportDataView.STYLE_GRAND_TOTAL);
                
                getStyleClass().add(balanceCellEntry.basicStyle);
                
                if (balanceCellEntry.accountEntryInfo != null) {
                    if (balanceCellEntry.rowEntry == balanceCellEntry.accountEntryInfo.accountEntry.summaryRowEntry) {
                        getStyleClass().add(ReportDataView.STYLE_SUMMARY);
                    }
                    else if (balanceCellEntry.rowEntry == balanceCellEntry.accountEntryInfo.rowEntry) {
                        getStyleClass().add(ReportDataView.STYLE_SUBTOTAL);
                    }
                }
                else {
                    getStyleClass().add(ReportDataView.STYLE_GRAND_TOTAL);
                }
            }
        }
    }
    
    
    @Override
    protected void setupAccountEntryRows(ReportDataView.ReportOutput reportOutput) {
        maxIncludedAccountDepth = 0;
        minIncludedAccountDepth = Integer.MAX_VALUE;
        super.setupAccountEntryRows(reportOutput);
    }

    
    @Override
    protected void setupRowsForAccountEntry(AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, AccountEntry parentAccountEntry) {
        if (accountEntry.isIncluded) {
            accountEntry.useSummaryRowEntry();
            if (accountEntry.isAnyChildAccountIncluded()) {
                accountEntry.usePostChildRowEntry();
            }
            
            if (accountEntry.accountDepth > maxIncludedAccountDepth) {
                maxIncludedAccountDepth = accountEntry.accountDepth;
            }
            if (accountEntry.accountDepth < minIncludedAccountDepth) {
                minIncludedAccountDepth = accountEntry.accountDepth;
            }
        }
        super.setupRowsForAccountEntry(accountEntry, reportOutput, parentAccountEntry);
    }

    
    @Override
    protected void setupColumnsForDateEntry(DateEntry dateEntry, AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        if (!accountEntry.isIncluded) {
            super.setupColumnsForDateEntry(dateEntry, accountEntry, reportOutput, columnIndexBase);
            return;
        }
        
        
        BigDecimal previousSubTotal = subTotal;
        subTotal = BigDecimal.ZERO;
        try {
            //int columnOffset = maxIncludedAccountDepth - accountEntry.accountDepth;
            int columnOffset = 0;
            final ColumnEntry columnEntry = dateEntry.getColumnEntryAtIndex(columnOffset + columnIndexBase);
            
            super.setupColumnsForDateEntry(dateEntry, accountEntry, reportOutput, columnIndexBase);
            
            RowEntry rowEntry = null;
            if (accountEntry.isAnyChildAccountIncluded()) {
                rowEntry = accountEntry.usePostChildRowEntry();
            }
            
            BigDecimal balance = getInternalAccountBalance(rowEntry, columnEntry, accountEntry, dateEntry, reportOutput);
            subTotal = subTotal.add(balance);
            
            BalanceDateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
            if (dateEntryInfo == null) {
                // First time, gotta set up the column entry.
                dateEntryInfo = createDateEntryInfo(accountEntry, dateEntry, columnEntry, columnIndexBase);
                dateEntryInfos.put(dateEntry, dateEntryInfo);
                
                columnEntry.treeTableColumn.setText(getColumnTitle(columnOffset, accountEntry, dateEntry, reportOutput));
                
                final BalanceDateEntryInfo cellDateEntryInfo = dateEntryInfo;
                columnEntry.treeTableColumn.setCellFactory((javafx.scene.control.TreeTableColumn<lbjgnash.ui.reportview.RowEntry, lbjgnash.ui.reportview.CellEntry> column) -> {
                    return new BalanceTreeCell(this, cellDateEntryInfo);
                });
            }
            
            BalanceAccountEntryInfo accountEntryInfo = createAccountEntryInfo(accountEntry, dateEntry, rowEntry, columnEntry, subTotal);
            dateEntryInfo.accountEntryInfos.put(accountEntry, accountEntryInfo);
            if (accountEntry.includeDepth == 1) {
                dateEntryInfo.totalBalance = dateEntryInfo.totalBalance.add(subTotal);
            }
        } finally {
            if (previousSubTotal != null) {
                previousSubTotal = previousSubTotal.add(subTotal);
            }
            subTotal = previousSubTotal;
        }
    }

    
    protected BalanceDateEntryInfo createDateEntryInfo(AccountEntry accountEntry, DateEntry dateEntry, ColumnEntry columnEntry, int columnIndexBase) {
        return new BalanceDateEntryInfo(columnIndexBase);
    }

    
    protected BalanceAccountEntryInfo createAccountEntryInfo(AccountEntry accountEntry, DateEntry dateEntry, RowEntry rowEntry, ColumnEntry columnEntry, BigDecimal balance) {
        return new BalanceAccountEntryInfo(accountEntry, rowEntry, columnEntry, balance);
    }

    
    @Override
    protected void updateCellValuesForDateEntryAccountEntry(DateEntry dateEntry, AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput) {
        if (accountEntry.isIncluded) {
            BalanceDateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
            if (dateEntryInfo != null) {
                
                BalanceAccountEntryInfo accountEntryInfo = dateEntryInfo.accountEntryInfos.get(accountEntry);
                String value = getAccountEntryCellValue(accountEntryInfo, dateEntry, reportOutput);

                if (accountEntryInfo.rowEntry != null) {
                    CellEntry cellEntry = new BalanceCellEntry(accountEntryInfo, accountEntryInfo.rowEntry, value);
                    accountEntryInfo.rowEntry.setExpandedColumnCellValue(accountEntryInfo.columnEntry, cellEntry);
                    accountEntryInfo.rowEntry.setNonExpandedColumnCellValue(accountEntryInfo.columnEntry, cellEntry);
                }

                if (accountEntry.summaryRowEntry != null) {
                    CellEntry cellEntry = new BalanceCellEntry(accountEntryInfo, accountEntry.summaryRowEntry, value);
                    accountEntry.summaryRowEntry.setNonExpandedColumnCellValue(accountEntryInfo.columnEntry, cellEntry);
                }
            }
        }
        super.updateCellValuesForDateEntryAccountEntry(dateEntry, accountEntry, reportOutput);
    }

    
    @Override
    protected void updateGrandTotalCellValue(DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        BalanceDateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
        if (dateEntryInfo != null) {
            String value = getGrandTotalCellValue(dateEntryInfo, dateEntry, reportOutput);
            if (StringUtil.isNonEmpty(value)) {
                ColumnEntry columnEntry = dateEntry.getColumnEntryAtIndex(dateEntryInfo.columnIndexBase);
                CellEntry cellEntry = new BalanceCellEntry(null, reportOutput.grandTotalRowEntry, value);
                reportOutput.grandTotalRowEntry.setNonExpandedColumnCellValue(columnEntry, cellEntry);
            }
        }
    }
    

    protected abstract String getColumnTitle(int columnOffset, AccountEntry accountEntry, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput);

    
    protected BigDecimal getInternalAccountBalance(RowEntry rowEntry, ColumnEntry columnEntry, AccountEntry accountEntry, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        if (dateEntry.endDate.equals(dateEntry.startDate)) {
            return accountEntry.account.getBalance(dateEntry.endDate);
        }
        else {
            return accountEntry.account.getBalance(dateEntry.startDate, dateEntry.endDate);
        }
    }

    
    protected abstract String getAccountEntryCellValue(BalanceAccountEntryInfo accountInfo, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput);

    
    protected String getGrandTotalCellValue(BalanceDateEntryInfo dateEntryInfo, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return null;
    }

    
    protected BalanceDateEntryInfo getReferenceDateEntryInfo(DateEntry dateEntry, ReportDataView.ReferencePeriodType periodType) {
        switch (periodType) {
            case PREVIOUS:
                Map.Entry<DateEntry, BalanceDateEntryInfo> entry = dateEntryInfos.lowerEntry(dateEntry);
                if (entry == null) {
                    entry = dateEntryInfos.firstEntry();
                }
                return entry.getValue();
            case NEWEST:
                return dateEntryInfos.lastEntry().getValue();
            case OLDEST:
                return dateEntryInfos.firstEntry().getValue();
            default:
                throw new AssertionError(periodType.name());
        }
    }
    
    
}
