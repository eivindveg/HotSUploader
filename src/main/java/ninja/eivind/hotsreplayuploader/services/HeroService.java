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

package ninja.eivind.hotsreplayuploader.services;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import ninja.eivind.hotsreplayuploader.concurrent.tasks.HeroListTask;
import ninja.eivind.hotsreplayuploader.providers.hotslogs.HotSLogsHero;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * {@link ScheduledService} for checking the current list of available {@link HotSLogsHero}es periodically.
 */
@Singleton
public class HeroService extends ScheduledService<List<HotSLogsHero>> {

    private static final Logger LOG = LoggerFactory.getLogger(HeroService.class);
    @Inject
    private SimpleHttpClient httpClient;

    public HeroService() {
        LOG.info("Instantiating " + getClass().getSimpleName());
        setPeriod(Duration.hours(2));
        setBackoffStrategy(param -> param.getPeriod().multiply(1.25));
        LOG.info("Instantiated " + getClass().getSimpleName());
    }

    @Override
    protected Task<List<HotSLogsHero>> createTask() {
        final HeroListTask heroListTask = new HeroListTask(httpClient);
        heroListTask.setOnSucceeded(event -> {
            getOnSucceeded().handle(event);
            LOG.info("Successfully retrieved Heroes from HotSLogs.com");
        });
        return heroListTask;
    }
}
