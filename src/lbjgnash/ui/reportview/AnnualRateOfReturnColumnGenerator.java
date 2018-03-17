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
public class AnnualRateOfReturnColumnGenerator extends SecuritiesColumnGenerator {

    @Override
    protected String getColumnTitle(AccountEntry accountEntry, DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        return ResourceSource.getString("Report.ColumnHeading.AnnualRateOfReturn");
    }

    @Override
    protected String getSecurityEntryCellValue(DatedSecurityEntryInfo securityEntryInfo, DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        BigDecimal yearAgoValueSum = securityEntryInfo.trackerDateEntry.getYearAgoValueSum(
                dateEntryInfo.dateEntry.endDate, reportOutput.getMinDaysForRateOfReturn());
        BigDecimal totalValue =  securityEntryInfo.trackerDateEntry.getMarketValue(dateEntryInfo.dateEntry.endDate);
        BigDecimal numerator = totalValue.subtract(yearAgoValueSum);
        return reportOutput.toPercentString(numerator, yearAgoValueSum);
    }

    @Override
    protected String getSummaryEntryCellValue(DatedSummaryEntryInfo datedAccountEntryInfo, AccountEntryInfo accountEntryInfo, DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        BigDecimal totalValue = datedAccountEntryInfo.totalMarketValue;
        BigDecimal numerator = totalValue.subtract(datedAccountEntryInfo.yearAgoValueSum);
        return reportOutput.toPercentString(numerator, datedAccountEntryInfo.yearAgoValueSum);
    }
    
    @Override
    protected String getGrandTotalCellValue(DateEntryInfo dateEntryInfo, ReportDataView.ReportOutput reportOutput) {
        BigDecimal totalValue = dateEntryInfo.totalMarketValue;
        BigDecimal numerator = totalValue.subtract(dateEntryInfo.yearAgoValueSum);
        return reportOutput.toPercentString(numerator, dateEntryInfo.yearAgoValueSum);
    }
}
