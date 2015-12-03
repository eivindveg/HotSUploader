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

package ninja.eivind.hotsreplayuploader.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StormHandlerTest {

    public static final String NO_MATCH = "Invalid string does not match";
    public static final String MATCH = "Valid string matches";
    private StormHandler stormHandler;

    @Before
    public void setup() {
        stormHandler = new StormHandler();
    }

    @Test
    public void testHotsAccountFilterMatching() throws Exception {
        final String hotsAccountFilter = stormHandler.getHotSAccountFilter();

        String value = "1-Hero-1-";
        assertFalse(NO_MATCH, value.matches(hotsAccountFilter));

        value = "asdf";
        assertFalse(NO_MATCH, value.matches(hotsAccountFilter));

        value = "1-Hero-123456";
        assertFalse(NO_MATCH, value.matches(hotsAccountFilter));

        value = "1-Hero-1-123456";
        assertTrue(MATCH, value.matches(hotsAccountFilter));

        value = "2-Hero-2-1234567890";
        assertTrue(MATCH, value.matches(hotsAccountFilter));
    }
}
