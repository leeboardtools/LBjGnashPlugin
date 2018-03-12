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
 * Used as the class for the cells of the {@link TreeTableView} so we can
 * associate data accessible to individual {@link Cell}s.
 */
public class CellEntry {

    final RowEntry rowEntry;
    final String value;

    public CellEntry(RowEntry rowEntry, String value) {
        this.rowEntry = rowEntry;
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
    
}
