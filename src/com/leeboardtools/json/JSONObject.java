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

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Abstract base class for JSON objects, which is the entity enclosed in matched
 * '{' and '}' and containing name value pairs.
 */
public abstract class JSONObject implements Iterable<JSONObject.NameValue> {
        
    
    /**
     * A JSONObject name that can be used to identify the class name associated with the object..
     */
    public static final String NAME_CLASS_NAME = "_className";

    
    protected JSONObject() {
    }

    
    
    /**
     * Holds a name and a value together.
     */
    public static class NameValue {
        private final String name;
        private final JSONValue value;
        
        public NameValue(String name, JSONValue value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        public JSONValue getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.name);
            hash = 53 * hash + Objects.hashCode(this.value);
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
            final NameValue other = (NameValue) obj;
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }
    }


    /**
     * Removes all name-value pairs from the object.
     */
    public abstract void clear();

    /**
     * Adds a name-value pair to the object. If the object does not allow duplicate
     * names and an entry with the name already exists, the value of the existing
     * entry is replaced.
     * @param name  The name.
     * @param value     The value.
     */
    public abstract void add(String name, JSONValue value);

    /**
     * @return Retrieves a collection of the names of all the name-value pairs
     * in the object. This collection should NOT be modified.
     */
    public abstract Collection<String> getNames();

    /**
     * Retrieves a value associated with a name, if any. If duplicate names are allowed
     * and there are multiple entries for the name, which value is retrieved is
     * up to the implementation.
     * @param name  The name of interest.
     * @return The value, <code>null</code> if the name is not part of the object.
     */
    public abstract JSONValue getValue(String name);

    @Override
    public abstract Iterator<NameValue> iterator();

    public final void add(String name, String value) {
        JSONObject.this.add(name, new JSONValue(value));
    }

    public final void add(String name, double value) {
        JSONObject.this.add(name, new JSONValue(value));
    }

    public final void add(String name, int value) {
        JSONObject.this.add(name, new JSONValue(value));
    }

    public final void add(String name, JSONObject value) {
        JSONObject.this.add(name, new JSONValue(value));
    }

    public final void add(String name, JSONValue[] value) {
        JSONObject.this.add(name, new JSONValue(value));
    }

    public final void add(String name, boolean value) {
        JSONObject.this.add(name, new JSONValue(value));
    }

    public final void add(String name) {
        JSONObject.this.add(name, new JSONValue());
    }

    public final <T extends Enum<T>> void add(String name, T value) {
        JSONObject.this.add(name, new JSONValue(value));
    }
    
    
    /**
     * Use to add the name of a class to the object so it can be verified when read back.
     * @param <T>   The class type.
     * @param value The class.
     */
    public final <T> void putClassName(Class<T> value) {
        JSONObject.this.add(NAME_CLASS_NAME, value.getCanonicalName());
    }
    
    /**
     * @return Retrieves the class name previously written by {@link #putClassName(java.lang.Class) }.
     */
    public final String getClassName() {
        return getValue(NAME_CLASS_NAME).getStringValue();
    }
    
    /**
     * Determines if the object contains an entry for a class that was written by
     * {@link #putClassName(java.lang.Class) }
     * @param <T>   The class type.
     * @param classValue    The class.
     * @return <code>true</code> if there's a matching entry, <code>false</code> if not.
     */
    public final <T> boolean hasClass(Class<T> classValue) {
        JSONValue value = getValue(NAME_CLASS_NAME);
        if ((value == null) || (value.getValueType() != JSONValue.ValueType.STRING)) {
            return false;
        }
        return classValue.getCanonicalName().equals(value.getStringValue());
    }
    
    /**
     * Verifies that the object has an entry generated by a call to {@link #putClassName(java.lang.Class) }
     * for a specific class.
     * @param <T>   The class type.
     * @param classValue    The class.
     * @throws InvalidContentException if the object does not have an appropriate entry.
     */
    public final <T> void verifyClass(Class<T> classValue) {
        if (!hasClass(classValue)) {
            throw new InvalidContentException("The JSON object does not have a \"" + NAME_CLASS_NAME + "\" : \"" + classValue.getCanonicalName() + "\" entry.");
        }
    }
    
    
    /**
     * Interface used by {@link #callIfValue(java.lang.String, com.leeboardtools.json.JSONObject.ValueCallback) },
     * can be a lambda expression.
     * @param <T> 
     */
    public interface ValueCallback<T> {
        void call(T jsonValue);
    }
    
    /**
     * Helper that calls a callback with a JSON value if a name is present.
     * This is  useful for handling the case where missing name value pairs should be interpreted
     * as use the default.
     * @param name  The name of interest.
     * @param callback The callback called if there is a name value pair with the name.
     */
    public void callIfValue(String name, ValueCallback<JSONValue> callback) {
        JSONValue jsonValue = getValue(name);
        if ((jsonValue != null) && !jsonValue.isNull()) {
            callback.call(jsonValue);
        }
    }
}
