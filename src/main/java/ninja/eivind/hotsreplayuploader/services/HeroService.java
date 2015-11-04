package ninja.eivind.hotsreplayuploader.services;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import ninja.eivind.hotsreplayuploader.concurrent.tasks.HeroListTask;
import ninja.eivind.hotsreplayuploader.models.Hero;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;

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
