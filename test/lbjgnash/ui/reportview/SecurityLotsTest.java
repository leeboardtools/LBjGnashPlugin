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
import java.util.Arrays;
import java.util.Iterator;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Albert Santos
 */
public class SecurityLotsTest {
    
    public SecurityLotsTest() {
    }
    
    void checkSecurityLots(SecurityLot[] refSecurityLots, SecurityLots securityLots) {
        assertEquals(refSecurityLots.length, securityLots.getSecurityLots().size());
        Iterator<SecurityLot> iterator = securityLots.getSecurityLots().iterator();
        for (int i = 0; i < refSecurityLots.length; ++i) {
            assertEquals(refSecurityLots[i], iterator.next());
        }
    }

    @Test
    public void testRemoveFIFOShares() {
        System.out.println("removeFIFOShares");
        
        SecurityLot.nextLotId = 1;
        
        SecurityLot refLotsA[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), null),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), null),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null),
        };
        SecurityLots securityLotsA = new SecurityLots(Arrays.asList(refLotsA));
        checkSecurityLots(refLotsA, securityLotsA);

        SecurityLots securityLots;
        
        SecurityLot refLotsB[] = {
            new SecurityLot("1", LocalDate.of(2017,1,2), new BigDecimal(50), new BigDecimal(500), null),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), null),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null),
        };
        securityLots = securityLotsA.removeFIFOShares(LocalDate.of(2017,1,2), new BigDecimal(50));
        checkSecurityLots(refLotsB, securityLots);
        
        SecurityLot refLotsC[] = {
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), null),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null),
        };
        securityLots = securityLotsA.removeFIFOShares(LocalDate.of(2017,1,2), new BigDecimal(100));
        checkSecurityLots(refLotsC, securityLots);
                
        SecurityLot refLotsD[] = {
            new SecurityLot("2", LocalDate.of(2018,1,2), new BigDecimal(100), new BigDecimal(500), LocalDate.of(2017,1,2)),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null),
        };
        securityLots = securityLotsA.removeFIFOShares(LocalDate.of(2018,1,2), new BigDecimal(200));
        checkSecurityLots(refLotsD, securityLots);
        
    }

    @Test
    public void testRemoveLIFOShares() {
        System.out.println("removeLIFOShares");
        
        SecurityLot.nextLotId = 1;
        
        SecurityLot refLotsA[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2)),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), LocalDate.of(2017,1,3)),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4)),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5)),
        };
        SecurityLots securityLotsA = new SecurityLots(Arrays.asList(refLotsA));
        checkSecurityLots(refLotsA, securityLotsA);

        SecurityLots securityLots;
        
        SecurityLot refLotsB[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2)),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), LocalDate.of(2017,1,3)),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4)),
            new SecurityLot("1", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,5)),
        };
        securityLots = securityLotsA.removeLIFOShares(LocalDate.of(2017,1,2), new BigDecimal(100));
        checkSecurityLots(refLotsB, securityLots);
        
        SecurityLot refLotsC[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2)),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), LocalDate.of(2017,1,3)),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4)),
        };
        securityLots = securityLotsA.removeLIFOShares(LocalDate.of(2017,1,2), new BigDecimal(300));
        checkSecurityLots(refLotsC, securityLots);
        
        SecurityLot refLotsD[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2)),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), LocalDate.of(2017,1,3)),
            new SecurityLot("2", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,4)),
        };
        securityLots = securityLotsA.removeLIFOShares(LocalDate.of(2017,1,2), new BigDecimal(500));
        checkSecurityLots(refLotsD, securityLots);
    }

    @Test
    public void testRemoveLotShares() {
        System.out.println("removeLotShares");
        
        SecurityLot.nextLotId = 1;
        
        SecurityLot refLotsA[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2)),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,3)),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4)),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5)),
        };
        SecurityLots securityLotsA = new SecurityLots(Arrays.asList(refLotsA));
        checkSecurityLots(refLotsA, securityLotsA);

        SecurityLots securityLots;

        
        SecurityLots.LotShares sharesB[] = {
            new SecurityLots.LotShares(refLotsA[1], new BigDecimal(50)),
            new SecurityLots.LotShares(refLotsA[2], new BigDecimal(100)),
        };
        SecurityLot refLotsB[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2)),
            new SecurityLot("1", LocalDate.of(2017,1,2), new BigDecimal(150), new BigDecimal(1500), LocalDate.of(2017,1,3)),
            new SecurityLot("2", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,4)),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5)),
        };
        securityLots = securityLotsA.removeLotShares(LocalDate.of(2017,1,2), Arrays.asList(sharesB));
        checkSecurityLots(refLotsB, securityLots);

        
        SecurityLots.LotShares sharesC[] = {
            new SecurityLots.LotShares(refLotsA[1], new BigDecimal(50)),
            new SecurityLots.LotShares(refLotsA[2], new BigDecimal(100)),
            new SecurityLots.LotShares(refLotsA[1], new BigDecimal(150)),
        };
        SecurityLot refLotsC[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2)),
            new SecurityLot("4", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,4)),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5)),
        };
        securityLots = securityLotsA.removeLotShares(LocalDate.of(2017,1,2), Arrays.asList(sharesC));
        checkSecurityLots(refLotsC, securityLots);
    }
    
}
