package ninja.eivind.hotsuploader;

import com.fasterxml.jackson.databind.ObjectMapper;
import ninja.eivind.hotsuploader.models.Account;
import ninja.eivind.hotsuploader.utils.SimpleHttpClient;
import ninja.eivind.hotsuploader.utils.StormHandler;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;

public class AccountService extends ScheduledService<List<Account>> {

    private final StormHandler stormHandler;
    private final SimpleHttpClient httpClient;
    private final ObjectMapper mapper;

    public AccountService(final StormHandler stormHandler, final SimpleHttpClient httpClient) {
        this.stormHandler = stormHandler;
        this.httpClient = httpClient;
        mapper = new ObjectMapper();
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
