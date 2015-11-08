// Copyright 2015 Eivind Vegsundvåg
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ninja.eivind.hotsreplayuploader.models;

import ninja.eivind.testutils.DataObjectTester;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit testing for Account class
 *
 * @author ehatle
 */
public class AccountTest {
    private static final Long PLAYERID = 1234L;
    private static final String PLAYER_NAME = "Eivind";
    // Set of necessary variables for testing
    private Account account;

    @Before
    public void setup() {
        // Initializing variables
        account = new Account();
        account.setPlayerId(PLAYERID);
        account.setName(PLAYER_NAME);
    }

    @Test
    public void testClassIsValidDataObject() throws Exception {
        // Verifying that the Account object is an actual object and contains
        // all neccessary getters and setters
        DataObjectTester<Account> tester = new DataObjectTester<>(Account.class, account);
        tester.run();
    }

    @Test
    public void testToString() {
        // Testing that toString is working properly. Testing against final
        // variables
        assertEquals("Account.toString not providing the correct result", account.toString(),
                ("Account{playerId='" + account.getPlayerId() + '\'' + ", name='" + account.getName() + '\''
                        + ", leaderboardRankings=" + account.getLeaderboardRankings() + '}'));
    }
}
