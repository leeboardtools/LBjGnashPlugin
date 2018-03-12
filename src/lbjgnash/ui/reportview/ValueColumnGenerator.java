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

/**
 * Reports the current balance as of the end date as the cell's value.
 */
class ValueColumnGenerator extends BalanceColumnGenerator {

    @Override
    protected String getColumnTitle(int columnOffset, AccountEntry accountEntry, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        if (columnOffset == 0) {
            return ResourceSource.getString("Report.ColumnHeading.Value");
        }
        return "";
    }

    @Override
    protected String getAccountEntryCellValue(BalanceAccountEntryInfo accountInfo, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return reportOutput.toMonetaryValueString(accountInfo.balance, accountInfo.accountEntry.account);
    }

    @Override
    protected String getGrandTotalCellValue(BalanceDateEntryInfo dateEntryInfo, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return reportOutput.toMonetaryValueString(dateEntryInfo.totalBalance, null);
    }
    
}
