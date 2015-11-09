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

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import ninja.eivind.hotsreplayuploader.models.Account;
import ninja.eivind.hotsreplayuploader.utils.SimpleHttpClient;
import ninja.eivind.hotsreplayuploader.utils.StormHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AccountService extends ScheduledService<List<Account>> {

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);
    @Inject
    private StormHandler stormHandler;
    @Inject
    private SimpleHttpClient httpClient;
    @Inject
    private ObjectMapper mapper;

    public AccountService() {
        setDelay(Duration.ZERO);
        setPeriod(Duration.minutes(10));
    }

    @Override
    protected Task<List<Account>> createTask() {
        return new Task<List<Account>>() {
            @Override
            protected List<Account> call() throws Exception {
                List<String> accountUris = stormHandler.getAccountStringUris();
                List<Account> value = new ArrayList<>();
                for (final String accountUri : accountUris) {
                    String response = httpClient.simpleRequest(accountUri);
                    value.add(mapper.readValue(response, Account.class));
                }
                return value;
            }
        };
    }
}
