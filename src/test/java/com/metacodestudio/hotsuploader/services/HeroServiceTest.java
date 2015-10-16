package com.metacodestudio.hotsuploader.services;

import com.metacodestudio.hotsuploader.concurrent.tasks.HeroListTask;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import com.metacodestudio.utils.SimpleHttpClientTestUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import rules.JavaFXThreadingRule;


import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Eivind Vegsundvåg
 */
public class HeroServiceTest {

    @Rule
    public JavaFXThreadingRule javaFXThreadingRule = new JavaFXThreadingRule();

    private final SimpleHttpClient simpleHttpClient = SimpleHttpClientTestUtils.getBaseMock();
    private final ObjectProperty<Exception> throwable = new SimpleObjectProperty<>();
    private HeroService service;
    private CountDownLatch latch;

    @Before
    public void setUp() {
        service = new HeroService(simpleHttpClient);
        latch = new CountDownLatch(1);
    }

    @Test
    public void testBackoffStrategyDoesNotExceedFiveMinutes() throws Exception {
        service.setPeriod(Duration.minutes(6));

        IOException ioException = new IOException("Mocked behaviour");
        when(simpleHttpClient.simpleRequest(HeroListTask.API_ROUTE)).thenThrow(ioException);

        service.start();
        service.setOnScheduled(event -> {
            try {
                System.out.println(service.getCumulativePeriod());
                assertEquals("Service failed as trained by mock.", ioException, service.getException());
                assertFalse("Period is not higher than five minutes.",
                        service.getCumulativePeriod().greaterThan(Duration.minutes(5)));
            } catch (Exception e) {
                throwable.setValue(e);
            }
            latch.countDown();
        });
        latch.await(1, TimeUnit.SECONDS);
        Exception exception = throwable.get();
        if (null != exception) {
            throw exception;
        }

    }

    @After
    public void tearDown() {
        service.cancel();
    }
}
