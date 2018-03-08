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

import com.leeboardtools.util.Comparators;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Class for tracking a security lot. Immutable.
 * @author Albert Santos
 */
public class SecurityLot implements Comparable<SecurityLot> {
    // package visibility for testing...
    static long nextLotId = 1;
    private final String lotId;
    private final LocalDate date;
    private final LocalDate costBasisDate;
    private final BigDecimal shares;
    private final BigDecimal costBasis;
    
    /**
     * @return Retrieves a lot id that's unique amongst all the lot ids returned
     * by this.
     */
    public static String makeLotId() {
        return Long.toString(nextLotId++);
    }
    
    /**
     * Constructor.
     * @param lotId The lot id.
     * @param date  The date of the lot purchase.
     * @param shares    The number of shares.
     * @param costBasis The cost-basis.
     * @param costBasisDate The date associated with the cost-basis, used when a lot is adjusted. If
     * <code>null</code> the date will be used.
     */
    public SecurityLot(String lotId, LocalDate date, BigDecimal shares, BigDecimal costBasis, LocalDate costBasisDate) {
        this.lotId = lotId;
        this.date = date;
        this.shares = shares;
        this.costBasis = costBasis;
        this.costBasisDate = (costBasisDate == null) ? date : costBasisDate;
    }

    /**
     * @return Used to uniquely identify a lot, primarily for the rare case when there are multiple
     * identical lots on the same date.
     */
    public final String getLotId() {
        return lotId;
    }
    
    /**
     * @return The date associated with the creation of this particular lot. Note that
     * this date is not compared in {@link #compareTo(lbjgnash.ui.reportview.SecurityLot) } nor
     * {@link #equals(java.lang.Object) }.
     */
    public final LocalDate getDate() {
        return date;
    }
    
    /**
     * @return The number of shares.
     */
    public final BigDecimal getShares() {
        return shares;
    }
    
    /**
     * @return The cost basis.
     */
    public final BigDecimal getCostBasis() {
        return costBasis;
    }
    
    /**
     * @return The date associated with the original purchase.
     */
    public final LocalDate getCostBasisDate() {
        return costBasisDate;
    }
    
    /**
     * Retrieves a new security lot that has a number of shares removed from this lot,
     * with an appropriately adjusted cost basis.
     * @param date  The date to assign to the lot.
     * @param shares    The number of shares to remove.
     * @return The security lot, <code>null</code> if shares is the same as the number
     * of shares in this lot (i.e. there are no more shares in the lot...)
     * @throws IllegalArgumentException if shares is more than the number of shares in the lot.
     */
    public final SecurityLot removeShares(LocalDate date, BigDecimal shares) {
        if (shares.equals(this.shares)) {
            return null;
        }
        
        BigDecimal remainingShares = this.shares.subtract(shares);
        if (remainingShares.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The shares requested is fewer than the shares available.");
        }
        
        BigDecimal remainingCostBasis = this.costBasis.multiply(remainingShares).divide(this.shares, RoundingMode.HALF_UP);
        return new SecurityLot(SecurityLot.makeLotId(), date, 
            remainingShares, remainingCostBasis, this.costBasisDate);
    }
    
    /**
     * Adjusts the shares by a ratio of shares in to shares out.
     * @param date  The date to assign to the lot.
     * @param sharesIn  The denominator of the scaling ratio.
     * @param sharesOut The numerator of the scaling ratio.
     * @return The security lot, which is <code>this</code> if sharesIn is equal to sharesOut.
     */
    public final SecurityLot scaleShares(LocalDate date, BigDecimal sharesIn, BigDecimal sharesOut) {
        if (sharesIn.equals(sharesOut)) {
            return this;
        }
        
        BigDecimal newShares = this.shares.multiply(sharesOut).divide(sharesIn, RoundingMode.HALF_UP);
        return new SecurityLot(SecurityLot.makeLotId(), date, newShares, this.costBasis, this.costBasisDate);
    }
    
    
    @Override
    public int compareTo(SecurityLot o) {
        int result = Comparators.compare(costBasisDate, o.costBasisDate);
        if (result != 0) {
            return result;
        }
        
        result = Comparators.compare(lotId, o.lotId);
        if (result != 0) {
            return result;
        }
        
        result = Comparators.compare(costBasis, o.costBasis);
        if (result != 0) {
            return result;
        }
        
        result = Comparators.compare(shares, o.shares);
        if (result != 0) {
            return result;
        }

        //result = Comparators.compare(date, o.date);
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.lotId);
        hash = 67 * hash + Objects.hashCode(this.date);
        hash = 67 * hash + Objects.hashCode(this.costBasisDate);
        hash = 67 * hash + Objects.hashCode(this.shares);
        hash = 67 * hash + Objects.hashCode(this.costBasis);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SecurityLot other = (SecurityLot) obj;
        return compareTo(other) == 0;
    }
    
    
    
}
