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

/**
 * The base abstract class for the objects responsible for generating {@link ColumnEntry}
 * objects.
 */
public abstract class ColumnGenerator {

    /**
     * This is called from {@link ReportDataView}.
     * @param reportOutput 
     */
    protected void setupAccountEntryRows(ReportDataView.ReportOutput reportOutput) {
        reportOutput.accountEntries.forEach((accountEntry) -> {
            setupRowsForAccountEntry(accountEntry, reportOutput, null);
        });
    }

    /**
     * This is normally overridden to handle the actual request for row entries for an account entry.
     * This implementation should normally be called from the overridden method because
     * this calls itself for all the child account entries.
     * @param accountEntry  The account entry to process.
     * @param reportOutput The report output.
     * @param parentAccountEntry    The parent account entry, may be <code>null</code>.
     */
    protected void setupRowsForAccountEntry(AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, AccountEntry parentAccountEntry) {
        accountEntry.childAccountEntries.forEach((childAccountEntry) -> {
            setupRowsForAccountEntry(childAccountEntry, reportOutput, accountEntry);
        });
    }

    /**
     * This is called from {@link ReportDataView}.
     * @param reportOutput 
     */
    protected void setupDateEntryColumns(ReportDataView.ReportOutput reportOutput) {
        reportOutput.dateEntries.forEach((dateEntry) -> {
            final int columnIndexBase = dateEntry.columnEntries.size();
            reportOutput.accountEntries.forEach((accountEntry) -> {
                setupColumnsForDateEntry(dateEntry, accountEntry, reportOutput, columnIndexBase);
            });
        });
    }

    /**
     * This is normally overridden to handle the actual processing of an account entry for
     * a date entry. This implementation should normally be called from the overridden method
     * because it calls itself for all the child account entries.
     * @param dateEntry The date entry being processed.
     * @param accountEntry  The account entry this is for.
     * @param reportOutput  The report output this is for.
     * @param columnIndexBase   The index to add to any column entry requests.
     */
    protected void setupColumnsForDateEntry(DateEntry dateEntry, AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        accountEntry.childAccountEntries.forEach((childAccountEntry) -> {
            setupColumnsForDateEntry(dateEntry, childAccountEntry, reportOutput, columnIndexBase);
        });
    }

    
    /**
     * This is called from {@link ReportDataView}.
     * @param reportOutput 
     */
    protected void updateCellValues(ReportDataView.ReportOutput reportOutput) {
        reportOutput.dateEntries.forEach((dateEntry) -> {
            if (reportOutput.grandTotalRowEntry != null) {
                updateDateEntryCellValues(dateEntry, reportOutput);
                updateGrandTotalCellValue(dateEntry, reportOutput);
            }
        });
    }

    protected void updateDateEntryCellValues(DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        reportOutput.accountEntries.forEach((accountEntry) -> {
            updateCellValuesForDateEntryAccountEntry(dateEntry, accountEntry, reportOutput);
        });
    }

    /**
     * This is normally overridden to handle the actual setting of the cell for a given
     * date entry and account entry. This implementation should normally be called from
     * the overridden method because it calls itself for all the child account entries.
     * @param dateEntry The date entry being processed.
     * @param accountEntry  The account entry this is for.
     * @param reportOutput The report output this is for.
     */
    protected void updateCellValuesForDateEntryAccountEntry(DateEntry dateEntry, AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput) {
        accountEntry.childAccountEntries.forEach((childAccountEntry) -> {
            updateCellValuesForDateEntryAccountEntry(dateEntry, childAccountEntry, reportOutput);
        });
    }

    protected void updateGrandTotalCellValue(DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
    }
    
}
