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
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.InvestmentTransaction;
import jgnash.engine.MathConstants;
import jgnash.engine.SecurityNode;
import jgnash.engine.Transaction;
import jgnash.engine.TransactionEntry;
import org.hsqldb.lib.StringUtil;

/**
 * This is used to keep track of the shares and market value of a security based
 * on the investment transactions.
 * @author Albert Santos
 */
public class SecurityTransactionTracker {
    private static final Logger LOG = Logger.getLogger(SecurityTransactionTracker.class.getName());
    
    private final SecurityNode securityNode;
    private final TreeSet<DateEntry> dateEntries = new TreeSet<>();
    private final TreeMap<LocalDate, TransactionsForDate> dateTransactionsToProcess = new TreeMap<>();


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
    
    private static class CashInLotEntry {
        private final SecurityLot originalLot;
        private BigDecimal totalShares;
        
        CashInLotEntry(SecurityLot originalLot) {
            this.originalLot = originalLot;
            this.totalShares = originalLot.getShares();
        }
    }
    
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
         * Retrieves an estimate of the value at the beginning of an annual period of
         * the security lots that's based upon the compound annual growth rate (CAGR) 
         * from a given date. For each lot the CAGR is computed, then the value of the
         * lot one year before the given date is computed based upon this value. The
         * return value is the sum of these 'year ago' values.
         * @param date  The date of interest.
         * @param minDays   The minimum number of days before a rate of return is computed for a lot,
         * that is, the lot's cost basis date must be at least this many days from date to be included.
         * @return The sum of the year ago values.
         */
        public final BigDecimal getYearAgoValueSum(LocalDate date, int minDays) {
            BigDecimal currentPrice = getMarketPrice(date);
            return calcYearAgoValueSum(date, minDays, securityLots, currentPrice);
        }


        /**
         * @return The total cash used to make direct purchases (excludes reinvested dividends)
         */
        public final BigDecimal getTotalCashIn() {
            return securityLots.getTotalCashIn();
        }


        /**
         * Similar in functionality to {@link getYearAgoValueSum(LocalDate date, int minDays)} except this uses the
         * cash-in basis of security lots and allocates security lots without cash-in basis to all prior
         * security lots.
         * @param date  The date of interest.
         * @param minDays   The minimum number of days before a rate of return is computed for a lot,
         * that is, the lot's cost basis date must be at least this many days from date to be included.
         * @return The sum of the year ago values.
         */
        public final BigDecimal getCashInYearAgoValueSum(LocalDate date, int minDays) {
            boolean isDebug = false;
            if (isDebug) {
                System.out.println("\nCashInYearAgoValueSum:\t" + getSecurityNode().getSymbol());
            }
            
            // Build a new security lots whose security lots are the security lots with
            // cash-in basis, and whose shares represent the original shares plus any
            // shares from future lots without cash-in basis (i.e. reinvested dividends)
            // proportionally allocated to existing shares at the time.
            List<CashInLotEntry> cashInEntries = new ArrayList<>();
            BigDecimal currentTotalShares = BigDecimal.ZERO;
            for (SecurityLot securityLot : securityLots.getSecurityLots()) {
                BigDecimal cashInBasis = securityLot.getCashInBasis();
                if (cashInBasis.compareTo(BigDecimal.ZERO) <= 0) {                    
                    if (isDebug) {
                        System.out.println("Distributing:\t" + securityLot.getDate() + "\t" + securityLot.getShares() + "\t" + securityLot.getCostBasis());
                    }

                    // Need to allocate the shares to all previous cash-in securities based upon
                    // the proportion of shares to the total shares.
                    // We need to be exact, so the last lot allocated to must be a remainder operation.
                    BigDecimal sharesRemaining = securityLot.getShares();
                    int lotCount = cashInEntries.size();
                    if (lotCount > 0) {
                        int end = lotCount - 1;
                        for (int i = 0; i < end; ++i) {
                            CashInLotEntry cashInEntry = cashInEntries.get(i);
                            BigDecimal sharesToAllocate = sharesRemaining
                                    .multiply(cashInEntry.totalShares)
                                    .divide(currentTotalShares, currentTotalShares.scale(), MathConstants.roundingMode);
                            cashInEntry.totalShares = cashInEntry.totalShares.add(sharesToAllocate);
                            sharesRemaining = sharesRemaining.subtract(sharesToAllocate);
                        }

                        cashInEntries.get(end).totalShares = cashInEntries.get(end).totalShares.add(sharesRemaining);
                    }
                }
                else {
                    // Just add to the list.
                    cashInEntries.add(new CashInLotEntry(securityLot));
                    
                    if (isDebug) {
                        System.out.println("Added:\t" + securityLot.getDate() + "\t" + securityLot.getShares() + "\t" + securityLot.getCostBasis());
                    }
                }
                
                currentTotalShares = currentTotalShares.add(securityLot.getShares());
            }
    
            if (isDebug) {
                System.out.println("New Lots:");
            }
            
            // Create the new security lots.
            List<SecurityLot> newLots = new ArrayList<>();
            for (CashInLotEntry cashInEntry : cashInEntries) {
                SecurityLot oldLot = cashInEntry.originalLot;
                SecurityLot newLot = new SecurityLot(oldLot.getLotId(), oldLot.getDate(), cashInEntry.totalShares,
                    oldLot.getCostBasis(), oldLot.getCostBasisDate(), oldLot.getCashInBasis());
                newLots.add(newLot);
                
                if (isDebug) {
                    System.out.println("New Lot:\t" + newLot.getDate() + "\t" + newLot.getShares() + "\t" + newLot.getCostBasis());
                }
            }
            
            if (isDebug) {
                System.out.println();
            }

            SecurityLots newSecurityLots = new SecurityLots(newLots);            
            BigDecimal currentPrice = getMarketPrice(date);
            return calcYearAgoValueSum(date, minDays, newSecurityLots, currentPrice);
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
    
    
    /**
     * Calculates an estimate of the value at the beginning of an annual period of
     * the security lots that's based upon the compound annual growth rate (CAGR) 
     * from a given date.For each lot the CAGR is computed, then the value of the
     * lot one year before the given date is computed based upon this value.The
     * return value is the sum of these 'year ago' values.
     * @param date  The date of interest.
     * @param minDays   The minimum number of days before a rate of return is computed for a lot,
     * that is, the lot's cost basis date must be at least this many days from date to be included.
     * If the lot is too new, the lot's cost basis is used as-is (i.e. 0% return)
     * @param securityLots  The security lots to use.
     * @param currentPrice  The current price of the security.
     * @return The sum of the year ago values.
     */
    public final BigDecimal calcYearAgoValueSum(LocalDate date, int minDays, SecurityLots securityLots, BigDecimal currentPrice) {
        LocalDate cutoffDate = date.minusDays(minDays);
        if (currentPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal yearAgoValueSum = BigDecimal.ZERO;

        for (SecurityLot securityLot : securityLots.getSecurityLots()) {
            BigDecimal costBasis = securityLot.getCostBasis();
            if (costBasis.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal yearAgoValue;
            if (securityLot.getCostBasisDate().isBefore(cutoffDate)) {
                BigDecimal value = securityLot.getShares().multiply(currentPrice);
                double doubleValue = value.doubleValue();
                
                // (Ending/Begining)^(1/time) - 1
                double time = DateUtil.getYearsUntil(securityLot.getCostBasisDate(), date);
                double rateOfReturn = Math.pow(doubleValue / costBasis.doubleValue(), 1./time) - 1.;

                double doubleYearAgoValue = doubleValue / (1. + rateOfReturn);
                yearAgoValue = BigDecimal.valueOf(doubleYearAgoValue).setScale(costBasis.scale(), MathConstants.roundingMode);
            }
            else {
                yearAgoValue = costBasis;
            }
            
            yearAgoValueSum = yearAgoValueSum.add(yearAgoValue);
        }

        return yearAgoValueSum;
    }
    
    // We want to sort transactions by date,
    // and then for a given date process in the following priority:
    //      - cash in
    //      - sell security
    //      - buy security
    //      - cash out
    static class CashTransactionToRecord {
        final Account cashAccount;
        final Transaction transaction;
        final BigDecimal amount;
        
        CashTransactionToRecord(Account cashAccount, Transaction transaction, BigDecimal amount) {
            this.cashAccount = cashAccount;
            this.transaction = transaction;
            this.amount = amount;
        }
    }
    
    static class TransactionsForDate {
        final List<CashTransactionToRecord> cashInTransactions = new ArrayList();
        final List<InvestmentTransaction> sellTransactions = new ArrayList<>();
        final List<InvestmentTransaction> buyTransactions = new ArrayList<>();
        final List<CashTransactionToRecord> cashOutTransactions = new ArrayList();
    }
    
    TransactionsForDate accessTransactionsForDate(LocalDate date) {
        TransactionsForDate transactions = dateTransactionsToProcess.get(date);
        if (transactions == null) {
            transactions = new TransactionsForDate();
            dateTransactionsToProcess.put(date, transactions);
        }
        return transactions;
    }
    
    public final void recordTransaction(InvestmentTransaction transaction) {
        LocalDate date = transaction.getLocalDate();
        
        switch (transaction.getTransactionType()){
            case ADDSHARE:
                accessTransactionsForDate(date).buyTransactions.add(transaction);
                break;
                
            case BUYSHARE:
                accessTransactionsForDate(date).buyTransactions.add(transaction);
                break;
                
            case DIVIDEND:
                break;
                
            case REINVESTDIV:
                accessTransactionsForDate(date).buyTransactions.add(transaction);
                break;
                
            case REMOVESHARE:
                break;
                
            case RETURNOFCAPITAL:
                break;
                
            case SELLSHARE:
                accessTransactionsForDate(date).sellTransactions.add(transaction);
                break;
                
            case SPLITSHARE:
                accessTransactionsForDate(date).sellTransactions.add(transaction);
                break;
                
            case MERGESHARE:
                accessTransactionsForDate(date).sellTransactions.add(transaction);
                break;
                
            default:
                throw new AssertionError(transaction.getTransactionType().name());
            
        }
        
    }
    
    public final void recordCashTransaction(Account cashAccount, Transaction transaction, BigDecimal amount) {
        LocalDate date = transaction.getLocalDate();
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            // Cash out
            accessTransactionsForDate(date).cashOutTransactions.add(new CashTransactionToRecord(cashAccount, transaction, amount));
        }
        else {
            accessTransactionsForDate(date).cashInTransactions.add(new CashTransactionToRecord(cashAccount, transaction, amount));
        }
    }
    
    public final void finalizeTransactions() {
        dateTransactionsToProcess.forEach((date, transactions) -> {
            transactions.cashInTransactions.forEach((entry) -> {
                recordCashTransactionImpl(entry.cashAccount, entry.transaction, entry.amount);
            });
            
            transactions.sellTransactions.forEach((entry) -> {
                recordTransactionImpl(entry);
            });
            
            transactions.buyTransactions.forEach((entry) -> {
                recordTransactionImpl(entry);
            });

            transactions.cashOutTransactions.forEach((entry) -> {
                recordCashTransactionImpl(entry.cashAccount, entry.transaction, entry.amount);
            });
        });
        
        dateTransactionsToProcess.clear();
    }
    
    public boolean isCashInString(String text) {
        text = text.toLowerCase();
        return text.contains("[cash-in]");
    }
    
    public final void recordCashTransactionImpl(Account cashAccount, Transaction transaction, BigDecimal amount) {
        LocalDate date = transaction.getLocalDate();

        String lotId = SecurityLot.makeLotId();
        SecurityLotAction action;

        SecurityLots previousLots;
        List<SecurityLotAction> otherActions;

        DateEntry previousDateEntry = dateEntries.floor(new DateEntry(date));
        if (previousDateEntry == null) {
            previousLots = new SecurityLots();
            otherActions = null;
        }
        else {
            previousLots = previousDateEntry.getSecurityLots();
            otherActions = (previousDateEntry.getDate().equals(date)) ? previousDateEntry.getSecurityLotActions() : null;
        }
        

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            // Is it income, or cash inflow?
            // Cash inflow has cost basis, income does not.
            boolean isIncome = !isCashInString(transaction.getMemo());
            
            if (isIncome) {
                isIncome = false;
                for (TransactionEntry entry : transaction.getTransactionEntries()) {
                    if (entry.getCreditAccount() != cashAccount) {
                        continue;
                    }

                    Account debitAccount = entry.getDebitAccount();
                    if ((debitAccount.getAccountType().getAccountGroup() == AccountGroup.INCOME)
                      && !isCashInString(debitAccount.getDescription())) {
                        isIncome = true;
                        break;
                    }
                }
            }
            
            if (isIncome) {
                action = new SecurityLotAction.DistributeCash(date, amount);
            }
            else {
                SecurityLot newLot = new SecurityLot(lotId, date, amount, amount, null, amount);
                action = new SecurityLotAction.AddLot(newLot);
            }
        }
        else {
            BigDecimal sharesToRemove = amount.negate();
            BigDecimal currentShares = previousLots.getTotalShares();
            if (sharesToRemove.compareTo(currentShares) <= 0) {
                action = new SecurityLotAction.SellWithinDateThenFIFOShares(date, amount.negate(), 90);
                //action = new SecurityLotAction.SellFIFOShares(date, amount.negate());
            }
            else {
                // Going negative, probably the result of a sell before a buy, but for now
                // we'll just go negative shares.
                BigDecimal negativeShares = currentShares.subtract(sharesToRemove);
                previousLots = new SecurityLots();
                SecurityLot newLot = new SecurityLot(lotId, date, negativeShares, negativeShares, null, BigDecimal.ZERO);
                action = new SecurityLotAction.AddLot(newLot);
            }
        }

        SecurityLots newLots = action.applyAction(previousLots);
        BigDecimal marketPrice = BigDecimal.ONE;

        
        // TEST!!!
        boolean isDebug = false;
        //isDebug = true;
        if (isDebug) {
            System.out.println("\n" + date + "\t" + transaction.getTransactionMemo() + "\t" + amount);
            System.out.println("Old Lots:\t" + "\t" + previousLots.getTotalShares() + "\t" + previousLots.getTotalCostBasis() + "\t" + previousLots.getTotalCashIn());
            for (SecurityLot lot : previousLots.getSecurityLots()) {
                System.out.println("\t" + lot.getCostBasisDate() + "\t" + lot.getShares() + "\t" + lot.getCostBasis() + "\t" + lot.getCashInBasis());
            }
            System.out.println("New Lots:\t" + "\t" + newLots.getTotalShares() + "\t" + newLots.getTotalCostBasis() + "\t" + newLots.getTotalCashIn());
            for (SecurityLot lot : newLots.getSecurityLots()) {
                System.out.println("\t" + lot.getCostBasisDate() + "\t" + lot.getShares() + "\t" + lot.getCostBasis() + "\t" + lot.getCashInBasis());
            }
            System.out.println();
        }
        
        
        DateEntry dateEntry = new DateEntry(date, marketPrice, action, otherActions, newLots);
        if (otherActions != null) {
            dateEntries.remove(previousDateEntry);
        }
        dateEntries.add(dateEntry);
    }
    
    protected void dumpTransaction(String title, LocalDate date, BigDecimal quantity, BigDecimal cashValue) {
        System.out.println(title + "\t" + date + "\t" + quantity + "\t" + cashValue);
    }
    
    public final void recordTransactionImpl(InvestmentTransaction transaction) {
        LocalDate date = transaction.getLocalDate();
        BigDecimal quantity = transaction.getQuantity();
        BigDecimal cashValue = getTransactionCashValue(transaction);
        Collection<SecurityLots.LotShares> lotShares = null;

        //dumpTransaction(transaction.getTransactionType().toString(), date, quantity, cashValue);
        
        SecurityLotAction action = null;
        SecurityLot newLot;
        switch (transaction.getTransactionType()){
            case ADDSHARE:
                newLot = newLotForTransaction(transaction, true);
                action = new SecurityLotAction.AddLot(newLot);
                break;
                
            case BUYSHARE:
                String memo = transaction.getMemo().toLowerCase();
                boolean isCashIn = !memo.contains("reinvested") && (cashValue.compareTo(BigDecimal.ZERO) > 0);
                newLot = newLotForTransaction(transaction, isCashIn);
                action = new SecurityLotAction.AddLot(newLot);
                break;
                
            case DIVIDEND:
                break;
                
            case REINVESTDIV:
                newLot = newLotForTransaction(transaction, false);
                action = new SecurityLotAction.AddLot(newLot);
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
                action = createScaleSharesAction(transaction, quantity);
                break;
                
            case MERGESHARE:
                action = createScaleSharesAction(transaction, quantity.negate());
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
    
    
    protected SecurityLotAction.ScaleShares createScaleSharesAction(InvestmentTransaction transaction, BigDecimal sharesAdded) {
        if (sharesAdded.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        LocalDate date = transaction.getLocalDate();
        DateEntry previousDateEntry = dateEntries.floor(new DateEntry(date));
        if (previousDateEntry == null) {
            return null;
        }
        
        SecurityLots previousLots = previousDateEntry.getSecurityLots();
        BigDecimal oldShares = previousLots.getTotalShares();
        BigDecimal newShares = oldShares.add(sharesAdded);        
        return new SecurityLotAction.ScaleShares(date, oldShares, newShares);
    }

    
    protected SecurityLot newLotForTransaction(InvestmentTransaction transaction, boolean isCashIn) {
        String lotId = null;
        Collection<String> lotNames = lotNamesFromString(transaction.getMemo());
        if ((lotNames != null) && !lotNames.isEmpty()) {
            Iterator<String> iterator = lotNames.iterator();
            if (iterator.hasNext()) {
                lotId = iterator.next();
            }
        }
        if (StringUtil.isEmpty(lotId)) {
            lotId = SecurityLot.makeLotId();
        }
        
        LocalDate date = transaction.getLocalDate();
        BigDecimal shares = transaction.getQuantity();
        BigDecimal costBasis = getTransactionCashValue(transaction);
        BigDecimal cashInBasis = (isCashIn) ? costBasis : BigDecimal.ZERO;
        return new SecurityLot(lotId, date, shares, costBasis, null, cashInBasis);
    }
    
    protected BigDecimal getTransactionCashValue(InvestmentTransaction transaction) {
        BigDecimal cashValue = transaction.getNetCashValue().setScale(securityNode.getScale(), MathConstants.roundingMode);
        return cashValue;
    }

    
    protected Collection<SecurityLots.LotShares> getLotSharesFromTransaction(InvestmentTransaction transaction) {
        String memo = transaction.getMemo();
        Collection<String> lotNames = lotNamesFromString(memo);
        if ((lotNames == null) || (lotNames.isEmpty())) {
            return null;
        }
        
        DateEntry dateEntry = getDateEntry(transaction.getLocalDate());
        if (dateEntry == null) {
            return null;
        }
        SecurityLots securityLots = dateEntry.getSecurityLots();
        
        BigDecimal totalShares = BigDecimal.ZERO;
        Collection<SecurityLots.LotShares> lotShares = new ArrayList<>();
        for (String lotName : lotNames) {
            SecurityLot lot = securityLots.getSecurityLotWithId(lotName);
            if (lot == null) {
                // All or nothing...
                LOG.warning("Lot not found, ignoring all lots:\t" + transaction.getLocalDate() + "\t" + lotName);
                return null;
            }
            
            SecurityLots.LotShares lotSharesToAdd = new SecurityLots.LotShares(lot, lot.getShares());
            lotShares.add(lotSharesToAdd);
            totalShares = totalShares.add(lot.getShares());
        }
        
        BigDecimal transactionShares = transaction.getQuantity();
        if (totalShares.compareTo(transactionShares) != 0) {
            LOG.warning("Lot shares do not match transaction shares, ignoring all lots:\t" + transaction.getLocalDate()
                + "\tTransaction Shares:\t" + transactionShares
                + "\tLot Shares:\t" + totalShares);
            return null;
        }
        
        return lotShares;
    }
    
    protected static final String LOT_TAG = "LOT:";
    protected static final String LOT_SEPARATOR_TAG = ";";
    
    protected Collection<String> lotNamesFromString(String text) {
        if (StringUtil.isEmpty(text)) {
            return null;
        }
        text = text.trim();
        
        int startIndex = text.indexOf(LOT_TAG);
        if (startIndex < 0) {
            return null;
        }
        
        Collection<String> lotNames = new ArrayList<>();
        
        while (true) {
            startIndex += LOT_TAG.length();
            int endIndex = text.indexOf(LOT_TAG, startIndex);

            String lotName;
            if (endIndex < 0) {
                lotName = text.substring(startIndex);
                lotName = cleanupLotName(lotName);
                lotNames.add(lotName);
                break;
            }
            
            lotName = text.substring(startIndex, endIndex);
            lotName = cleanupLotName(lotName);
            lotNames.add(lotName);
            
            startIndex = endIndex;
        }

        return lotNames;
    }
    
    protected String cleanupLotName(String lotName) {
        lotName = lotName.trim();
        if (lotName.endsWith(LOT_SEPARATOR_TAG)) {
            lotName = lotName.substring(0, lotName.length() - LOT_SEPARATOR_TAG.length()).trim();
        }
        return lotName;
    }
}
