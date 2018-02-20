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
package lbjgnash.ui;

import com.leeboardtools.util.CompositeObservable;
import com.leeboardtools.util.SetUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.AccountType;
import jgnash.engine.Engine;

/**
 *
 * @author Albert Santos
 */
public class AccountFilter extends CompositeObservable {
    private final ObservableSet<AccountType> accountTypesToInclude = FXCollections.observableSet(new HashSet<>());
    private final ObservableSet<AccountGroup> accountGroupsToInclude = FXCollections.observableSet(new HashSet<>());
    private final ObservableSet<String> accountNamesToInclude = FXCollections.observableSet(new TreeSet<>());
    private boolean includeHiddenAccounts = true;
    
    private final ObservableSet<AccountType> accountTypesToExclude = FXCollections.observableSet(new HashSet<>());
    private final ObservableSet<AccountGroup> accountGroupsToExclude = FXCollections.observableSet(new HashSet<>());
    private final ObservableSet<String> accountNamesToExclude = FXCollections.observableSet(new TreeSet<>());
    private boolean excludeVisibleAccounts = false;
    
    public final ObservableSet<AccountType> getAccountTypesToInclude() {
        return accountTypesToInclude;
    }
    public final ObservableSet<AccountGroup> getAccountGroupsToInclude() {
        return accountGroupsToInclude;
    }
    public final ObservableSet<String> getAccountNamesToInclude() {
        return accountNamesToInclude;
    }
    public final boolean isIncludeHiddenAccounts() {
        return includeHiddenAccounts;
    }
    public final void setIncludeHiddenAccounts(boolean isInclude) {
        if (isInclude != includeHiddenAccounts) {
            includeHiddenAccounts = isInclude;
            markModified();
        }
    }
    
    public final ObservableSet<AccountType> getAccountTypesToExclude() {
        return accountTypesToExclude;
    }
    public final ObservableSet<AccountGroup> getAccountGroupsToExclude() {
        return accountGroupsToExclude;
    }
    public final ObservableSet<String> getAccountNamesToExclude() {
        return accountNamesToExclude;
    }
    public final boolean isExcludeVisibleAccounts() {
        return excludeVisibleAccounts;
    }
    public final void setExcludeVisibleAccounts(boolean isExclude) {
        if (isExclude != excludeVisibleAccounts) {
            excludeVisibleAccounts = isExclude;
            markModified();
        }
    }
    
    
    
    public AccountFilter() {
        accountTypesToInclude.addListener((SetChangeListener.Change<? extends AccountType> change)-> {
            markModified();
        });
        accountGroupsToInclude.addListener((SetChangeListener.Change<? extends AccountGroup> change)-> {
            markModified();
        });
        accountNamesToInclude.addListener((SetChangeListener.Change<? extends String> change)-> {
            markModified();
        });
        accountTypesToExclude.addListener((SetChangeListener.Change<? extends AccountType> change)-> {
            markModified();
        });
        accountGroupsToExclude.addListener((SetChangeListener.Change<? extends AccountGroup> change)-> {
            markModified();
        });
        accountNamesToExclude.addListener((SetChangeListener.Change<? extends String> change)-> {
            markModified();
        });
    }
    
    public void copyFrom(AccountFilter other) {
        if (this == other) {
            return;
        }
        
        incrementDisableFireListeners();
        try {
            SetUtil.copySet(accountTypesToInclude, other.accountTypesToInclude);
            SetUtil.copySet(accountGroupsToInclude, other.accountGroupsToInclude);
            SetUtil.copySet(accountNamesToInclude, other.accountNamesToInclude);
            setIncludeHiddenAccounts(other.includeHiddenAccounts);

            SetUtil.copySet(accountTypesToExclude, other.accountTypesToExclude);
            SetUtil.copySet(accountGroupsToExclude, other.accountGroupsToExclude);
            SetUtil.copySet(accountNamesToExclude, other.accountNamesToExclude);
            setExcludeVisibleAccounts(other.excludeVisibleAccounts);

        } finally {
            decrementDisableFireListeners();
        }
    }
    
    
    public final Set<Account> filterAccounts(Engine engine) {
        HashSet<Account> accounts = new HashSet<>();
        List<Account> allAccounts = engine.getAccountList();
        allAccounts.forEach((account) -> {
            if (isIncludeAccount(account)) {
                accounts.add(account);
            }
        });
        
        return accounts;
    }
    
    public boolean isIncludeAccount(Account account) {
        String accountName = account.getPathName();
        AccountType accountType = account.getAccountType();
        AccountGroup accountGroup = accountType.getAccountGroup();
        boolean isVisible = account.isVisible();
        
        // Specific accounts always get included/excluded.
        if (accountNamesToInclude.contains(accountName)) {
            return true;
        }
        
        if (accountNamesToExclude.contains(accountName)) {
            return false;
        }
        
        return isIncludeAccountExceptName(account);
    }
    
    boolean isIncludeAccountExceptName(Account account) {
        AccountType accountType = account.getAccountType();
        AccountGroup accountGroup = accountType.getAccountGroup();
        boolean isVisible = account.isVisible();
        
        // Gotta satisfy at least one include criteria...
        boolean accountTypeOK = accountTypesToInclude.isEmpty() || accountTypesToInclude.contains(accountType);
        boolean accountGroupOK = accountGroupsToInclude.isEmpty() || accountGroupsToInclude.contains(accountGroup);
        boolean isVisibleOK = isVisible || includeHiddenAccounts;
        if (!accountTypeOK || !accountGroupOK || !isVisibleOK) {
            return false;
        }
        
        // Can't satisfy a single exclude criteria.
        if (accountTypesToExclude.contains(accountType)
         || accountGroupsToExclude.contains(accountGroup)
         || (isVisible && excludeVisibleAccounts)) {
            return false;
        }
        
        Account parentAccount = account.getParent();
        if ((parentAccount == null) || (parentAccount.getAccountType().getAccountGroup().equals(AccountGroup.ROOT))) {
            return true;
        }
        return isIncludeAccountExceptName(parentAccount);
    }
    
    
    protected void markModified() {
        fireInvalidationListeners();
    }
    
}
