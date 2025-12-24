package iteration1;

import models.Account;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.CustomerAccountsResponse;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {

        CreateUserRequest user1 = AdminSteps.createUser();

        // создаем аккаунт(счет)
        CreateAccountResponse response =  new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);
        String accountNumber = response.getAccountNumber();

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там

        CustomerAccountsResponse customerProfile = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        List<Account> accounts = customerProfile.getAccounts();
        softly.assertThat(accountNumber).isEqualTo(accounts.getFirst().getAccountNumber());

    }
}
