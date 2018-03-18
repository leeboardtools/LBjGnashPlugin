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
package com.leeboardtools.util;

import java.io.IOException;
import java.io.Writer;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;

/**
 * Some utilities for working with CSV files. Unless otherwise noted this attempts to
 * follow RFC 4180 {@link https://tools.ietf.org/html/rfc4180}.
 * @author Albert Santos
 */
public class CSVUtil {
    public static final String CRLF = "\r\n";
    
    public static final String CSV_EXTENSION = ".csv";
    public static final String CSV_WILDCARD_EXTENSION = "*.csv";
    
    /**
     * Converts a string to a CSV field, enclosing it in double-quotes if it contains
     * characters that require it to be enclosed in double-quotes.
     * @param text  The text.
     * @return text as a CSV field.
     */
    public static String toCSVField(String text) {
        // We need to enclose the field if it has a comma, a quotation mark, or a CRLF.
        if (text == null) {
            return "";
        }
        
        final int count = text.length();
        for (int i = 0; i < count; ++i) {
            char ch = text.charAt(i);
            if ((ch == ',') || (ch == '"') || (ch == '\r')) {
                return encloseInQuotes(text);
            }
        }
        return text;
    }
    
    /**
     * Encloses a string in double-quotes, escaping any double-quotes with 
     * a double-quote.
     * @param text  The text to enclose.
     * @return The text enclosed in double-quotes.
     */
    public static String encloseInQuotes(String text) {
        final StringBuilder builder = new StringBuilder();
        builder.append('"');
        
        int startIndex = 0;
        int quoteIndex = text.indexOf('"', startIndex);
        while (quoteIndex >= 0) {
            builder.append(text, startIndex, quoteIndex + 1);
            builder.append('"');
            startIndex = quoteIndex + 1;
            quoteIndex = text.indexOf('"', startIndex);
        }
        builder.append(text, startIndex, text.length());
        
        builder.append('"');
        return builder.toString();
    }

    
    
    /**
     * Writes the contents of a {@link TreeTableView} to a {@link Writer} in CSV format.
     * @param <T>   The data type of the tree table view.
     * @param treeTableView  The tree table view.
     * @param writer The writer.
     * @throws IOException if thrown by the writer.
     */
    public static <T> void treeTableViewToCSV(TreeTableView<T> treeTableView, Writer writer) throws IOException {
        int columnCount = treeTableView.getColumns().size();
        for (int c = 0; c < columnCount; ++c) {
            TreeTableColumn<T, ?> column = treeTableView.getColumns().get(c);
            treeTableColumnHeaderToCSV(treeTableView, c == 0, column, writer);
        }
        writer.append(CRLF);
        
        int rowCount = treeTableView.getExpandedItemCount();
        for (int r = 0; r < rowCount; ++r) {
            TreeItem<T> treeItem = treeTableView.getTreeItem(r);
            for (int c = 0; c < columnCount; ++c) {
                TreeTableColumn<T, ?> column = treeTableView.getColumns().get(c);
                treeTableColumnToCSV(treeTableView, c == 0, treeItem, column, writer);
            }
            writer.append(CRLF);
        }
    }

    
    protected static <T> void treeTableColumnHeaderToCSV(TreeTableView<T> treeTableView, boolean isFirstColumn,
            TreeTableColumn<T, ?> column, Writer writer) throws IOException {
        if (!column.getColumns().isEmpty()) {
            for (TreeTableColumn<T, ?> subColumn : column.getColumns()) {
                treeTableColumnHeaderToCSV(treeTableView, isFirstColumn, subColumn, writer);
                isFirstColumn = false;
            }
            return;
        }
        
        String valueText = column.getText();
        if (!isFirstColumn) {
            writer.append(',');
        }
        writer.append(toCSVField(valueText));
    }

    
    protected static <T> void treeTableColumnToCSV(TreeTableView<T> treeTableView, boolean isFirstColumn,
            TreeItem<T> treeItem, TreeTableColumn<T, ?> column, Writer writer) throws IOException {
        if (!column.getColumns().isEmpty()) {
            for (TreeTableColumn<T, ?> subColumn : column.getColumns()) {
                treeTableColumnToCSV(treeTableView, isFirstColumn, treeItem, subColumn, writer);
                isFirstColumn = false;
            }
            return;
        }
        
        String valueText = "";
        ObservableValue<?> observableValue = column.getCellObservableValue(treeItem);
        if (observableValue != null) {
            if (observableValue.getValue() != null) {
                valueText = observableValue.getValue().toString();
            }
        }
        
        if (!isFirstColumn) {
            writer.append(',');
        }
        writer.append(toCSVField(valueText));
    }
}
