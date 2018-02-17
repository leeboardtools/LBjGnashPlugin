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
package com.leeboardtools.dialog;

import com.leeboardtools.util.ResourceSource;
import javafx.scene.control.TextInputControl;
import javafx.stage.Window;

/**
 * Some utilities for performing control validation.
 * @author Albert Santos
 */
public class Validation {
    public static <T extends TextInputControl> boolean validateEditCount(T control, String messageId, Window ownerWindow) {
        try {
            Integer.parseInt(control.getText());
            return true;
        } catch (RuntimeException ex) {
            String message = ResourceSource.getString(messageId, ex.getLocalizedMessage());
            reportError(message, ownerWindow);
            control.requestFocus();
            return false;
        }
    }
    
    public static <T extends TextInputControl> boolean validateEditCount(T control, String messageId) {
        return validateEditCount(control, messageId, null);
    }
    
    public static void reportError(String message, Window ownerWindow) {
        String title = ResourceSource.getString("LBDialog.Validation.InvalidEntry");
        PromptDialog.showOKDialog(ownerWindow, message, title);
    }
    
    public static void reportError(String message) {
        reportError(message, null);
    }
}
