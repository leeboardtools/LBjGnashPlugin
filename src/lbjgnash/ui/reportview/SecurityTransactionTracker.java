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
package lbjgnash.ui.reportview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import jgnash.engine.SecurityNode;

/**
 * This is used to keep track of the shares and market value of a security based
 * on the investment transactions.
 * @author Albert Santos
 */
public class SecurityTransactionTracker {
    private final SecurityNode securityNode;
    private final TreeSet<DateEntry> dateEntries = new TreeSet<>();


    // We want to track cost-basis.
    // In order to do that, we need to track lots.
    // Ideally we'd be able to track individual lots being sold, but jGnash doesn't support
    // lots, though we could do something with transaction notes.
    // 
    // For our tracking purposes, though, what we want to do is track the change in lots.
    // For purchases we just add a new lot.
    // For sales, we need the number of shares sold and optionally the lots they were sold from.
    // We start with a master lot list and the first date entry.
    // Each subsequent entry then contains the change in shares, represented as:
    //  - Lot added.
    //  - Lots adjusted/removed.
    //  - Shares removed.
    //
    // Also need to support mergers, splits, and all those other fun things.
    // Here we need to have a scale and sale?
    //
    // Basically the date entry needs all the information to make adjustments to
    // the 'current' lot list.
    //
    // Have SecurityLotAdjustment class
    // Defines what's happening to a list of security lots.
    // Things that can happen:
    //  Add Lot
    //  Remove Lot
    //  Replace Lot
    //  Remove FIFO shares.
    //  Remove LIFO shares.
    //  Split Lots.
    
    
    
    public static class DateEntry {
        private final LocalDate date;
        private final BigDecimal totalShares;
        private final BigDecimal marketPrice;
        private final BigDecimal totalCostBasis;
        private final SortedSet<SecurityLot> lots = new TreeSet<>();
        
        public DateEntry(LocalDate date, BigDecimal marketPrice, SortedSet<SecurityLot> lots) {
            this.date = date;
            
            BigDecimal shares = BigDecimal.ZERO;
            BigDecimal costBasis = BigDecimal.ZERO;
            for (SecurityLot lot : lots) {
                shares = shares.add(lot.getShares());
                costBasis = costBasis.add(lot.getCostBasis());
            }
            
            this.totalShares = shares;
            this.totalCostBasis = costBasis;
            this.marketPrice = marketPrice;
        }
        
        public DateEntry(LocalDate date) {
            this.date = date;
            this.totalShares = null;
            this.marketPrice = null;
            this.totalCostBasis = null;
        }
        
        /**
         * @return The date represented by this entry.
         */
        public final LocalDate getDate() {
            return date;
        }
        
        /**
         * @return The total shares owned as of the date.
         */
        public final BigDecimal getTotalShares() {
            return totalShares;
        }
        
        /**
         * @return The market price as of the date.
         */
        public final BigDecimal getMarketPrice() {
            return marketPrice;
        }
        
        /**
         * @return The cost basis of the total shares.
         */
        public final BigDecimal getCostBasis() {
            return totalCostBasis;
        }
    }
    
    
    public SecurityTransactionTracker(SecurityNode securityNode) {
        this.securityNode = securityNode;
    }
    
    public final SecurityNode getSecurityNode() {
        return securityNode;
    }
    
    public final DateEntry getDateEntry(LocalDate date) {
        DateEntry dateEntry = dateEntries.floor(new DateEntry(date));
        if (dateEntry == null) {
            if (dateEntries.isEmpty()) {
                return null;
            }
            dateEntry = dateEntries.first();
        }
        return dateEntry;
    }
    
    
    
}
