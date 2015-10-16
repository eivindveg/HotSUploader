package com.metacodestudio.hotsuploader.services;

import com.metacodestudio.hotsuploader.concurrent.tasks.HeroListTask;
import com.metacodestudio.hotsuploader.models.Hero;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

import java.util.List;

/**
 * @author Eivind Vegsundvåg
 */
public class HeroService extends ScheduledService<List<Hero>> {

    private final SimpleHttpClient httpClient;

    public HeroService(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
        setPeriod(Duration.hours(2));
        setBackoffStrategy(param -> param.getPeriod().multiply(1.25));
    }

    @Override
    protected Task<List<Hero>> createTask() {
        HeroListTask heroListTask = new HeroListTask(httpClient);
        heroListTask.setOnSucceeded(event -> {
            getOnSucceeded().handle(event);
        });
        return heroListTask;
    }
}
