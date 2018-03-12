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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;

/**
 * This an individual row for the {@link TreeTableView}.
 */
public class RowEntry {

    final StringProperty rowTitle = new SimpleStringProperty(this, "rowTitle", null);
    final List<ObjectProperty<CellEntry>> expandedColumnCellProperties = new ArrayList<>();
    final List<ObjectProperty<CellEntry>> nonExpandedColumnCellProperties = new ArrayList<>();

    public void setRowTitle(String title) {
        this.rowTitle.set(title);
    }

    public StringProperty getRowTitle() {
        return this.rowTitle;
    }

    public ObjectProperty<CellEntry> getColumnCellProperties(int index, TreeItem<RowEntry> entry) {
        List<ObjectProperty<CellEntry>> propertiesList = (entry.isExpanded()) ? expandedColumnCellProperties : nonExpandedColumnCellProperties;
        if (index >= propertiesList.size()) {
            return null;
        }
        return propertiesList.get(index);
    }

    public void setExpandedColumnCellValue(ColumnEntry columnEntry, CellEntry cellEntry) {
        setColumnCellValue(expandedColumnCellProperties, columnEntry.columnIndex, cellEntry);
    }

    public void setNonExpandedColumnCellValue(ColumnEntry columnEntry, CellEntry cellEntry) {
        setColumnCellValue(nonExpandedColumnCellProperties, columnEntry.columnIndex, cellEntry);
    }

    public void setColumnCellValue(List<ObjectProperty<CellEntry>> properties, int index, CellEntry cellEntry) {
        while (index >= properties.size()) {
            properties.add(null);
        }
        
        ObjectProperty<CellEntry> property = properties.get(index);
        if (property == null) {
            property = new SimpleObjectProperty();
            properties.set(index, property);
        }
        property.set(cellEntry);
    }
    
}
