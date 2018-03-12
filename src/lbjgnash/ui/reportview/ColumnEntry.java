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

import javafx.scene.control.TreeTableColumn;

/**
 * This represents an individual {@link TreeTableColumn}.
 */
public class ColumnEntry {

    protected TreeTableColumn<RowEntry, CellEntry> treeTableColumn = new TreeTableColumn<>();
    // This is set as the column entries of the DateEntries are added
    // to the ReportOutput's columnEntries list.
    int columnIndex;

    public ColumnEntry() {
        treeTableColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<RowEntry, CellEntry> param) -> {
            if (param.getValue().getValue() == null) {
                return null;
            }
            return param.getValue().getValue().getColumnCellProperties(columnIndex, param.getValue());
        });
    }

    public void setExpandedRowValue(RowEntry rowEntry, CellEntry cellEntry) {
        rowEntry.setExpandedColumnCellValue(this, cellEntry);
    }

    public void setNonExpandedRowValue(RowEntry rowEntry, CellEntry cellEntry) {
        rowEntry.setNonExpandedColumnCellValue(this, cellEntry);
    }
    
}
