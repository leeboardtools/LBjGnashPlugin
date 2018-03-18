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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;
import jgnash.engine.Account;
import jgnash.engine.AccountGroup;
import jgnash.engine.AccountType;
import jgnash.engine.Engine;

/**
 * FXML Controller class
 *
 * @author Albert Santos
 */
public class AccountFilterViewController implements Initializable {

    @FXML
    private ListView<CheckEntry<AccountType>> includeAccountTypes;
    @FXML
    private ListView<CheckEntry<AccountGroup>> includeAccountGroups;
    @FXML
    private ListView<CheckEntry<String>> includeAccountNames;
    @FXML
    private CheckBox includeHiddenAccounts;
    @FXML
    private ListView<String> filteredAccounts;
    @FXML
    private Button includeCheckAll;
    @FXML
    private Button includeClearAll;
    @FXML
    private ListView<CheckEntry<AccountType>> excludeAccountTypes;
    @FXML
    private ListView<CheckEntry<AccountGroup>> excludeAccountGroups;
    @FXML
    private ListView<CheckEntry<String>> excludeAccountNames;
    @FXML
    private Button excludeCheckAll;
    @FXML
    private Button excludeClearAll;
    @FXML
    private CheckBox excludeVisibleAccounts;
    
    private List<CheckEntry<String>> masterIncludeCheckItems;
    private List<CheckEntry<String>> masterExcludeCheckItems;
    
    
    private Engine engine;
    private ChangeListener<Boolean> checkChangeListener = (property, oldValue, newValue) -> {
        updateAccountFilter();
    };
    
    private int updateAccountFilterDisableCount = 0;
    
    private AccountFilter accountFilter;
    @FXML
    private Tab includeAccountTypesTab;
    @FXML
    private Tab includeAccountGroupsTab;
    @FXML
    private Tab includeAccountNamesTab;
    @FXML
    private Tab excludeAccountTypesTab;
    @FXML
    private Tab excludeAccountGroupsTab;
    @FXML
    private Tab excludeAccountNamesTab;
    @FXML
    private CheckBox displayHiddenAccountsInclude;
    @FXML
    private CheckBox displayHiddenAccountsExclude;
    
    class CheckEntry<T> {
        final T item;
        final Account account;
        
        CheckEntry(T item, Account account) {
            this.item = item;
            this.name.set(item.toString());
            this.selected.addListener(checkChangeListener);
            this.account = account;
        }
        CheckEntry(T item) {
            this(item, null);
        }
        
        private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();
        public final ReadOnlyStringProperty nameProperty() {
            return name.getReadOnlyProperty();
        }
        public final String getName() {
            return name.get();
        }
        
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        public final BooleanProperty selectedProperty() {
            return selected;
        }
        public final boolean isSelected() {
            return selected.get();
        }
        public final void setSelected(boolean value) {
            selected.set(value);
        }
        
        public final T getItem() {
            return this.item;
        }
        
        @Override
        public final String toString() {
            return name.get();
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        
        ObservableList<CheckEntry<AccountType>> includeAccountTypeItems = FXCollections.observableArrayList();
        ObservableList<CheckEntry<AccountType>> excludeAccountTypeItems = FXCollections.observableArrayList();
        for (AccountType accountType : AccountType.values()) {
            if (!accountType.getAccountGroup().equals(AccountGroup.ROOT)) {
                includeAccountTypeItems.add(new CheckEntry<>(accountType));
                excludeAccountTypeItems.add(new CheckEntry<>(accountType));
            }
        }
        
        ObservableList<CheckEntry<AccountGroup>> includeAccountGroupItems = FXCollections.observableArrayList();
        ObservableList<CheckEntry<AccountGroup>> excludeAccountGroupItems = FXCollections.observableArrayList();
        for (AccountGroup accountGroup : AccountGroup.values()) {
            if (!accountGroup.equals(AccountGroup.ROOT)) {
                includeAccountGroupItems.add(new CheckEntry<>(accountGroup));
                excludeAccountGroupItems.add(new CheckEntry<>(accountGroup));
            }
        }
        
        includeAccountTypes.setCellFactory(CheckBoxListCell.forListView(CheckEntry<AccountType>::selectedProperty));
        includeAccountTypes.setItems(includeAccountTypeItems);
        
        excludeAccountTypes.setCellFactory(CheckBoxListCell.forListView(CheckEntry<AccountType>::selectedProperty));
        excludeAccountTypes.setItems(excludeAccountTypeItems);
        
        includeAccountGroups.setCellFactory(CheckBoxListCell.forListView(CheckEntry<AccountGroup>::selectedProperty));
        includeAccountGroups.setItems(includeAccountGroupItems);
        
        excludeAccountGroups.setCellFactory(CheckBoxListCell.forListView(CheckEntry<AccountGroup>::selectedProperty));
        excludeAccountGroups.setItems(excludeAccountGroupItems);
        
        includeHiddenAccounts.selectedProperty().addListener((property, oldValue, newValue) -> {
            updateAccountFilter();
        });
        excludeVisibleAccounts.selectedProperty().addListener((property, oldValue, newValue) -> {
            updateAccountFilter();
        });
    }
    
    public void setupController(Engine engine, AccountFilter accountFilter) {
        incrementDisableUpdateAccountFilter();
        try {
            if (this.engine != engine) {
                this.engine = engine;

                this.masterIncludeCheckItems = new ArrayList<>();
                this.masterExcludeCheckItems = new ArrayList<>();
                
                if (engine != null) {
                    List<Account> accounts = engine.getAccountList();
                    SortedMap<String, Account> accountNames = new TreeMap<>();

                    accounts.forEach((account) -> {
                        if (!account.getAccountType().getAccountGroup().equals(AccountGroup.ROOT)) {
                            accountNames.put(account.getPathName(), account);
                        }
                    });
                    accountNames.forEach((name, account) -> {
                        CheckEntry<String> includeCheckEntry = new CheckEntry<>(name, account);
                        this.masterIncludeCheckItems.add(includeCheckEntry);
                        
                        CheckEntry<String> excludeCheckEntry = new CheckEntry<>(name, account);
                        this.masterExcludeCheckItems.add(excludeCheckEntry);
                    });
                }

                includeAccountNames.setCellFactory(CheckBoxListCell.forListView(CheckEntry<String>::selectedProperty));
                excludeAccountNames.setCellFactory(CheckBoxListCell.forListView(CheckEntry<String>::selectedProperty));

                updateAccountsList(this.includeAccountNames, this.masterIncludeCheckItems);
                updateAccountsList(this.excludeAccountNames, this.masterExcludeCheckItems);
            }
            
            if (this.accountFilter != accountFilter) {
                this.accountFilter = accountFilter;
                updateFromAccountFilter();
            }
            
        } finally {
            decrementDisableUpdateAccountFilter();
        }
    }

    <T> void checkAllItemsTheSame(ListView<CheckEntry<T>> listView, boolean isCheck) {
        listView.getItems().forEach((entry)-> {
            entry.setSelected(isCheck);
        });
    }

    @FXML
    private void onIncludeCheckAll(ActionEvent event) {
        incrementDisableUpdateAccountFilter();
        try {
            if (includeAccountTypesTab.isSelected()) {
                checkAllItemsTheSame(includeAccountTypes, true);
            }
            else if (includeAccountGroupsTab.isSelected()) {
                checkAllItemsTheSame(includeAccountGroups, true);
            }
            else if (includeAccountNamesTab.isSelected()) {
                checkAllItemsTheSame(includeAccountNames, true);
            }
        } finally {
            decrementDisableUpdateAccountFilter();
        }
    }
    
    @FXML
    private void onIncludeClearAll(ActionEvent event) {
        incrementDisableUpdateAccountFilter();
        try {
            if (includeAccountTypesTab.isSelected()) {
                checkAllItemsTheSame(includeAccountTypes, false);
            }
            else if (includeAccountGroupsTab.isSelected()) {
                checkAllItemsTheSame(includeAccountGroups, false);
            }
            else if (includeAccountNamesTab.isSelected()) {
                checkAllItemsTheSame(includeAccountNames, false);
            }
        } finally {
            decrementDisableUpdateAccountFilter();
        }
    }

    @FXML
    private void onIncludeHiddenAccounts(ActionEvent event) {
        incrementDisableUpdateAccountFilter();
        try {
        } finally {
            decrementDisableUpdateAccountFilter();
        }
    }

    @FXML
    private void onExcludeCheckAll(ActionEvent event) {
        incrementDisableUpdateAccountFilter();
        try {
            if (excludeAccountTypesTab.isSelected()) {
                checkAllItemsTheSame(excludeAccountTypes, true);
            }
            else if (excludeAccountGroupsTab.isSelected()) {
                checkAllItemsTheSame(excludeAccountGroups, true);
            }
            else if (excludeAccountNamesTab.isSelected()) {
                checkAllItemsTheSame(excludeAccountNames, true);
            }
        } finally {
            decrementDisableUpdateAccountFilter();
        }
    }

    @FXML
    private void onExcludeClearAll(ActionEvent event) {
        incrementDisableUpdateAccountFilter();
        try {
            if (excludeAccountTypesTab.isSelected()) {
                checkAllItemsTheSame(excludeAccountTypes, false);
            }
            else if (excludeAccountGroupsTab.isSelected()) {
                checkAllItemsTheSame(excludeAccountGroups, false);
            }
            else if (excludeAccountNamesTab.isSelected()) {
                checkAllItemsTheSame(excludeAccountNames, false);
            }
        } finally {
            decrementDisableUpdateAccountFilter();
        }
    }

    @FXML
    private void onExcludeVisibleAccounts(ActionEvent event) {
        incrementDisableUpdateAccountFilter();
        try {
            
        } finally {
            decrementDisableUpdateAccountFilter();
        }
    }
    

    void incrementDisableUpdateAccountFilter() {
        ++updateAccountFilterDisableCount;
    }
    void decrementDisableUpdateAccountFilter() {
        --updateAccountFilterDisableCount;
        if (updateAccountFilterDisableCount == 0) {
            updateAccountFilter();
        }
    }
    
    void updateAccountFilter() {
        if (updateAccountFilterDisableCount > 0) {
            return;
        }
        
        if (accountFilter == null) {
            return;
        }
        
        updateAccountSet(includeAccountTypes, accountFilter.getAccountTypesToInclude());
        updateAccountSet(includeAccountGroups, accountFilter.getAccountGroupsToInclude());
        updateAccountSet(includeAccountNames, accountFilter.getAccountNamesToInclude());
        accountFilter.setIncludeHiddenAccounts(includeHiddenAccounts.isSelected());

        updateAccountSet(excludeAccountTypes, accountFilter.getAccountTypesToExclude());
        updateAccountSet(excludeAccountGroups, accountFilter.getAccountGroupsToExclude());
        updateAccountSet(excludeAccountNames, accountFilter.getAccountNamesToExclude());
        accountFilter.setExcludeVisibleAccounts(excludeVisibleAccounts.isSelected());
        
        updateFilteredAccounts();
    }
    
    void updateFilteredAccounts() {
        filteredAccounts.getItems().clear();
        if (engine != null) {
            Set<Account> accounts = accountFilter.filterAccounts(engine);
            TreeSet<String> sortedAccounts = new TreeSet<>();
            accounts.forEach((account) -> {
                sortedAccounts.add(account.getPathName());
            });
            
            filteredAccounts.getItems().addAll(sortedAccounts);
        }
    }
    
    <T> void updateAccountSet(ListView<CheckEntry<T>> listView, Set<T> accountSet) {
        accountSet.clear();
        listView.getItems().forEach((entry) -> {
            if (entry.isSelected()) {
                accountSet.add(entry.getItem());
            }
        });
    }
    
    <T> void updateListView(Set<T> accountSet, ListView<CheckEntry<T>> listView) {
        listView.getItems().forEach((entry) -> {
            entry.setSelected(accountSet.contains(entry.getItem()));
        });
    }
    
    void updateFromAccountFilter() {
        ++updateAccountFilterDisableCount;
        try {
            boolean isDisable;
            if (accountFilter == null) {
                isDisable = true;
                
                filteredAccounts.getItems().clear();
            }
            else {
                isDisable = false;
                updateListView(accountFilter.getAccountTypesToInclude(), includeAccountTypes);
                updateListView(accountFilter.getAccountGroupsToInclude(), includeAccountGroups);
                updateListView(accountFilter.getAccountNamesToInclude(), includeAccountNames);
                includeHiddenAccounts.setSelected(accountFilter.isIncludeHiddenAccounts());

                updateListView(accountFilter.getAccountTypesToExclude(), excludeAccountTypes);
                updateListView(accountFilter.getAccountGroupsToExclude(), excludeAccountGroups);
                updateListView(accountFilter.getAccountNamesToExclude(), excludeAccountNames);
                excludeVisibleAccounts.setSelected(accountFilter.isExcludeVisibleAccounts());
                
                updateFilteredAccounts();
            }
            
            includeAccountTypes.setDisable(isDisable);
            includeAccountGroups.setDisable(isDisable);
            includeAccountNames.setDisable(isDisable);
            includeHiddenAccounts.setDisable(isDisable);

            excludeAccountTypes.setDisable(isDisable);
            excludeAccountGroups.setDisable(isDisable);
            excludeAccountNames.setDisable(isDisable);
            excludeVisibleAccounts.setDisable(isDisable);
        } finally {
            // Directly decrementing this instead of calling decrementDisableUpdateAccountFilter()
            // so updateAccountFilter() doesn't get called if it reaches 0.
            --updateAccountFilterDisableCount;
        }
    }

    @FXML
    private void onDisplayHiddenAccountsInclude(ActionEvent event) {
        if (displayHiddenAccountsInclude.isSelected() != displayHiddenAccountsExclude.isSelected()) {
            displayHiddenAccountsExclude.setSelected(displayHiddenAccountsInclude.isSelected());

            updateAccountsList(this.includeAccountNames, this.masterIncludeCheckItems);
            updateAccountsList(this.excludeAccountNames, this.masterExcludeCheckItems);
        }
    }

    @FXML
    private void onDisplayHiddenAccountsExclude(ActionEvent event) {
        if (displayHiddenAccountsInclude.isSelected() != displayHiddenAccountsExclude.isSelected()) {
            displayHiddenAccountsInclude.setSelected(displayHiddenAccountsExclude.isSelected());

            updateAccountsList(this.includeAccountNames, this.masterIncludeCheckItems);
            updateAccountsList(this.excludeAccountNames, this.masterExcludeCheckItems);
        }
    }
    
    private void updateAccountsList(ListView<CheckEntry<String>> listView, List<CheckEntry<String>> items) {
        boolean includeHidden = displayHiddenAccountsInclude.isSelected();
        
        ObservableList<CheckEntry<String>> accountNameItems = FXCollections.observableArrayList();
        items.forEach((item) -> {
            Account account = item.account;
            if ((account != null) && (account.isVisible() || includeHidden)) {
                accountNameItems.add(item);
            }
        });
        
        listView.setItems(accountNameItems);
    }
}
