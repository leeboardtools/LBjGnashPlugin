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

import com.leeboardtools.time.DateUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import jgnash.engine.InvestmentTransaction;
import jgnash.engine.MathConstants;
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
    
    
    
    public class DateEntry implements Comparable<DateEntry> {
        private final LocalDate date;
        private final SecurityLots securityLots;
        private final BigDecimal transactionPrice;
        private final List<SecurityLotAction> securityLotActions = new ArrayList<>();
        
        /**
         * Constructor.
         * @param date  The date.
         * @param marketPrice   The market price on the date.
         * @param securityLotAction The action that was applied to obtain the security lots.
         * @param otherActions  If not <code>null</code> the other actions that preceded securityLotAction on this date.
         * @param securityLots  The security lots, this is after the action has been applied.
         */
        public DateEntry(LocalDate date, BigDecimal marketPrice, SecurityLotAction securityLotAction, Collection<SecurityLotAction> otherActions,
                SecurityLots securityLots) {
            this.date = date;
            this.transactionPrice = marketPrice;
            this.securityLots = securityLots;           
            if (otherActions != null) {
                this.securityLotActions.addAll(otherActions);
            }
            this.securityLotActions.add(securityLotAction);
        }
        
        public DateEntry(LocalDate date) {
            this.date = date;
            this.transactionPrice = null;
            this.securityLots = null;
        }
        
        /**
         * @return The date represented by this entry.
         */
        public final LocalDate getDate() {
            return date;
        }
        
        /**
         * @return The security node this represents.
         */
        public final SecurityNode getSecurityNode() {
            return securityNode;
        }
        
        /**
         * @return The security lots of this entry.
         */
        public final SecurityLots getSecurityLots() {
            return securityLots;
        }
        
        /**
         * @return The list of actions that were applied to obtain the entry's security lots.
         */
        public final List<SecurityLotAction> getSecurityLotActions() {
            return securityLotActions;
        }
        
        /**
         * @return The market price at the time of the entry.
         */
        public final BigDecimal getTransactionPrice() {
            return transactionPrice;
        }
        
        /**
         * @return The total shares owned as of the date.
         */
        public final BigDecimal getTotalShares() {
            return securityLots.getTotalShares();
        }
        
        /**
         * @return The cost basis of the total shares.
         */
        public final BigDecimal getCostBasis() {
            return securityLots.getTotalCostBasis();
        }
        
        /**
         * Retrieves the market value of the security lots for a given date.
         * @param date The date of interest.
         * @return The market value.
         */
        public final BigDecimal getMarketValue(LocalDate date) {
            BigDecimal currentPrice = getMarketPrice(date);
            BigDecimal value = currentPrice.multiply(securityLots.getTotalShares());
            return value;
        }
        
        /**
         * Retrieves the security price for a given date.
         * @param date  The date of interest.
         * @return The market value.
         */
        public final BigDecimal getMarketPrice(LocalDate date) {
            BigDecimal currentPrice = securityNode.getMarketPrice(date, securityNode.getReportedCurrencyNode());
            return currentPrice;
        }
        
        /**
         * Retrieves a weighted compound annual growth rate (CAGR) from a given date. The value returned
         * is the sum of the annual rate of return of each lot multiplied by the current market
         * price of the lot. To obtain the actual annual rate of return, divide by the market value
         * at the passed in date.
         * @param date  The date of interest.
         * @param minDays   The minimum number of days before a rate of return is computed for a lot,
         * that is, the lot's cost basis date must be at least this many days from date to be included.
         * @return The sum of the weighted annual rate of returns of the lots.
         */
        public final BigDecimal getWeightedAnnualRateOfReturnSum(LocalDate date, int minDays) {
            LocalDate cutoffDate = date.minusDays(minDays);
            BigDecimal currentPrice = getMarketPrice(date);
            BigDecimal weightedRateOfReturnSum = BigDecimal.ZERO;
            
            for (SecurityLot securityLot : securityLots.getSecurityLots()) {
                if (!securityLot.getCostBasisDate().isBefore(cutoffDate)) {
                    continue;
                }
                
                BigDecimal costBasis = securityLot.getCostBasis();
                if (costBasis.compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                
                BigDecimal value = securityLot.getShares().multiply(currentPrice);
                double doubleValue = value.doubleValue();
                
                // (Ending/Begining)^(1/time) - 1
                double time = DateUtil.getYearsUntil(securityLot.getCostBasisDate(), date);
                double rateOfReturn = Math.pow(doubleValue / costBasis.doubleValue(), 1./time) - 1.;
                rateOfReturn *= doubleValue;
                
                weightedRateOfReturnSum = weightedRateOfReturnSum.add(BigDecimal.valueOf(rateOfReturn));
            }
            
            return weightedRateOfReturnSum;
        }

        
        @Override
        public int compareTo(DateEntry o) {
            return date.compareTo(o.date);
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
    
    public final void clearAll() {
        dateEntries.clear();
    }
    
    protected void dumpTransaction(String title, LocalDate date, BigDecimal quantity, BigDecimal cashValue) {
        System.out.println(title + "\t" + date + "\t" + quantity + "\t" + cashValue);
    }
    
    public final void recordTransaction(InvestmentTransaction transaction) {
        LocalDate date = transaction.getLocalDate();
        BigDecimal quantity = transaction.getQuantity();
        BigDecimal cashValue = getTransactionCashValue(transaction);
        Collection<SecurityLots.LotShares> lotShares = null;

        //dumpTransaction(transaction.getTransactionType().toString(), date, quantity, cashValue);
        
        SecurityLotAction action = null;
        SecurityLot newLot;
        switch (transaction.getTransactionType()){
            case ADDSHARE:
                newLot = newLotForTransaction(transaction);
                action = new SecurityLotAction.AddLot(newLot);
                break;
                
            case BUYSHARE:
                newLot = newLotForTransaction(transaction);
                action = new SecurityLotAction.AddLot(newLot);
                break;
                
            case DIVIDEND:
                break;
                
            case REINVESTDIV:
                break;
                
            case REMOVESHARE:
                break;
                
            case RETURNOFCAPITAL:
                break;
                
            case SELLSHARE:
                lotShares = getLotSharesFromTransaction(transaction);
                if (lotShares == null) {
                    action = new SecurityLotAction.SellFIFOShares(date, quantity);
                }
                else {
                    action = new SecurityLotAction.SellSpecificLots(date, lotShares);
                }
                break;
                
            case SPLITSHARE:
                break;
                
            case MERGESHARE:
                break;
                
            default:
                throw new AssertionError(transaction.getTransactionType().name());
            
        }
        
        if (action == null) {
            return;
        }
        
        DateEntry previousDateEntry = dateEntries.floor(new DateEntry(date));
        SecurityLots previousLots;
        List<SecurityLotAction> otherActions;
        if (previousDateEntry == null) {
            if (!(action instanceof SecurityLotAction.AddLot)) {
                throw new IllegalArgumentException("Transactions before the first recorded must be either ADDSHARES or BUYSHARES!");
            }
            previousLots = new SecurityLots();
            otherActions = null;
        }
        else {
            previousLots = previousDateEntry.getSecurityLots();
            otherActions = (previousDateEntry.getDate().equals(date)) ? previousDateEntry.getSecurityLotActions() : null;
        }
        
        SecurityLots newLots = action.applyAction(previousLots);
        BigDecimal marketPrice = transaction.getSecurityNode().getMarketPrice(date, transaction.getInvestmentAccount().getCurrencyNode());
        
        DateEntry dateEntry = new DateEntry(transaction.getLocalDate(), marketPrice, action, otherActions, newLots);
        if (otherActions != null) {
            dateEntries.remove(previousDateEntry);
        }
        dateEntries.add(dateEntry);
    }
    
    protected SecurityLot newLotForTransaction(InvestmentTransaction transaction) {
        String lotId = SecurityLot.makeLotId();
        LocalDate date = transaction.getLocalDate();
        BigDecimal shares = transaction.getQuantity();
        BigDecimal costBasis = getTransactionCashValue(transaction);
        
        return new SecurityLot(lotId, date, shares, costBasis, null);
    }
    
    protected BigDecimal getTransactionCashValue(InvestmentTransaction transaction) {
        BigDecimal cashValue = transaction.getNetCashValue().setScale(securityNode.getScale(), MathConstants.roundingMode);
        return cashValue;
    }
    
    //
    // TODO: Need a lot coding scheme for the InvestmentTransaction memo.
    // For add/buy shares, could be just the date plus the occurance index for multiple occurances in a single date.
    // For remove/sell shares, need to identify the lots:
    // LOT xxx:nn
    // xxx is the lot id,
    // nn is the number of shares.
    // Multiple lines by space separating the entries.
    
    protected Collection<SecurityLots.LotShares> getLotSharesFromTransaction(InvestmentTransaction transaction) {
        return null;
    }
}
