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

import java.util.ArrayList;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

/**
 * A helper implementation of {@link Observable} that is intended for use with observables
 * that are composed of various objects.
 * <p>
 * This manages the listener list, as well as offering the ability to increment or
 * decrement a disable count of the listener firing, to handle stuff like setting
 * numerous fields at the same time so the listeners aren't notified of each change,
 * just at the end of all the changes.
 * 
 * @author Albert Santos
 */
public class CompositeObservable implements Observable {
    protected List<InvalidationListener> listeners;
    private int disableFireListenerCount = 0;
    private boolean isDeferredFireListener = false;
    
    
    /**
     * Increments the disabling of the firing of the listeners. This must be
     * matched with a call to {@link #decrementDisableFireListeners() }.
     */
    public void incrementDisableFireListeners() {
        ++disableFireListenerCount;
    }
    
    /**
     * Decrements the disabling of the firing of the listeners. If the count
     * gets to 0, {@link #onReenableFireListeners() }.
     */
    public void decrementDisableFireListeners() {
        --disableFireListenerCount;
        if (disableFireListenerCount == 0) {
        }
    }
    
    
    /**
     * Called by {@link #decrementDisableFireListeners() } when the disable count
     * drops down to 0, this will fire the listeners if an attempt had been made
     * to fire the listeners while they were disabled.
     */
    protected void onReenableFireListeners() {
        if (isDeferredFireListener) {
            fireInvalidationListeners();
        }
    }
    
    /**
     * Fires the listeners, calling the {@link InvalidationListener#invalidated(javafx.beans.Observable) }
     * method of each listener.
     */
    protected void fireInvalidationListeners() {
        if (disableFireListenerCount > 0) {
            isDeferredFireListener = true;
        }
        else {
            if (listeners != null) {
                listeners.forEach((listener)-> {
                    listener.invalidated(this);
                });
            }
            isDeferredFireListener = false;
        }
    }

    @Override
    public void addListener(InvalidationListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }
    
    @Override
    public void removeListener(InvalidationListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
}
