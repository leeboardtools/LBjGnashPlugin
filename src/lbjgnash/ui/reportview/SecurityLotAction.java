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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Albert Santos
 */
public interface SecurityLotAction {
    public SecurityLots applyAction(SecurityLots securityLots);
    
    
    public static class AddLot implements SecurityLotAction {
        private final SecurityLot newLot;
        
        public AddLot(SecurityLot newLot) {
            this.newLot = newLot;
        }

        @Override
        public SecurityLots applyAction(SecurityLots securityLots) {
            SortedSet<SecurityLot> lots = new TreeSet<>(securityLots.getSecurityLots());
            lots.add(newLot);
            return new SecurityLots(lots);
        }
    }
    
    public static class SellFIFOShares implements SecurityLotAction {
        private final LocalDate date;
        private final BigDecimal sharesSold;
        
        public SellFIFOShares(LocalDate date, BigDecimal sharesSold) {
            this.date = date;
            this.sharesSold = sharesSold;
        }

        @Override
        public SecurityLots applyAction(SecurityLots securityLots) {
            return securityLots.removeFIFOShares(date, sharesSold);
        }       
    }
    
    public static class SellLIFOShares implements SecurityLotAction {
        private final LocalDate date;
        private final BigDecimal sharesSold;
        
        public SellLIFOShares(LocalDate date, BigDecimal sharesSold) {
            this.date = date;
            this.sharesSold = sharesSold;
        }

        @Override
        public SecurityLots applyAction(SecurityLots securityLots) {
            return securityLots.removeLIFOShares(date, sharesSold);
        }       
    }
    
    
    public static class SellSpecificLots implements SecurityLotAction {
        private final LocalDate date;
        private final List<SecurityLots.LotShares> lotShares = new ArrayList<>();
        
        public SellSpecificLots(LocalDate date, Collection<SecurityLots.LotShares> lotShares) {
            this.date = date;
            this.lotShares.addAll(lotShares);
        }

        @Override
        public SecurityLots applyAction(SecurityLots securityLots) {
            return securityLots.removeLotShares(date, lotShares);
        }
    }
    
        
    public static class ScaleShares implements SecurityLotAction {
        private final LocalDate date;
        private final BigDecimal sharesIn;
        private final BigDecimal sharesOut;
        
        public ScaleShares(LocalDate date, BigDecimal sharesIn, BigDecimal sharesOut) {
            this.date = date;
            this.sharesIn = sharesIn;
            this.sharesOut = sharesOut;
        }

        @Override
        public SecurityLots applyAction(SecurityLots securityLots) {
            return securityLots.scaleShares(date, sharesIn, sharesOut);
        }
    }

    
    public static class DistributeCash implements SecurityLotAction {
        private final LocalDate date;
        private final BigDecimal cashIn;
        
        public DistributeCash(LocalDate date, BigDecimal cashIn) {
            this.date = date;
            this.cashIn = cashIn;
        }

        @Override
        public SecurityLots applyAction(SecurityLots securityLots) {
            return securityLots.distributeCash(date, cashIn);
        }
    }
}
