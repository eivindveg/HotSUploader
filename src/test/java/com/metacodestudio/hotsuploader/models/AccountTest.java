package com.metacodestudio.hotsuploader.models;

import com.metacodestudio.testutils.DataObjectTester;
import org.junit.Before;
import org.junit.Test;

public class AccountTest {

    private Account account;

    @Before
    public void setup() {
        account = new Account();
        account.setPlayerId(1234L);
        account.setName("TestUser");
    }

    @Test
    public void testClassIsValidDataObject() throws Exception {
        DataObjectTester<Account> tester = new DataObjectTester<>(Account.class, account);
        tester.run();
    }
}
