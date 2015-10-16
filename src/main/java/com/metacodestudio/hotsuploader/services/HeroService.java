package com.metacodestudio.hotsuploader.services;

import com.metacodestudio.hotsuploader.concurrent.tasks.HeroListTask;
import com.metacodestudio.hotsuploader.models.Hero;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.time.Duration;
import java.util.List;

/**
 * @author Eivind Vegsundvåg
 */
public class HeroService extends ScheduledService<List<Hero>> {

    private final SimpleHttpClient httpClient;

    public HeroService(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    protected Task<List<Hero>> createTask() {
        return new HeroListTask(httpClient);
    }


}
