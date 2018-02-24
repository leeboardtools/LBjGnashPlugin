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
package com.leeboardtools.json;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Writes out JSON objects and arrays to a {@link Writer}.
 */
public class JSONWriter {

    private final Writer writer;
    private final List<ScopeInfo> scopeInfoStack = new ArrayList<>();
    private ScopeInfo activeScopeInfo;
    private String currentIndentString = "";
    private String arrayStart;
    private String arrayEnd;
    private String objectStart;
    private String objectEnd;
    private String nameValueSeparator;
    private String entrySeparator;
    private int indentAmount;
    private String spacesPerIndent;

    static class ScopeInfo {

        int itemCount = 0;

        ScopeInfo() {
        }
    }

    /**
     * Constructor.
     * @param writer    The writer to write to, not <code>null</code>.
     * @param indentAmount The amount to indent entries, set to 0 for the most compact.
     */
    public JSONWriter(Writer writer, int indentAmount) {
        Objects.requireNonNull(writer);
        this.writer = writer;
        pushIndent();
        setIndentAmount(indentAmount);
    }

    /**
     * Changes the indentation level.
     * @param indentAmount The amount to indent entries, set to 0 for the most compact.
     */
    public final void setIndentAmount(int indentAmount) {
        if (indentAmount <= 0) {
            this.indentAmount = 0;
            this.arrayStart = "[";
            this.arrayEnd = "]";
            this.objectStart = "{";
            this.objectEnd = "}";
            this.nameValueSeparator = ":";
            this.entrySeparator = ",";
            this.spacesPerIndent = null;
            this.currentIndentString = "";
        } else {
            this.indentAmount = indentAmount;
            this.arrayStart = "[\n";
            this.arrayEnd = "]";
            this.objectStart = "{\n";
            this.objectEnd = "}";
            this.nameValueSeparator = " : ";
            this.entrySeparator = ",\n";
            char[] charsPerIndent = new char[this.indentAmount];
            Arrays.fill(charsPerIndent, ' ');
            this.spacesPerIndent = new String(charsPerIndent);
            this.currentIndentString = "";
            for (int i = 1; i < this.scopeInfoStack.size(); ++i) {
                this.currentIndentString += this.spacesPerIndent;
            }
        }
    }

    /**
     * Writes out a {@link JSONObject}.
     * @param object    The object to write.
     * @throws IOException on I/O errors.
     */
    public void writeJSONObject(JSONObject object) throws IOException {
        startObject();
        for (JSONObject.NameValue nameValue : object) {
            startNewEntry();
            writeName(nameValue.getName());
            writeJSONValue(nameValue.getValue());
        }
        endObject();
    }

    /**
     * Writes out a {@link JSONValue}.
     * @param value The value to write.
     * @throws IOException on I/O errors.
     */
    public void writeJSONValue(JSONValue value) throws IOException {
        switch (value.getValueType()) {
            case STRING:
                writeJSONText(JSONLite.encodeToJSONString(value.getStringValue()));
                break;
            case NUMBER:
                final int intValue = value.getIntValue();
                final double doubleValue = value.getDoubleValue();
                if (intValue == doubleValue) {
                    writeJSONText(Integer.toString(intValue));
                } else {
                    writeJSONText(Double.toString(doubleValue));
                }
                break;
            case OBJECT:
                writeJSONObject(value.getObjectValue());
                break;
            case ARRAY:
                writeJSONArray(value.getArrayValue());
                break;
            case TRUE:
                writeJSONText("true");
                break;
            case FALSE:
                writeJSONText("false");
                break;
            case NULL:
                writeJSONText("null");
                break;
            default:
                throw new AssertionError(value.getValueType().name());
        }
    }

    /**
     * Writes out a JSON array.
     * @param array The array to write.
     * @throws IOException on I/O errors.
     */
    public void writeJSONArray(JSONValue[] array) throws IOException {
        startArray();
        for (JSONValue arrayValue : array) {
            startNewEntry();
            writeJSONValue(arrayValue);
        }
        endArray();
    }

    void startArray() throws IOException {
        writer.append(arrayStart);
        pushIndent();
    }

    void endArray() throws IOException {
        popIndent();
        if (indentAmount > 0) {
            writer.append('\n');
            writer.append(currentIndentString);
        }
        writer.append(arrayEnd);
    }

    void startObject() throws IOException {
        writer.append(objectStart);
        pushIndent();
    }

    void endObject() throws IOException {
        popIndent();
        if (indentAmount > 0) {
            writer.append('\n');
            writer.append(currentIndentString);
        }
        writer.append(objectEnd);
    }

    void startNewEntry() throws IOException {
        if (activeScopeInfo.itemCount > 0) {
            writer.append(entrySeparator);
        }
        if (indentAmount > 0) {
            writer.append(currentIndentString);
        }
        ++activeScopeInfo.itemCount;
    }

    void writeName(String name) throws IOException {
        writer.append(JSONLite.encodeToJSONString(name));
        writer.append(nameValueSeparator);
    }

    void writeJSONText(String value) throws IOException {
        writer.append(value);
    }

    final void pushIndent() {
        activeScopeInfo = new ScopeInfo();
        scopeInfoStack.add(activeScopeInfo);
        if (indentAmount > 0) {
            currentIndentString += spacesPerIndent;
        }
    }

    final void popIndent() {
        scopeInfoStack.remove(scopeInfoStack.size() - 1);
        activeScopeInfo = scopeInfoStack.get(scopeInfoStack.size() - 1);
        if (indentAmount > 0) {
            currentIndentString = currentIndentString.substring(0, currentIndentString.length() - indentAmount);
        }
    }
    
}
