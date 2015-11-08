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

package ninja.eivind.hotsreplayuploader.concurrent.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninja.eivind.hotsreplayuploader.models.Hero;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClientTestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class HeroListTaskTest {

    private HeroListTask task;
    private Hero artanis = new Hero("Artanis", "Artanis");
    private Hero abathur = new Hero("Abathur", "Abathur");
    private Hero diablo = new Hero("Diablo", "Diablo");
    private final SimpleHttpClient simpleHttpClient = SimpleHttpClientTestUtils.getBaseMock();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        task = new HeroListTask(simpleHttpClient);
    }

    @Test
    public void testResultIsSorted() throws Exception {
        when(simpleHttpClient.simpleRequest(HeroListTask.API_ROUTE))
                .thenReturn(objectMapper.writeValueAsString(Arrays.asList(artanis, diablo, abathur)));

        List<Hero> expected = Arrays.asList(abathur, artanis, diablo);
        List<Hero> actual = task.call();

        assertEquals("HeroListTask returns sorted list.", expected, actual);
    }
}
