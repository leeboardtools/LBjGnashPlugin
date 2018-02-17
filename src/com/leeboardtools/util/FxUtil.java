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

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

/**
 * Miscellaneous JavaFX utility methods.
 * @author Albert Santos
 */
public class FxUtil {
    
    /**
     * Searches the descendants of a parent node for a child node with a given id,
     * the search is performed breadth first.
     * @param parent    The parent node to search.
     * @param id    The id to look for, must not be <code>null</code>
     * @return The descendant node with the id, <code>null</code> if none found.
     */
    public static Node getChildWithId(Parent parent, String id) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (id.equals(child.getId())) {
                return child;
            }
        }
        
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Parent) {
                Node nodeWithId = getChildWithId((Parent)child, id);
                if (nodeWithId != null) {
                    return nodeWithId;
                }
            }
        }
        
        return null;
    }
    
    
    /**
     * Changes the size of a font.
     * @param font  The font, if <code>null</code> the default font will be used..
     * @param size  The new size.
     * @return A font with the requested size.
     */
    public static Font changeFontSize(Font font, double size) {
        if (font == null) {
            font = Font.getDefault();
        }
        return (size == font.getSize()) ? font : new Font(font.getName(), size);
    }
    
    /**
     * Changes the posture of a font.
     * @param font  The font, if <code>null</code> the default font will be used..
     * @param posture   The desired posture.
     * @return A font with the desired posture (or at least what's available).
     */
    public static Font changeFontWeight(Font font, FontPosture posture) {
        if (font == null) {
            font = Font.getDefault();
        }
        return Font.font(font.getFamily(), posture, font.getSize());
    }
    
    /**
     * Changes the weight of a font.
     * @param font  The font, if <code>null</code> the default font will be used..
     * @param weight    The desired weight.
     * @return A font with the desired weight (or at least what's available).
     */
    public static Font changeFontWeight(Font font, FontWeight weight) {
        if (font == null) {
            font = Font.getDefault();
        }
        return Font.font(font.getFamily(), weight, font.getSize());
    }
}
