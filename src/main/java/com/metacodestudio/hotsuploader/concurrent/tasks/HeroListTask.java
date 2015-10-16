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

package com.metacodestudio.hotsuploader.concurrent.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metacodestudio.hotsuploader.models.Hero;
import com.metacodestudio.hotsuploader.utils.SimpleHttpClient;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Eivind Vegsundvåg
 */
public class HeroListTask extends Task<List<Hero>> {
    private final SimpleHttpClient httpClient;

    public HeroListTask(SimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    protected List<Hero> call() throws Exception {
        final String result = httpClient.simpleRequest("https://www.hotslogs.com/API/Data/Heroes");
        final Hero[] heroes = new ObjectMapper().readValue(result, Hero[].class);
        return Arrays.asList(heroes);
    }
}
