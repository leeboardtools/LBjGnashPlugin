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

import java.util.SortedMap;
import java.util.TreeMap;
import jgnash.engine.Account;
import jgnash.engine.InvestmentTransaction;
import jgnash.engine.SecurityNode;

/**
 *
 * @author Albert Santos
 */
public class AccountSecuritiesTracker {
    private final Account account;
    private final SortedMap<SecurityNode, SecurityTransactionTracker> transactionTrackers = new TreeMap<>();
    
    AccountSecuritiesTracker(Account account) {
        this.account = account;
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
            default :
                return null;
        }
        
        AccountSecuritiesTracker tracker = new AccountSecuritiesTracker(account);
        tracker.loadSecurities();
        return tracker;
    }
    
    
    protected void loadSecurities() {
        transactionTrackers.clear();
        account.getSortedTransactionList().forEach((transaction) -> {
            if (transaction instanceof InvestmentTransaction) {
                addInvestmentTransaction((InvestmentTransaction)transaction);
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
}
