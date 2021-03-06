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

/**
 * Reports the change in current balance from the reference period's balance.
 */
class DeltaPeriodColumnGenerator extends BalanceColumnGenerator {

    protected final ReportDataView.ReferencePeriodType referencePeriodType;

    protected DeltaPeriodColumnGenerator(ReportDataView.ReferencePeriodType referencePeriodType) {
        this.referencePeriodType = referencePeriodType;
    }

    @Override
    protected String getColumnTitle(int columnOffset, AccountEntry accountEntry, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        if (columnOffset == 0) {
            return ResourceSource.getString("Report.ColumnHeading.DeltaPreviousPeriod");
        }
        return "";
    }

    @Override
    protected String getAccountEntryCellValue(BalanceAccountEntryInfo accountInfo, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        BalanceDateEntryInfo dateEntryInfo = dateEntryInfos.get(dateEntry);
        BalanceDateEntryInfo refDateEntryInfo = getReferenceDateEntryInfo(dateEntry, referencePeriodType);
        if (dateEntryInfo == refDateEntryInfo) {
            return getBaselineAccountEntryCellValue(accountInfo, dateEntry, reportOutput);
        }
        if ((dateEntryInfo != null) && (refDateEntryInfo != null)) {
            BalanceAccountEntryInfo refAccountInfo = refDateEntryInfo.accountEntryInfos.get(accountInfo.accountEntry);
            if (refAccountInfo != null) {
                return getDeltaAccountEntryCellValue(accountInfo, refAccountInfo, dateEntryInfo, refDateEntryInfo, reportOutput);
            }
        }
        return "-";
    }

    @Override
    protected String getGrandTotalCellValue(BalanceDateEntryInfo dateEntryInfo, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        BalanceDateEntryInfo refDateEntryInfo = getReferenceDateEntryInfo(dateEntry, referencePeriodType);
        if (dateEntryInfo == refDateEntryInfo) {
            return getBaselineGrandTotalCellValue(dateEntryInfo, dateEntry, reportOutput);
        }
        if ((dateEntryInfo != null) && (refDateEntryInfo != null)) {
            return getDeltaGrandTotalCellValue(dateEntryInfo, refDateEntryInfo, reportOutput);
        }
        return null;
    }

    protected String getBaselineAccountEntryCellValue(BalanceAccountEntryInfo accountInfo, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return reportOutput.toMonetaryValueString(accountInfo.balance, accountInfo.accountEntry.account);
    }

    protected String getBaselineGrandTotalCellValue(BalanceDateEntryInfo dateEntryInfo, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return reportOutput.toMonetaryValueString(dateEntryInfo.totalBalance, null);
    }

    protected String getDeltaAccountEntryCellValue(BalanceAccountEntryInfo accountInfo, BalanceAccountEntryInfo refAccountInfo, BalanceDateEntryInfo dateEntryInfo, BalanceDateEntryInfo refDateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        if (accountInfo == refAccountInfo) {
            return null;
        }
        BigDecimal balance = accountInfo.balance.subtract(refAccountInfo.balance);
        return reportOutput.toMonetaryValueString(balance, accountInfo.accountEntry.account);
    }

    protected String getDeltaGrandTotalCellValue(BalanceDateEntryInfo dateEntryInfo, BalanceDateEntryInfo refDateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        if (dateEntryInfo == refDateEntryInfo) {
            return null;
        }
        BigDecimal totalBalance = dateEntryInfo.totalBalance.subtract(refDateEntryInfo.totalBalance);
        return reportOutput.toMonetaryValueString(totalBalance, null);
    }
    
}
