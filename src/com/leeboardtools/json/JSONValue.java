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

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a JSON value.
 */
public class JSONValue {

    private final ValueType valueType;
    private final String stringValue;
    private final double numberValue;
    private final JSONObject objectValue;
    private final JSONValue[] arrayValue;

    private JSONValue(ValueType type, String stringValue, double numberValue, JSONObject object, JSONValue[] array) {
        switch (type) {
            case STRING:
                this.valueType = (stringValue == null) ? ValueType.NULL : type;
                break;
                
            case OBJECT:
                this.valueType = (object == null) ? ValueType.NULL : type;
                break;
                
            case ARRAY:
                this.valueType = (array == null) ? ValueType.NULL : type;
                break;
                
            case NUMBER:
            case TRUE:
            case FALSE:
            case NULL:
                this.valueType = type;
                break;
                
            default:
                throw new AssertionError(type.name());
            
        }

        this.stringValue = stringValue;
        this.numberValue = numberValue;
        this.objectValue = object;
        this.arrayValue = array;
    }

    /**
     * Constructor for a JSON string value.
     * @param value The value. If <code>null</code> the value's type will be {@link ValueType.NULL}.
     */
    public JSONValue(String value) {
        this(ValueType.STRING, value, Double.NaN, null, null);
    }

    /**
     * Constructor for a JSON number value using a <code>double</code>.
     * @param value The value.
     */
    public JSONValue(double value) {
        this(ValueType.NUMBER, null, value, null, null);
    }

    /**
     * Constructor for a JSON number value using a <code>int</code>.
     * @param value The value.
     */
    public JSONValue(int value) {
        this(ValueType.NUMBER, null, value, null, null);
    }

    /**
     * Constructor for a JSON object value.
     * @param value The value. If <code>null</code> the value's type will be {@link ValueType.NULL}.
     */
    public JSONValue(JSONObject value) {
        this(ValueType.OBJECT, null, Double.NaN, value, null);
    }

    /**
     * Constructor for a JSON array value.
     * @param value The value, must not be <code>null</code>.
     */
    public JSONValue(JSONValue[] value) {
        this(ValueType.ARRAY, null, Double.NaN, null, value);
    }

    /**
     * Constructor for a JSON true or false value.
     * @param value The value. If <code>null</code> the value's type will be {@link ValueType.NULL}.
     */
    public JSONValue(boolean value) {
        this((value) ? ValueType.TRUE : ValueType.FALSE, null, Double.NaN, null, null);
    }

    /**
     * Constructor for a JSON null value.
     */
    public JSONValue() {
        this(ValueType.NULL, null, Double.NaN, null, null);
    }

    /**
     * Constructor that stores an enum value as a JSON string.
     * @param <E>   The enum type.
     * @param value The value to store.
     */
    public <E extends Enum<E>> JSONValue(E value) {
        this((value == null) ? null : value.toString());
    }

    
    
    /**
     * The different types of JSON values.
     */
    public static enum ValueType {
        STRING,
        NUMBER,
        OBJECT,
        ARRAY,
        TRUE,
        FALSE,
        NULL,
    }


    /**
     * @return The value's type.
     */
    public final ValueType getValueType() {
        return valueType;
    }

    /**
     * @return A string value's string, <code>null</code> if the value's type is {@link ValueType#NULL}.
     * @throws InvalidContentException if the value's type is neither {@link ValueType#STRING} nor {@link ValueType#NULL}.
     */
    public final String getStringValue() {
        if (isNull()) {
            return null;
        }
        
        if (valueType != ValueType.STRING) {
            throw new InvalidContentException("getValueType() is not ValueType.STRING!");
        }
        return this.stringValue;
    }

    /**
     * @return A number value's value as a double.
     * @throws InvalidContentException if the value's type is not {@link ValueType#NUMBER}.
     */
    public final double getDoubleValue() {
        if (valueType != ValueType.NUMBER) {
            throw new InvalidContentException("getValueType() is not ValueType.NUMBER!");
        }
        return this.numberValue;
    }

    /**
     * @return A number value's value as an int. The value is cast to an int from a double.
     * @throws InvalidContentException if the value's type is not {@link ValueType#NUMBER}.
     */
    public final int getIntValue() {
        if (valueType != ValueType.NUMBER) {
            throw new InvalidContentException("getValueType() is not ValueType.NUMBER!");
        }
        return (int) this.numberValue;
    }

    /**
     * @return A object value's object, <code>null</code> if the value's type is {@link ValueType#NULL}.
     * @throws InvalidContentException if the value's type is neither {@link ValueType#OBJECT} nor {@link ValueType#NULL}.
     */
    public final JSONObject getObjectValue() {
        if (isNull()) {
            return null;
        }
        
        if (valueType != ValueType.OBJECT) {
            throw new InvalidContentException("getValueType() is not ValueType.OBJECT!");
        }
        return this.objectValue;
    }

    /**
     * @return An array value's array, <code>null</code> if the value's type is {@link ValueType#NULL}.
     * @throws InvalidContentException if the value's type is neither {@link ValueType#ARRAY} nor {@link ValueType#NULL}.
     */
    public final JSONValue[] getArrayValue() {
        if (isNull()) {
            return null;
        }
        
        if (valueType != ValueType.ARRAY) {
            throw new InvalidContentException("getValueType() is not ValueType.ARRAY!");
        }
        return this.arrayValue;
    }

    /**
     * @return <code>true</code> if the value's type is {@link ValueType#TRUE}.
     */
    public final boolean isTrue() {
        return valueType == ValueType.TRUE;
    }

    /**
     * @return <code>true</code> if the value's type is {@link ValueType#FALSE}.
     */
    public final boolean isFalse() {
        return valueType == ValueType.FALSE;
    }

    /**
     * @return <code>true</code> if the value's type is {@link ValueType#NULL}.
     */
    public final boolean isNull() {
        return valueType == ValueType.NULL;
    }
    

    /**
     * Retrieves the enumeration represented by the JSON string value.
     * @param <E>   The enum type.
     * @param enumValues    The array of enumeration values, from the enum's values() method.
     * @return The enum value, <code>null</code> if the value's type is {@link ValueType#NULL}.
     */
    public final <E extends Enum<E>> E getEnumValue(E[] enumValues) {
        if (isNull()) {
            return null;
        }
        
        String value = getStringValue();
        for (E typeValue : enumValues) {
            if (typeValue.toString().equals(value)) {
                return typeValue;
            }
        }
        
        throw new InvalidContentException("JSON value '" + value + "' could not be converted to a " + enumValues[0].getClass().getCanonicalName());
    }
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.valueType);
        hash = 29 * hash + Objects.hashCode(this.stringValue);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.numberValue) ^ (Double.doubleToLongBits(this.numberValue) >>> 32));
        hash = 29 * hash + Objects.hashCode(this.objectValue);
        hash = 29 * hash + Arrays.deepHashCode(this.arrayValue);
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
        final JSONValue other = (JSONValue) obj;
        if (this.valueType != other.valueType) {
            return false;
        }
        switch (this.valueType) {
            case STRING:
                if (!this.stringValue.equals(other.stringValue)) {
                    return false;
                }
                break;
            case NUMBER:
                if (this.numberValue != other.numberValue) {
                    return false;
                }
                break;
            case OBJECT:
                if (!Objects.equals(this.objectValue, other.objectValue)) {
                    return false;
                }
                break;
            case ARRAY:
                if (!Arrays.deepEquals(this.arrayValue, other.arrayValue)) {
                    return false;
                }
                break;
            case TRUE:
                break;
            case FALSE:
                break;
            case NULL:
                break;
            default:
                throw new AssertionError(this.valueType.name());
        }
        return true;
    }
    
}
