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
import java.time.LocalDate;
import java.util.SortedMap;
import java.util.TreeMap;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.CurrencyNode;
import jgnash.engine.InvestmentTransaction;
import jgnash.engine.SecurityNode;
import jgnash.engine.Transaction;

/**
 *
 * @author Albert Santos
 */
public class AccountSecuritiesTracker {
    private final Account account;
    private final SortedMap<SecurityNode, SecurityTransactionTracker> transactionTrackers = new TreeMap<>();
    private final SecurityNode cashSecurityNode = new SecurityNode() {
        @Override
        public BigDecimal getMarketPrice(LocalDate date, CurrencyNode node) {
            return BigDecimal.ONE;
        }
    };
    
    AccountSecuritiesTracker(Account account) {
        this.account = account;
        this.cashSecurityNode.setSymbol("Cash");
    }
    
    public final Account getAccount() {
        return account;
    }
    
    public final SortedMap<SecurityNode, SecurityTransactionTracker> getTransactionTrackers() {
        return transactionTrackers;
    }
    
    public static AccountSecuritiesTracker createForAccount(Account account) {
        switch (account.getAccountType().getAccountGroup()) {
            case INVEST:
            case SIMPLEINVEST:
                break;
                
            case ASSET:
                if (isSecuritiesCashAccount(account)) {
                    break;
                }
                return null;
                
            default :
                return null;
        }
        
        AccountSecuritiesTracker tracker = new AccountSecuritiesTracker(account);
        tracker.loadSecurities();
        return tracker;
    }
    
    public static boolean isSecuritiesCashAccount(Account account) {
        if (account.getAccountType().getAccountGroup() != AccountGroup.ASSET) {
            return false;
        }
        
        // A securities cash account either has securities, or contains child accounts
        // that have securities.
        if (!account.getUsedSecurities().isEmpty()) {
            return true;
        }
        
        for (Account childAccount : account.getChildren()) {
            if (!childAccount.getUsedSecurities().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    protected void loadSecurities() {
        transactionTrackers.clear();
        final boolean isCashOnly = account.getSecurities().isEmpty();
        
        account.getSortedTransactionList().forEach((transaction) -> {
            if (!isCashOnly && (transaction instanceof InvestmentTransaction)) {
                addInvestmentTransaction((InvestmentTransaction)transaction);
            }
            else {
                addCashTransaction(transaction);
            }
        });
    }
    
    protected void addInvestmentTransaction(InvestmentTransaction transaction) {
        SecurityNode securityNode = transaction.getSecurityNode();
        SecurityTransactionTracker tracker = transactionTrackers.get(securityNode);
        if (tracker == null) {
            tracker = new SecurityTransactionTracker(securityNode);
            transactionTrackers.put(securityNode, tracker);
        }
        
        tracker.recordTransaction(transaction);
    }
    
    protected void addCashTransaction(Transaction transaction) {
        BigDecimal amount = transaction.getAmount(account);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        
        SecurityTransactionTracker tracker = transactionTrackers.get(cashSecurityNode);
        if (tracker == null) {
            tracker = new SecurityTransactionTracker(cashSecurityNode);
            transactionTrackers.put(cashSecurityNode, tracker);
        }
        
        tracker.recordCashTransaction(account, transaction, amount);
    }
}
