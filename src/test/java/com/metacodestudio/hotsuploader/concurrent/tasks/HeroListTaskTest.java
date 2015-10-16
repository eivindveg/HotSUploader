package com.metacodestudio.hotsuploader.concurrent.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metacodestudio.hotsuploader.models.Hero;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import com.metacodestudio.utils.SimpleHttpClientTestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Eivind Vegsundvåg
 */
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
