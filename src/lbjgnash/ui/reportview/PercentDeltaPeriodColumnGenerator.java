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

import java.math.BigDecimal;

/**
 * Reports the percent change in current balance from the reference period's balance.
 * @author albert
 */
class PercentDeltaPeriodColumnGenerator extends DeltaPeriodColumnGenerator {
    
    public PercentDeltaPeriodColumnGenerator(ReportDataView.ReferencePeriodType referencePeriodType) {
        super(referencePeriodType);
    }

    @Override
    protected String getDeltaAccountEntryCellValue(ReportDataView.BalanceAccountEntryInfo accountInfo, ReportDataView.BalanceAccountEntryInfo refAccountInfo, ReportDataView.BalanceDateEntryInfo dateEntryInfo, ReportDataView.BalanceDateEntryInfo refDateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        if (accountInfo == refAccountInfo) {
            return null;
        }
        BigDecimal balance = accountInfo.balance.subtract(refAccountInfo.balance);
        return reportOutput.toPercentString(balance, refAccountInfo.balance);
    }

    @Override
    protected String getDeltaGrandTotalCellValue(ReportDataView.BalanceDateEntryInfo dateEntryInfo, ReportDataView.BalanceDateEntryInfo refDateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        if (dateEntryInfo == refDateEntryInfo) {
            return null;
        }
        BigDecimal totalBalance = dateEntryInfo.totalBalance.subtract(refDateEntryInfo.totalBalance);
        return reportOutput.toPercentString(totalBalance, refDateEntryInfo.totalBalance);
    }
    
}
