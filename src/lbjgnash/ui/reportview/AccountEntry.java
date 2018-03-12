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

import java.util.ArrayList;
import java.util.List;
import jgnash.engine.Account;

/**
 * Encapsulates a single account, with a list of all the child accounts.
 */
public class AccountEntry {

    final Account account;
    final int accountDepth;
    final List<AccountEntry> childAccountEntries = new ArrayList<>();
    final boolean isIncluded;
    final int includeDepth;
    // summaryRowEntry is the main row entry for the account, and the parent account
    // of any child accounts and other row entries.
    RowEntry summaryRowEntry;
    List<RowEntry> postChildAccountRowEntries;
    RowEntry postChildRowEntry;
    AccountSecuritiesTracker accountSecuritiesTracker;

    public AccountEntry(Account account, boolean isIncluded, AccountEntry parentAccountEntry) {
        this.account = account;
        this.accountDepth = account.getDepth();
        this.isIncluded = isIncluded;
        int myIncludeDepth = (parentAccountEntry != null) ? parentAccountEntry.includeDepth : 0;
        if (isIncluded) {
            ++myIncludeDepth;
        }
        this.includeDepth = myIncludeDepth;
    }

    
    public final RowEntry useSummaryRowEntry() {
        if (summaryRowEntry == null) {
            summaryRowEntry = new RowEntry();
            summaryRowEntry.setRowTitle(account.getName());
        }
        return summaryRowEntry;
    }


    public final RowEntry usePostChildRowEntry() {
        if (postChildRowEntry == null) {
            postChildRowEntry = new RowEntry();
            postChildRowEntry.setRowTitle(account.getName());
        }
        return postChildRowEntry;
    }

    public final RowEntry usePostChildAccountRowEntry(int index) {
        if (postChildAccountRowEntries == null) {
            postChildAccountRowEntries = new ArrayList<>();
        }
        while (index >= postChildAccountRowEntries.size()) {
            postChildAccountRowEntries.add(null);
        }
        
        RowEntry rowEntry = postChildAccountRowEntries.get(index);
        if (rowEntry == null) {
            rowEntry = new RowEntry();
            postChildAccountRowEntries.add(index, rowEntry);
        }
        
        return rowEntry;
    }

    public final boolean isAnyChildAccountIncluded() {
        for (AccountEntry accountEntry : childAccountEntries) {
            if (accountEntry.isIncluded) {
                return true;
            }
        }
        for (AccountEntry accountEntry : childAccountEntries) {
            if (accountEntry.isAnyChildAccountIncluded()) {
                return true;
            }
        }
        return false;
    }

    
    public final int getDeepestIncludedChildAccountDepth() {
        int deepestDepth = -1;
        for (AccountEntry accountEntry : childAccountEntries) {
            int depth = accountEntry.getDeepestIncludedChildAccountDepth();
            if (depth > deepestDepth) {
                deepestDepth = depth;
            } else if (accountEntry.isIncluded) {
                depth = accountEntry.accountDepth;
                if (depth > deepestDepth) {
                    deepestDepth = depth;
                }
            }
        }
        return deepestDepth;
    }

    
    public final AccountSecuritiesTracker getAccountSecuritiesTracker() {
        if (accountSecuritiesTracker == null) {
            accountSecuritiesTracker = AccountSecuritiesTracker.createForAccount(account);
        }
        return accountSecuritiesTracker;
    }
    
}
