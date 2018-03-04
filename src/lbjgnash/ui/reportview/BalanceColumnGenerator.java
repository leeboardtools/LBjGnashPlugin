/*
 * Copyright 2018 albert.
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
import java.util.Map;
import java.util.TreeMap;

/**
 * Abstract class for the report columns that work off the standard account balance.
 */
abstract class BalanceColumnGenerator extends ReportDataView.ColumnGenerator {

    int maxIncludedAccountDepth;
    int minIncludedAccountDepth;
    final TreeMap<ReportDataView.DateEntry, ReportDataView.BalanceDateEntryInfo> dateEntryInfos = new TreeMap<>();
    BigDecimal subTotal;

    @Override
    protected void setupAccountEntryRows(ReportDataView.ReportOutput reportOutput) {
        maxIncludedAccountDepth = 0;
        minIncludedAccountDepth = Integer.MAX_VALUE;
        super.setupAccountEntryRows(reportOutput);
    }

    @Override
    protected void setupAccountEntryRows(ReportDataView.AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput) {
        if (accountEntry.isIncluded) {
            if (accountEntry.isAnyChildAccountIncluded()) {
                accountEntry.useAccountRowEntry(ReportDataView.AccountRowEntry.POST_CHILD_ACCOUNT);
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
    protected void setupDateEntryColumns(ReportDataView.DateEntry dateEntry, ReportDataView.AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        if (!accountEntry.isIncluded) {
            super.setupDateEntryColumns(dateEntry, accountEntry, reportOutput, columnIndexBase);
            return;
        }
        BigDecimal previousSubTotal = subTotal;
        subTotal = BigDecimal.ZERO;
        try {
            //int columnOffset = maxIncludedAccountDepth - accountEntry.accountDepth;
            int columnOffset = 0;
            final ReportDataView.ColumnEntry columnEntry = dateEntry.getColumnEntryAtIndex(columnOffset + columnIndexBase);
            super.setupDateEntryColumns(dateEntry, accountEntry, reportOutput, columnIndexBase);
            ReportDataView.RowEntry rowEntry = null;
            if (accountEntry.isAnyChildAccountIncluded()) {
                rowEntry = accountEntry.getAccountRowEntry(ReportDataView.AccountRowEntry.POST_CHILD_ACCOUNT);
            }
            BigDecimal balance = getInternalAccountBalance(rowEntry, columnEntry, accountEntry, dateEntry, reportOutput);
            subTotal = subTotal.add(balance);
            ReportDataView.BalanceDateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
            if (dateEntryInfo == null) {
                // First time, gotta set up the column entry.
                dateEntryInfo = createDateEntryInfo(accountEntry, dateEntry, columnEntry, columnIndexBase);
                dateEntryInfos.put(dateEntry, dateEntryInfo);
                columnEntry.treeTableColumn.setText(getColumnTitle(columnOffset, accountEntry, dateEntry, reportOutput));
                final ReportDataView.BalanceDateEntryInfo cellDateEntryInfo = dateEntryInfo;
                columnEntry.treeTableColumn.setCellFactory((javafx.scene.control.TreeTableColumn<lbjgnash.ui.reportview.ReportDataView.RowEntry, lbjgnash.ui.reportview.ReportDataView.CellEntry> column) -> {
                    return new ReportDataView.BalanceTreeCell(this, cellDateEntryInfo);
                });
            }
            ReportDataView.BalanceAccountEntryInfo accountEntryInfo = createAccountEntryInfo(accountEntry, dateEntry, rowEntry, columnEntry, subTotal);
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

    protected ReportDataView.BalanceDateEntryInfo createDateEntryInfo(ReportDataView.AccountEntry accountEntry, ReportDataView.DateEntry dateEntry, ReportDataView.ColumnEntry columnEntry, int columnIndexBase) {
        return new ReportDataView.BalanceDateEntryInfo(columnIndexBase);
    }

    protected ReportDataView.BalanceAccountEntryInfo createAccountEntryInfo(ReportDataView.AccountEntry accountEntry, ReportDataView.DateEntry dateEntry, ReportDataView.RowEntry rowEntry, ReportDataView.ColumnEntry columnEntry, BigDecimal balance) {
        return new ReportDataView.BalanceAccountEntryInfo(accountEntry, rowEntry, columnEntry, balance);
    }

    @Override
    protected void updateDateEntryCellValues(ReportDataView.DateEntry dateEntry, ReportDataView.AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput) {
        if (accountEntry.isIncluded) {
            ReportDataView.BalanceDateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
            if (dateEntryInfo != null) {
                ReportDataView.BalanceAccountEntryInfo accountEntryInfo = dateEntryInfo.accountEntryInfos.get(accountEntry);
                String value = getAccountEntryCellValue(accountEntryInfo, dateEntry, reportOutput);
                if (accountEntryInfo.rowEntry != null) {
                    ReportDataView.CellEntry cellEntry = new ReportDataView.BalanceCellEntry(accountEntryInfo, accountEntryInfo.rowEntry, value);
                    accountEntryInfo.rowEntry.setExpandedColumnCellValue(accountEntryInfo.columnEntry, cellEntry);
                    accountEntryInfo.rowEntry.setNonExpandedColumnCellValue(accountEntryInfo.columnEntry, cellEntry);
                }
                if (accountEntry.summaryRowEntry != null) {
                    ReportDataView.CellEntry cellEntry = new ReportDataView.BalanceCellEntry(accountEntryInfo, accountEntry.summaryRowEntry, value);
                    accountEntry.summaryRowEntry.setNonExpandedColumnCellValue(accountEntryInfo.columnEntry, cellEntry);
                }
            }
        }
        super.updateDateEntryCellValues(dateEntry, accountEntry, reportOutput);
    }

    @Override
    protected void updateGrandTotalCellValue(ReportDataView.DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        ReportDataView.BalanceDateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
        if (dateEntryInfo != null) {
            String value = getGrandTotalCellValue(dateEntryInfo, dateEntry, reportOutput);
            if (StringUtil.isNonEmpty(value)) {
                ReportDataView.ColumnEntry columnEntry = dateEntry.getColumnEntryAtIndex(dateEntryInfo.columnIndexBase);
                ReportDataView.CellEntry cellEntry = new ReportDataView.BalanceCellEntry(null, reportOutput.grandTotalRowEntry, value);
                reportOutput.grandTotalRowEntry.setNonExpandedColumnCellValue(columnEntry, cellEntry);
            }
        }
    }

    protected abstract String getColumnTitle(int columnOffset, ReportDataView.AccountEntry accountEntry, ReportDataView.DateEntry dateEntry, ReportDataView.ReportOutput reportOutput);

    protected BigDecimal getInternalAccountBalance(ReportDataView.RowEntry rowEntry, ReportDataView.ColumnEntry columnEntry, ReportDataView.AccountEntry accountEntry, ReportDataView.DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return accountEntry.account.getBalance(dateEntry.endDate);
    }

    protected abstract String getAccountEntryCellValue(ReportDataView.BalanceAccountEntryInfo accountInfo, ReportDataView.DateEntry dateEntry, ReportDataView.ReportOutput reportOutput);

    protected String getGrandTotalCellValue(ReportDataView.BalanceDateEntryInfo dateEntryInfo, ReportDataView.DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return null;
    }

    protected ReportDataView.BalanceDateEntryInfo getReferenceDateEntryInfo(ReportDataView.DateEntry dateEntry, ReportDataView.ReferencePeriodType periodType) {
        switch (periodType) {
            case PREVIOUS:
                Map.Entry<ReportDataView.DateEntry, ReportDataView.BalanceDateEntryInfo> entry = dateEntryInfos.lowerEntry(dateEntry);
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
