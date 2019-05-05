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
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), null, BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), null, BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null, BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null, BigDecimal.ZERO),
        };
        SecurityLots securityLotsA = new SecurityLots(Arrays.asList(refLotsA));
        checkSecurityLots(refLotsA, securityLotsA);

        SecurityLots securityLots;
        
        SecurityLot refLotsB[] = {
            new SecurityLot("1", LocalDate.of(2017,1,2), new BigDecimal(50), new BigDecimal(500), null, BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), null, BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null, BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null, BigDecimal.ZERO),
        };
        securityLots = securityLotsA.removeFIFOShares(LocalDate.of(2017,1,2), new BigDecimal(50));
        checkSecurityLots(refLotsB, securityLots);
        
        SecurityLot refLotsC[] = {
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), null, BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null, BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null, BigDecimal.ZERO),
        };
        securityLots = securityLotsA.removeFIFOShares(LocalDate.of(2017,1,2), new BigDecimal(100));
        checkSecurityLots(refLotsC, securityLots);
                
        SecurityLot refLotsD[] = {
            new SecurityLot("2", LocalDate.of(2018,1,2), new BigDecimal(100), new BigDecimal(500), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null, BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), null, BigDecimal.ZERO),
        };
        securityLots = securityLotsA.removeFIFOShares(LocalDate.of(2018,1,2), new BigDecimal(200));
        checkSecurityLots(refLotsD, securityLots);
        
    }

    @Test
    public void testRemoveLIFOShares() {
        System.out.println("removeLIFOShares");
        
        SecurityLot.nextLotId = 1;
        
        SecurityLot refLotsA[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        SecurityLots securityLotsA = new SecurityLots(Arrays.asList(refLotsA));
        checkSecurityLots(refLotsA, securityLotsA);

        SecurityLots securityLots;
        
        SecurityLot refLotsB[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("1", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        securityLots = securityLotsA.removeLIFOShares(LocalDate.of(2017,1,2), new BigDecimal(100));
        checkSecurityLots(refLotsB, securityLots);
        
        SecurityLot refLotsC[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
        };
        securityLots = securityLotsA.removeLIFOShares(LocalDate.of(2017,1,2), new BigDecimal(300));
        checkSecurityLots(refLotsC, securityLots);
        
        SecurityLot refLotsD[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(1000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("2", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
        };
        securityLots = securityLotsA.removeLIFOShares(LocalDate.of(2017,1,2), new BigDecimal(500));
        checkSecurityLots(refLotsD, securityLots);
    }

    @Test
    public void testRemoveLotShares() {
        System.out.println("removeLotShares");
        
        SecurityLot.nextLotId = 1;
        
        SecurityLot refLotsA[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        SecurityLots securityLotsA = new SecurityLots(Arrays.asList(refLotsA));
        checkSecurityLots(refLotsA, securityLotsA);

        SecurityLots securityLots;

        
        SecurityLots.LotShares sharesB[] = {
            new SecurityLots.LotShares(refLotsA[1], new BigDecimal(50)),
            new SecurityLots.LotShares(refLotsA[2], new BigDecimal(100)),
        };
        SecurityLot refLotsB[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("1", LocalDate.of(2017,1,2), new BigDecimal(150), new BigDecimal(1500), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("2", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        securityLots = securityLotsA.removeLotShares(LocalDate.of(2017,1,2), Arrays.asList(sharesB));
        checkSecurityLots(refLotsB, securityLots);

        
        SecurityLots.LotShares sharesC[] = {
            new SecurityLots.LotShares(refLotsA[1], new BigDecimal(50)),
            new SecurityLots.LotShares(refLotsA[2], new BigDecimal(100)),
            new SecurityLots.LotShares(refLotsA[1], new BigDecimal(150)),
        };
        SecurityLot refLotsC[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("4", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        securityLots = securityLotsA.removeLotShares(LocalDate.of(2017,1,2), Arrays.asList(sharesC));
        checkSecurityLots(refLotsC, securityLots);
    }

    @Test
    public void testScaleShares() {
        System.out.println("scaleLotShares");
        
        SecurityLot.nextLotId = 1;
        
        SecurityLot refLotsA[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        SecurityLots securityLotsA = new SecurityLots(Arrays.asList(refLotsA));
        checkSecurityLots(refLotsA, securityLotsA);

        SecurityLots securityLots;

        SecurityLot refLotsB[] = {
            new SecurityLot("1", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("2", LocalDate.of(2017,1,2), new BigDecimal(600), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("3", LocalDate.of(2017,1,2), new BigDecimal(900), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("4", LocalDate.of(2017,1,2), new BigDecimal(900), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        securityLots = securityLotsA.scaleShares(LocalDate.of(2017,1,2), new BigDecimal(1), new BigDecimal(3));
        checkSecurityLots(refLotsB, securityLots);

        SecurityLot refLotsC[] = {
            new SecurityLot("5", LocalDate.of(2018,1,2), new BigDecimal(150), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("6", LocalDate.of(2018,1,2), new BigDecimal(300), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("7", LocalDate.of(2018,1,2), new BigDecimal(450), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("8", LocalDate.of(2018,1,2), new BigDecimal(450), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        securityLots = securityLotsA.scaleShares(LocalDate.of(2018,1,2), new BigDecimal(2), new BigDecimal(3));
        checkSecurityLots(refLotsC, securityLots);

    }

    @Test
    public void testDistributeCash() {
        System.out.println("distributeCash");
        
        SecurityLot.nextLotId = 1;
        
        SecurityLot refLotsA[] = {
            new SecurityLot("A", LocalDate.of(2017,1,2), new BigDecimal(100), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2017,1,2), new BigDecimal(200), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2017,1,2), new BigDecimal(300), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        SecurityLots securityLotsA = new SecurityLots(Arrays.asList(refLotsA));
        checkSecurityLots(refLotsA, securityLotsA);

        SecurityLots securityLots;

        SecurityLot refLotsB[] = {
            new SecurityLot("4", LocalDate.of(2017,1,2), new BigDecimal(101), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("3", LocalDate.of(2017,1,2), new BigDecimal(202), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("1", LocalDate.of(2017,1,2), new BigDecimal(303), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("2", LocalDate.of(2017,1,2), new BigDecimal(303), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
        };
        securityLots = securityLotsA.distributeCash(LocalDate.of(2017,1,2), new BigDecimal(9));
        checkSecurityLots(refLotsB, securityLots);

        SecurityLot refLotsC[] = {
            new SecurityLot("A", LocalDate.of(2018,1,2), new BigDecimal(394), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2018,1,2), new BigDecimal(300), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2018,1,2), new BigDecimal(6), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2018,1,2), new BigDecimal(200), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
            new SecurityLot("E", LocalDate.of(2018,1,2), new BigDecimal(100), new BigDecimal(2000), LocalDate.of(2017,1,6), BigDecimal.ZERO),
        };
        SecurityLots securityLotsC = new SecurityLots(Arrays.asList(refLotsC));
        checkSecurityLots(refLotsC, securityLotsC);
        
        // 394 -> 0.39
        // 300 -> 0.30
        // 6 -> 0.006
        // 200 -> 0.20
        // 100 -> 0.10
        SecurityLot refLotsD[] = {
            new SecurityLot("5", LocalDate.of(2018,1,2), BigDecimal.valueOf(39439,2), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("6", LocalDate.of(2018,1,2), BigDecimal.valueOf(30030,2), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("9", LocalDate.of(2018,1,2), BigDecimal.valueOf(601,2), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("7", LocalDate.of(2018,1,2), BigDecimal.valueOf(20020,2), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
            new SecurityLot("8", LocalDate.of(2018,1,2), BigDecimal.valueOf(10010,2), new BigDecimal(2000), LocalDate.of(2017,1,6), BigDecimal.ZERO),
        };
        securityLots = securityLotsC.distributeCash(LocalDate.of(2018,1,2), new BigDecimal(1));
        checkSecurityLots(refLotsD, securityLots);

        
        SecurityLot refLotsE[] = {
            new SecurityLot("A", LocalDate.of(2018,1,2), new BigDecimal(396), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("B", LocalDate.of(2018,1,2), new BigDecimal(300), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2018,1,2), new BigDecimal(4), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("D", LocalDate.of(2018,1,2), new BigDecimal(200), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
            new SecurityLot("E", LocalDate.of(2018,1,2), new BigDecimal(100), new BigDecimal(2000), LocalDate.of(2017,1,6), BigDecimal.ZERO),
        };
        SecurityLots securityLotsE = new SecurityLots(Arrays.asList(refLotsE));
        checkSecurityLots(refLotsE, securityLotsE);
        
        SecurityLot refLotsF[] = {
            new SecurityLot("10", LocalDate.of(2018,1,2), BigDecimal.valueOf(39640,2), new BigDecimal(1000), LocalDate.of(2017,1,2), BigDecimal.ZERO),
            new SecurityLot("11", LocalDate.of(2018,1,2), BigDecimal.valueOf(30030,2), new BigDecimal(2000), LocalDate.of(2017,1,3), BigDecimal.ZERO),
            new SecurityLot("C", LocalDate.of(2018,1,2), BigDecimal.valueOf(4,0), new BigDecimal(3000), LocalDate.of(2017,1,4), BigDecimal.ZERO),
            new SecurityLot("12", LocalDate.of(2018,1,2), BigDecimal.valueOf(20020,2), new BigDecimal(3000), LocalDate.of(2017,1,5), BigDecimal.ZERO),
            new SecurityLot("13", LocalDate.of(2018,1,2), BigDecimal.valueOf(10010,2), new BigDecimal(2000), LocalDate.of(2017,1,6), BigDecimal.ZERO),
        };
        securityLots = securityLotsE.distributeCash(LocalDate.of(2018,1,2), new BigDecimal(1));
        checkSecurityLots(refLotsF, securityLots);
        
    }
}
