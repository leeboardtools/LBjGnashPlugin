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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * An immutable collection of {@link SecurityLot}s.
 * @author Albert Santos
 */
public class SecurityLots {
    private final int cashScale = 2;
    private final TreeSet<SecurityLot> securityLots;
    private final SortedSet<SecurityLot> readOnlySecurityLots;
    private BigDecimal totalShares;
    private BigDecimal totalCostBasis;
    private BigDecimal totalCashIn;
    
    
    /**
     * Constructor.
     * @param securityLots The collection of {@link SecurityLot}s, the lots are copied
     * from this.
     */
    public SecurityLots(Collection<SecurityLot> securityLots) {
        this.securityLots = new TreeSet<>();
        this.securityLots.addAll(securityLots);
        this.readOnlySecurityLots = Collections.unmodifiableSortedSet(this.securityLots);
    }
    
    
    /**
     * Constructor.
     * @param securityLot The security lot.
     */
    public SecurityLots(SecurityLot securityLot) {
        this.securityLots = new TreeSet<>();
        if (securityLot != null) {
            this.securityLots.add(securityLot);
        }
        this.readOnlySecurityLots = Collections.unmodifiableSortedSet(this.securityLots);
    }
    
    
    /**
     * Constructor.
     */
    public SecurityLots() {
        this((SecurityLot)null);
    }
    
    
    /**
     * @return An un-modifiable sorted set containing the security lots.
     */
    public final SortedSet<SecurityLot> getSecurityLots() {
        return readOnlySecurityLots;
    }
    
    /**
     * Retrieves the security lot with a given lot id.
     * @param lotId The lot id.
     * @return The security lot, <code>null</code> if none found.
     */
    public final SecurityLot getSecurityLotWithId(String lotId) {
        for (SecurityLot lot : securityLots) {
            if (Objects.equals(lotId, lot.getLotId())) {
                return lot;
            }
        }
        
        return null;
    }
    
    /**
     * @return The total number of shares.
     */
    public final BigDecimal getTotalShares() {
        useTotals();
        return totalShares;
    }
    
    /**
     * @return The total cost basis.
     */
    public final BigDecimal getTotalCostBasis() {
        useTotals();
        return totalCostBasis;
    }
    
    /**
     * @return The total cash-in.
     */
    public final BigDecimal getTotalCashIn() {
        useTotals();
        return totalCashIn;
    }
    
    protected void useTotals() {
        if (totalShares == null) {
            totalShares = BigDecimal.ZERO;
            totalCostBasis = BigDecimal.ZERO;
            totalCashIn = BigDecimal.ZERO;
            
            securityLots.forEach((securityLot) -> {
                totalShares = totalShares.add(securityLot.getShares());
                totalCostBasis = totalCostBasis.add(securityLot.getCostBasis());
                totalCashIn = totalCashIn.add(securityLot.getCashInBasis());
            });
        }
    }
    
    /**
     * Returns the number of shares in lots that are on or after a given date.
     * @param date  The date of interest.
     * @return The number of shares.
     */
    public final BigDecimal getSharesAfterDate(LocalDate date) {
        BigDecimal shares = BigDecimal.ZERO;
        Iterator<SecurityLot> iterator = securityLots.descendingIterator();
        while (iterator.hasNext()) {
            SecurityLot lot = iterator.next();
            if (lot.getCostBasisDate().isBefore(date)) {
                break;
            }
            
            shares = shares.add(lot.getShares());
        }
        return shares;
    }
    
    
    /**
     * Retrieves a new {@link SecurityLots} that has a lot added to it. If the lot
     * does not have a cash-in basis, the value of the lot will be distributed among
     * all the lots that have cash-in basis.
     * @param lot   The lot to add.
     * @return The new security lots.
     */
    public SecurityLots addLot(SecurityLot lot) {
        SortedSet<SecurityLot> lots = new TreeSet<>(getSecurityLots());
        lots.add(lot);
        return new SecurityLots(lots);
    }
    
    
    /**
     * Retrieves a new {@link SecurityLots} that has a given number of shares removed
     * starting from the oldest lots.
     * @param date  The date the action is being applied.
     * @param shares    The number of shares to be removed.
     * @return The new security lots.
     * @throws IllegalArgumentException if shares is greater than the total number of shares in
     * the lots of this security lots.
     */
    public SecurityLots removeFIFOShares(LocalDate date, BigDecimal shares) {
        Iterator<SecurityLot> iterator = securityLots.iterator();
        return removeShares(date, shares, iterator);
    }
        
    /**
     * Retrieves a new {@link SecurityLots} that has a given number of shares removed
     * starting from the newest lots.
     * @param date  The date the action is being applied.
     * @param shares    The number of shares to be removed.
     * @return The new security lots.
     * @throws IllegalArgumentException if shares is greater than the total number of shares in
     * the lots of this security lots.
     */
    public SecurityLots removeLIFOShares(LocalDate date, BigDecimal shares) {
        Iterator<SecurityLot> iterator = securityLots.descendingIterator();
        return removeShares(date, shares, iterator);
    }
    
    
    /**
     * Retrieves a new {@link SecurityLots} that has a given number of shares removed
     * from the lots returned by a {@link Iterator}. If there are more shares than are
     * in all the returned lots then an {@link InvalidArgumentException} is thrown.
     * @param date  The date the action is being applied.
     * @param shares    The number of shares to be removed.
     * @param iterator  The iterator.
     * @return The new security lots.
     * @throws IllegalArgumentException if shares is greater than the total number of shares in
     * the lots returned by iterator.
     */
    public static SecurityLots removeShares(LocalDate date, BigDecimal shares, Iterator<SecurityLot> iterator) {
        List<SecurityLot> newLots = new ArrayList<>();

        while (iterator.hasNext()) {
            SecurityLot lot = iterator.next();
            int compareResult = shares.compareTo(lot.getShares());
            if (compareResult >= 0) {
                shares = shares.subtract(lot.getShares());
                if (compareResult == 0) {
                    break;
                }
            }
            else {
                // A partial result, gotta remove the shares from the lot.
                lot = lot.removeShares(date, shares);
                newLots.add(lot);
                
                shares = BigDecimal.ZERO;
                break;
            }
        }
        
        if (shares.compareTo(BigDecimal.ZERO) != 0) {
            // We've gone negative...
            throw new IllegalArgumentException("More shares were requested than are in the set of lots!");
        }
        
        // Now for the remaining shares...
        while (iterator.hasNext()) {
            newLots.add(iterator.next());
        }
        
        return new SecurityLots(newLots);
    }
    
    
    /**
     * Identifies a number of shares with a security lot.
     */
    public static class LotShares {
        private final SecurityLot lot;
        private final BigDecimal shares;
        
        /**
         * Constructor.
         * @param lot   The lot.
         * @param shares The number of shares.
         */
        public LotShares(SecurityLot lot, BigDecimal shares) {
            this.lot = lot;
            this.shares = shares;
        }
        
        /**
         * @return The lot.
         */
        public final SecurityLot getLot() {
            return lot;
        }
        
        /**
         * @return The number of shares.
         */
        public final BigDecimal getShares() {
            return shares;
        }
    }
    
    /**
     * Retrieves a new {@link SecurityLots} that has shares from specific lots removed from
     * the lots of this.
     * @param date  The date the action is being applied.
     * @param lotSharesCollection   The collection of lot/shares to be removed.
     * @return The new security lots object.
     * @throws IllegalArgumentException if there are more shares requested for removal
     * from a specific lot, or if lotSharesCollection contains lots that are not
     * part of this security lots.
     */
    public SecurityLots removeLotShares(LocalDate date, Collection<LotShares> lotSharesCollection) {
        Iterator<SecurityLot> iterator = securityLots.iterator();
        
        List<SecurityLot> newLots = new ArrayList<>();
        
        // lotShares will typically be only a few entries, so we'll stick to a simple
        // linked list.
        LinkedList<LotShares> remainingLotShares = new LinkedList<>(lotSharesCollection);
        while (iterator.hasNext()) {
            SecurityLot securityLot = iterator.next();
            SecurityLot originalLot = securityLot;
            
            ListIterator<LotShares> sharesIterator = remainingLotShares.listIterator();
            while (sharesIterator.hasNext()) {
                LotShares lotShares = sharesIterator.next();
                if (lotShares.getLot() == originalLot) {
                    securityLot = securityLot.removeShares(date, lotShares.getShares());
                    
                    // It's possible to have multiple entries for the same lot, so we need
                    // to keep searching the list...
                    sharesIterator.remove();
                    
                    if (securityLot == null) {
                        break;
                    }
                }
            }
            
            if (securityLot != null) {
                newLots.add(securityLot);
            }
        }
        
        if (!remainingLotShares.isEmpty()) {
            throw new IllegalArgumentException("One or more lot shares were not removed!");
        }
        
        return new SecurityLots(newLots);
    }
    
    
    /**
     * Applies a ratio to the shares of all the lots. The ratio is defined by a number
     * of shares out per number of shares in.
     * @param date  The date to apply to the new security lots.
     * @param sharesIn  The number of shares in of the ratio.
     * @param sharesOut The number of shares out of the ratio.
     * @return The new security lots, <code>this</code> if sharesIn is equal to sharesOut.
     */
    public SecurityLots scaleShares(LocalDate date, BigDecimal sharesIn, BigDecimal sharesOut) {
        if (sharesIn.equals(sharesOut)) {
            return this;
        }
        
        Iterator<SecurityLot> iterator = securityLots.iterator();

        List<SecurityLot> newLots = new ArrayList<>();
        while (iterator.hasNext()) {
            SecurityLot oldLot = iterator.next();
            newLots.add(oldLot.scaleShares(date, sharesIn, sharesOut));
        }
        
        return new SecurityLots(newLots);
    }
    
    
    /**
     * Distributes cash amongst all the lots.
     * @param date  The date to apply to the new security lots.
     * @param cash  The cash to distribute.
     * @return The new security lots.
     */
    public SecurityLots distributeCash(LocalDate date, BigDecimal cash) {
        BigDecimal cashRemaining = cash;
        if (cash.scale() < cashScale) {
            cash = cash.setScale(cashScale);
        }

        List<SecurityLot> newLots = new ArrayList<>();
        getTotalShares();
        
        if (securityLots.isEmpty() || (totalShares.compareTo(BigDecimal.ZERO) == 0)) {
            // No shares, have to add the shares as a lot.
            SecurityLot newLot = new SecurityLot(SecurityLot.makeLotId(), date, cash, cash, null, cash);
            newLots.add(newLot);
            return new SecurityLots(newLots);
        }
        
        
        // Since we're cash, we don't have to worry about the market price, we can
        // work directly with shares.

        // Since we might have a very small amount of cash to allocate, and we can't
        // allocate anything less than 1 cent, we're going to have to start allocating
        // to the largest lots until we run out of cash to allocate.
        Iterator<SecurityLot> iterator = securityLots.iterator();
        TreeMap<BigDecimal, List<SecurityLot>> securityLotsByShares = new TreeMap<>();
        while (iterator.hasNext()) {
            SecurityLot oldLot = iterator.next();
            BigDecimal shares = oldLot.getShares();
            List<SecurityLot> sharesList = securityLotsByShares.get(shares);
            if (sharesList == null) {
                sharesList = new ArrayList<>();
                securityLotsByShares.put(shares, sharesList);
            }
            
            sharesList.add(oldLot);            
        }
        
        for (BigDecimal shares : securityLotsByShares.descendingKeySet()) {
            List<SecurityLot> lotsList = securityLotsByShares.get(shares);
            BigDecimal toDistribute;
            if (cashRemaining.compareTo(BigDecimal.ZERO) > 0) {
                toDistribute = cash.multiply(shares).divide(totalShares, cashScale, RoundingMode.HALF_UP);
                
                if ((newLots.size() + 1) == securityLots.size()) {
                    toDistribute = cashRemaining;
                }
                else if (toDistribute.compareTo(BigDecimal.ZERO) == 0) {
                    toDistribute = cashRemaining;
                }
                else if (toDistribute.compareTo(cashRemaining) > 0) {
                    toDistribute = cashRemaining;
                }
            }
            else {
                toDistribute = null;
            }
            
            for (SecurityLot lot : lotsList) {
                if (toDistribute != null) {
                    String lotId = SecurityLot.makeLotId();
                    BigDecimal newShares = lot.getShares().add(toDistribute);
                    SecurityLot newLot = new SecurityLot(lotId, date, newShares, lot.getCostBasis(), lot.getCostBasisDate(), lot.getCashInBasis());
                    newLots.add(newLot);
                    
                    cashRemaining = cashRemaining.subtract(toDistribute);
                    if (cashRemaining.compareTo(BigDecimal.ZERO) <= 0) {
                        toDistribute = null;
                    }
                    else if ((newLots.size() + 1) == securityLots.size()) {
                        toDistribute = cashRemaining;
                    }
                    else if (toDistribute.compareTo(cashRemaining) > 0) {
                        toDistribute = cashRemaining;
                    }
                }
                else {
                    newLots.add(lot);
                }
            }
        }
        
        return new SecurityLots(newLots);
    }
}
