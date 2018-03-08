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
import java.time.Month;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class SecurityLotTest {
    
    public SecurityLotTest() {
    }

    @Test
    public void testRemoveShares() {
        System.out.println("removeShares");
        
        BigDecimal price = new BigDecimal(12.3456);
        BigDecimal shares = new BigDecimal(100);
        BigDecimal costBasis = shares.multiply(price);
        
        SecurityLot lotA = new SecurityLot("Abc", LocalDate.of(2017,3,21), shares, costBasis, null);
        assertEquals("Abc", lotA.getLotId());
        assertEquals(LocalDate.of(2017,3,21), lotA.getDate());
        assertEquals(shares, lotA.getShares());
        assertEquals(costBasis, lotA.getCostBasis());
        assertEquals(LocalDate.of(2017,3,21), lotA.getCostBasisDate());
        
        BigDecimal sharesToRemove = new BigDecimal(25);
        BigDecimal remainingShares = new BigDecimal(75);
        BigDecimal remainingCostBasis = remainingShares.multiply(price);
        
        SecurityLot lotB = lotA.removeShares(LocalDate.of(2018,2,3), sharesToRemove);
        assertEquals(LocalDate.of(2018,2,3), lotB.getDate());
        assertEquals(remainingShares, lotB.getShares());
        assertEquals(remainingCostBasis, lotB.getCostBasis());
        assertEquals(LocalDate.of(2017,3,21), lotB.getCostBasisDate());
        
        SecurityLot lotC = lotB.removeShares(LocalDate.of(2018,2,3), remainingShares);
        assertNull(lotC);
    }

    @Test
    public void testScaleShares() {
        System.out.println("scaleShares");
                
        BigDecimal price = new BigDecimal(20);
        BigDecimal shares = new BigDecimal(100);
        BigDecimal costBasis = shares.multiply(price);
        
        SecurityLot lotA = new SecurityLot("Abc", LocalDate.of(2017,3,21), shares, costBasis, null);
        
        SecurityLot lotB = lotA.scaleShares(LocalDate.of(2018, 3, 4), new BigDecimal(2), new BigDecimal(3));
        assertEquals(costBasis, lotB.getCostBasis());
        assertEquals(new BigDecimal(150), lotB.getShares());
        assertEquals(LocalDate.of(2017,3,21), lotB.getCostBasisDate());
        assertEquals(LocalDate.of(2018,3,4), lotB.getDate());
        
        lotB = lotA.scaleShares(LocalDate.of(2018, 3, 4), new BigDecimal(5), new BigDecimal(2));
        assertEquals(new BigDecimal(40), lotB.getShares());
    }

    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        String id = "Abc";
        LocalDate date = LocalDate.of(2018,2,3);
        BigDecimal shares = new BigDecimal(100);
        BigDecimal costBasis = new BigDecimal(1000);
        SecurityLot lotA = new SecurityLot(id, date, shares, costBasis, LocalDate.of(2017,3,4));
        SecurityLot lotB = new SecurityLot(id, date, shares, costBasis, LocalDate.of(2017,3,5));
        
        int result = lotA.compareTo(lotB);
        assertTrue(result < 0);

        result = lotB.compareTo(lotA);
        assertTrue(result > 0);
        
        lotB = new SecurityLot(id, date, shares, costBasis, lotA.getCostBasisDate());
        result = lotA.compareTo(lotB);
        assertEquals(0, result);
        
        result = lotB.compareTo(lotA);
        assertEquals(0, result);
    }
    
}
