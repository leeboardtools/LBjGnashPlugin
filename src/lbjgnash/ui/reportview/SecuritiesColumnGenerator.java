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
 *
 * @author Albert Santos
 */
abstract class SecuritiesColumnGenerator extends ReportDataView.ColumnGenerator {

    @Override
    protected void setupAccountEntryRows(ReportDataView.ReportOutput reportOutput) {
        super.setupAccountEntryRows(reportOutput); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setupAccountEntryRows(ReportDataView.AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput) {
        AccountSecuritiesTracker accountTracker = accountEntry.getAccountSecuritiesTracker();
        if (accountTracker != null) {
            
        }
        super.setupAccountEntryRows(accountEntry, reportOutput);
    }

    @Override
    protected void updateGrandTotalCellValue(ReportDataView.DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        super.updateGrandTotalCellValue(dateEntry, reportOutput); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void updateDateEntryCellValues(ReportDataView.DateEntry dateEntry, ReportDataView.AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput) {
        super.updateDateEntryCellValues(dateEntry, accountEntry, reportOutput); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void updateDateEntryCellValues(ReportDataView.DateEntry dateEntry, ReportDataView.ReportOutput reportOutput) {
        super.updateDateEntryCellValues(dateEntry, reportOutput); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void updateCellValues(ReportDataView.ReportOutput reportOutput) {
        super.updateCellValues(reportOutput); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setupDateEntryColumns(ReportDataView.DateEntry dateEntry, ReportDataView.AccountEntry accountEntry, ReportDataView.ReportOutput reportOutput, int columnIndexBase) {
        super.setupDateEntryColumns(dateEntry, accountEntry, reportOutput, columnIndexBase); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setupDateEntryColumns(ReportDataView.ReportOutput reportOutput) {
        super.setupDateEntryColumns(reportOutput); //To change body of generated methods, choose Tools | Templates.
    }
    
}
