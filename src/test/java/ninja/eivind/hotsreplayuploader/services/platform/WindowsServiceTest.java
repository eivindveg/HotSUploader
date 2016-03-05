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

package ninja.eivind.hotsreplayuploader.services.platform;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class WindowsServiceTest {

    private WindowsService windowsService;

    @Before
    public void setUp() throws Exception {
        windowsService = new WindowsService();
    }

    @Test
    public void testDocumentsPathCanContainDots() {
        final String expected = "Z:\\Users\\S.omeUser\\Documents\\";
        final Optional<String> matchForPath = windowsService.getMatchForPath(expected);

        assertTrue("Path matches.", matchForPath.isPresent());

        final String actual = matchForPath.get();
        assertEquals("Path match is equal to inserted path.", expected, actual);
    }
}
