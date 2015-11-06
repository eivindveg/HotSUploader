package ninja.eivind.hotsreplayuploader;

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
