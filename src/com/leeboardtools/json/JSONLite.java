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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.util.Callback;

/**
 * My own little JSON reader/writer, just because there aren't enough implementations out there...
 * <p>
 * To write an object to JSON format, create an instance of {@link JSONObject} by calling {@link JSONLite#newJSONObject() }
 * and then populating it with name-value pairs representing the object. The values are
 * represented by {@link JSONValue} objects.
 * To actually generate the JSON stream, create a {@link JSONWriter} with the appropriate
 * {@link Writer} to receive the stream, then call {@link JSONWriter#writeJSONObject(com.leeboardtools.json.JSONObject) }.
 * <p>
 * Do the same thing for arrays of JSON values, except in this case create an array of {@link JSONValue}s,
 * and then call {@link JSONWriter#writeJSONArray(com.leeboardtools.json.JSONValue[]) } to write
 * the array of values to the stream.
 * <p>
 * To read a JSON stream, create a {@link JSONReader} with the appropriate input.
 * You then call {@link JSONReader#readJSONObject() } or {@link JSONReader#readJSONArray() }
 * to read an object and an array of values, respectively.
 * @author Albert Santos
 */
public class JSONLite {
    private static Callback<Void, JSONObject> objectCreator = (Void param) -> new JSONHashObject();
    private static ObjectNameHandling objectNameHandling = ObjectNameHandling.NAMES_UNIQUE;
    
    /**
     * Determines how JSON objects handle duplicate names.
     */
    public enum ObjectNameHandling {
        NAMES_UNIQUE,
        DUPLICATES_ALLOWED,
    }
    
    /**
     * @return The current setting determining how duplicate names are handled in JSON objects.
     */
    public static ObjectNameHandling getObjectNameHandling() {
        return objectNameHandling;
    }
    
    /**
     * Sets how duplicate names are handled in JSON objects. This only affects new readings
     * with {@link JSONReader} and creating of JSON objects via {@link #newJSONObject() }.
     * @param handling The handling to install.
     */
    public static void setObjectNameHandling(ObjectNameHandling handling) {
        if (objectNameHandling != handling) {
            Objects.requireNonNull(handling);
            objectNameHandling = handling;
            switch(objectNameHandling) {
                case NAMES_UNIQUE:
                    objectCreator = (Void param) -> new JSONHashObject();
                    break;
                    
                case DUPLICATES_ALLOWED:
                    objectCreator = (Void param) -> new JSONListObject();
                    break;
            }
        }
    }
    
    /**
     * Used to allocate new JSON objects based on the current {@link #getObjectNameHandling() } setting.
     * @return A new {@link JSONObject}.
     */
    public static JSONObject newJSONObject() {
        return objectCreator.call(null);
    }
    
    
    
    /**
     * A {@link HashMap} based implementation of {@link JSONObject}, this does not
     * allow duplicate names.
     */
    public static class JSONHashObject extends JSONObject {
        final Map<String, JSONValue> nameValues = new HashMap<>();

        public JSONHashObject() {}

        @Override
        public void clear() {
            nameValues.clear();
        }
        
        @Override
        public void add(String name, JSONValue value) {
            nameValues.put(name, value);
        }
        
        @Override
        public Collection<String> getNames() {
            return nameValues.keySet();
        }
        
        @Override
        public JSONValue getValue(String name) {
            return nameValues.get(name);
        }
        
        @Override
        public Iterator<NameValue> iterator() {
            return new Iterator<NameValue>() {
                final Iterator<Map.Entry<String, JSONValue>> myIterator = nameValues.entrySet().iterator();
                
                @Override
                public boolean hasNext() {
                    return myIterator.hasNext();
                }

                @Override
                public NameValue next() {
                    Map.Entry<String, JSONValue> entry = myIterator.next();
                    return new NameValue(entry.getKey(), entry.getValue());
                }
                
            };
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + Objects.hashCode(this.nameValues);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JSONHashObject other = (JSONHashObject) obj;
            if (!Objects.equals(this.nameValues, other.nameValues)) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * A {@link ArrayList} based implementation of {@link JSONObject}, this allows
     * duplicate names. It also maintains the order in which name-values are encountered.
     */
    public static class JSONListObject extends JSONObject {
        final List<NameValue> nameValues = new ArrayList<>();

        public JSONListObject() {}

        @Override
        public void clear() {
            nameValues.clear();
        }
        
        @Override
        public void add(String name, JSONValue value) {
            nameValues.add(new NameValue(name, value));
        }
        
        @Override
        public Collection<String> getNames() {
            List<String> names = new ArrayList<>();
            nameValues.forEach((nameValue) -> {
                names.add(nameValue.getName());
            });
            return names;
        }
        
        @Override
        public JSONValue getValue(String name) {
            for (NameValue nameValue : nameValues) {
                if (nameValue.getName().equals(name)) {
                    return nameValue.getValue();
                }
            }
            return null;
        }
        
        @Override
        public Iterator<NameValue> iterator() {
            return nameValues.iterator();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.nameValues);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JSONListObject other = (JSONListObject) obj;
            if (!Objects.equals(this.nameValues, other.nameValues)) {
                return false;
            }
            return true;
        }
    }
    
    
    
    
    
    /**
     * Determines if a Unicode code point is to be treated as white space according to the JSON spec.
     * @param codePoint The code point.
     * @return <code>true</code> if the code point is white space.
     */
    public static boolean isWhiteSpace(int codePoint) {
        switch (codePoint) {
            case 0x0009 :
            case 0x000A :
            case 0x000D :
            case 0x0020 :
                return true;
        }
        return false;
    }
    
    
    public static final String ESC_QUOTATION = "\\\"";
    public static final String ESC_REVERSE_SOLIDUS = "\\\\";
    public static final String ESC_SOLIDUS = "\\/";
    public static final String ESC_BACKSPACE = "\\b";
    public static final String ESC_FORM_FEED = "\\f";
    public static final String ESC_LINE_FEED = "\\n";
    public static final String ESC_CARRIAGE_RETURN = "\\r";
    public static final String ESC_TAB = "\\t";
    public static final String ESC_UNICODE_FOUR_HEX = "\\u";
    
    /**
     * Encodes a normal string into a JSON string.
     * @param plainString   The string to encode.
     * @return The encoded JSON string.
     */
    public static String encodeToJSONString(String plainString) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('"');
        int length = plainString.length();
        for (int i = 0; i < length; ++i) {
            int codePoint = plainString.codePointAt(i);
            switch (codePoint) {
                case 0x0022 :
                    stringBuilder.append(ESC_QUOTATION);
                    break;
                    
                case 0x005C :
                    stringBuilder.append(ESC_REVERSE_SOLIDUS);
                    break;
                    
                case 0x002F :
                    stringBuilder.append(ESC_SOLIDUS);
                    break;
                    
                case 0x0008 :
                    stringBuilder.append(ESC_BACKSPACE);
                    break;
                    
                case 0x000C :
                    stringBuilder.append(ESC_FORM_FEED);
                    break;
                    
                case 0x000A :
                    stringBuilder.append(ESC_LINE_FEED);
                    break;
                    
                case 0x000D :
                    stringBuilder.append(ESC_CARRIAGE_RETURN);
                    break;
                    
                case 0x0009 :
                    stringBuilder.append(ESC_TAB);
                    break;
                    
                default :
                    if ((codePoint >= 0) && (codePoint <= 0x001F)) {
                        stringBuilder.append(ESC_UNICODE_FOUR_HEX);
                        stringBuilder.append(String.format("%04X", codePoint));
                    }
                    else {
                        stringBuilder.appendCodePoint(codePoint);
                    }
                    break;
            }
        }
        stringBuilder.append('"');
        return stringBuilder.toString();
    }
    
    
    
    /**
     * Helper that generates a JSON array from the items in a collection. A converter callback
     * is required to convert the individual items to a JSON value.
     * @param <T>   The collection item type.
     * @param collection    The collection to be converted.
     * @param converter The converter callback.
     * @return The JSON value, <code>null</code> if collection is <code>null</code>.
     */
    public static <T> JSONValue toJSONValue(Collection<T> collection, Callback<T, JSONValue> converter) {
        if (collection == null) {
            return null;
        }
        
        JSONValue [] jsonArray = new JSONValue [collection.size()];
        int index = 0;
        for (T item : collection) {
            jsonArray[index] = converter.call(item);
            ++index;
        }
        
        return new JSONValue(jsonArray);
    }
    
    
    /**
     * Populates a collection from the values in a JSON array. A converter callback is used
     * to convert the individual JSON values into the collection items.
     * @param <T>   The collection item type.
     * @param jsonValue The JSON value to be converted.
     * @param collection    The collection to be populated, it is cleared on entry.
     * @param converter The converter callback.
     */
    public static <T> void fillFromJSONValue(JSONValue jsonValue, Collection<T> collection, Callback<JSONValue, T> converter) {
        collection.clear();
        
        if (jsonValue == null) {
            return;
        }
        
        JSONValue [] jsonArray = jsonValue.getArrayValue();
        if (jsonArray != null) {
            for (JSONValue value : jsonArray) {
                collection.add(converter.call(value));
            }
        }
    }
}
