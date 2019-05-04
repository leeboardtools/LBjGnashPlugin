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
 *
 * @author Albert Santos
 */
public class NetGainColumnGenerator extends SecuritiesColumnGenerator {

    @Override
    protected String getColumnTitle(AccountEntry accountEntry, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return ResourceSource.getString("Report.ColumnHeading.NetGain");
    }

    @Override
    protected String getSecurityEntryCellValue(DatedSecurityEntryInfo securityEntryInfo, DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        BigDecimal value = securityEntryInfo.getNetGain(dateEntryInfo.dateEntry.endDate);
        return reportOutput.toMonetaryValueString(value, securityEntryInfo.securityRowEntry.accountEntry.account);
    }

    @Override
    protected String getSummaryEntryCellValue(DatedSummaryEntryInfo datedAccountEntryInfo, DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        BigDecimal value = datedAccountEntryInfo.getNetGain();
        return reportOutput.toMonetaryValueString(value, datedAccountEntryInfo.reportingAccountEntryInfo.accountEntry.account);
    }
    
    @Override
    protected String getGrandTotalCellValue(DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        BigDecimal value = dateEntryInfo.getNetGain();
        return reportOutput.toMonetaryValueString(value, null);
    }
}
