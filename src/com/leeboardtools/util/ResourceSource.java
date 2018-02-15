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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This serves as a base where the application's resource bundle is installed so
 * the everything has a common point for obtaining resources.
 * @author Albert Santos
 */
public class ResourceSource {
    private static ResourceBundle resourceBundle;
    private static final String DEFAULT_RESOURCE_BUNDLE = "resource";
    private static String appName;
    private static String version;
    
    public static synchronized ResourceBundle getBundle() {
        if (resourceBundle == null) {
            try {
                resourceBundle = ResourceBundle.getBundle(DEFAULT_RESOURCE_BUNDLE);
            } catch (final MissingResourceException e) {
                resourceBundle = ResourceBundle.getBundle(DEFAULT_RESOURCE_BUNDLE, Locale.ENGLISH);
            }
        }

        return resourceBundle;
    }
    
    public static void setBundle(ResourceBundle resourceBundle) {
        ResourceSource.resourceBundle = resourceBundle;
    }
    

    /**
     * Gets a localized string with arguments
     *
     * @param key The key for the localized string
     * @param arguments arguments to pass the the message formatter
     * @return The localized string
     */
    public static String getString(final String key, final Object... arguments) {
        try {
            if (arguments.length == 0) {
                return getBundle().getString(key);
            }
            
            String format = getBundle().getString(key);
            return MessageFormat.format(format, arguments);
        } catch (final MissingResourceException mre) {
            //Logger.getLogger(GnuCashConvertUtil.class.getName()).log(Level.WARNING, "Missing resource for: " + key, mre);
            return key;
        }
    }
    
    /**
     * Retrieves the value of the AppName key.
     * @return The app name string.
     */
    public static String getAppName() {
        if (version == null) {
            version = getString("AppName");
        }
        return version;
    }
    
    /**
     * Retrieves the value of the Version key.
     * @return The version string.
     */
    public static String getVersion() {
        if (version == null) {
            version = getString("AppVersion");
        }
        return version;
    }
}
