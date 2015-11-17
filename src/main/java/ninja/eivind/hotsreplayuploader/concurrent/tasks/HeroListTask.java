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
import javafx.concurrent.Task;
import ninja.eivind.hotsreplayuploader.providers.hotslogs.HotSLogsHero;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;

import java.util.Arrays;
import java.util.List;

/**
 * Asynchronous {@link Task}, which retrieves a list of
 * all currently available {@link HotSLogsHero}es from the hotslogs API.
 */
public class HeroListTask extends Task<List<HotSLogsHero>> {
    public static final String API_ROUTE = "https://www.hotslogs.com/API/Data/Heroes";
    private final SimpleHttpClient httpClient;

    public HeroListTask(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    protected List<HotSLogsHero> call() throws Exception {
        final String result = httpClient.simpleRequest(API_ROUTE);
        final HotSLogsHero[] heroes = new ObjectMapper().readValue(result, HotSLogsHero[].class);
        List<HotSLogsHero> heroList = Arrays.asList(heroes);
        heroList.sort((o1, o2) -> o1.getPrimaryName().compareTo(o2.getPrimaryName()));
        return heroList;
    }
}
