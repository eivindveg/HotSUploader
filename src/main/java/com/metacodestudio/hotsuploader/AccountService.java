package com.metacodestudio.hotsuploader;

import com.metacodestudio.hotsuploader.models.Account;
import com.metacodestudio.hotsuploader.utils.OSUtils;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

import java.util.List;

public class AccountService extends ScheduledService<List<Account>> {
    @Override
    protected Task<List<Account>> createTask() {
        return new Task<List<Account>>() {
            @Override
            protected List<Account> call() throws Exception {
                return OSUtils.getAccounts();
            }
        };
    }
}
