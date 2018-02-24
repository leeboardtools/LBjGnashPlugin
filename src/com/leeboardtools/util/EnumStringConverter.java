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

import javafx.util.StringConverter;

/**
 * An abstract class for implementing a string converter for {@link Enum}s. For a typical
 * implementation see {@link DateOffset#INTERVAL_STRING_CONVERTER}.
 * @author Albert Santos
 * @param <T>   The enum's type.
 */
public abstract class EnumStringConverter <T extends Enum> extends StringConverter<T> {
    private String [] text;

    private void loadText() {
        if (text == null) {
            T [] enumValues = getEnumValues();
            text = new String [enumValues.length];
            for (int i = 0; i < enumValues.length; ++i) {
                text[i] = ResourceSource.getString(getEnumStringResourceId(enumValues[i]));
            }
        }
    }

    /**
     * Overload to return what is effectively T.values().
     * @return The array of the Enum values.
     */
    protected abstract T [] getEnumValues();
    
    /**
     * Overload to return the string resource id for a given enum, this  is passed
     * to {@link ResourceSource#getString(java.lang.String, java.lang.Object...) } as
     * the key.
     * @param enumValue The enum value of interest.
     * @return The key string.
     */
    protected abstract String getEnumStringResourceId(T enumValue);

    @Override
    public String toString(T object) {
        if (object == null) {
            return null;
        }

        loadText();
        return text[object.ordinal()];
    }

    @Override
    public T fromString(String string) {
        loadText();

        for (int i = 0; i < text.length; ++i) {
            if (text[i].equals(string)) {
                return getEnumValues()[i];
            }
        }
        return null;
    }
}
