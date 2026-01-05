package iteration1.api;

import api.models.Account;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.CustomerAccountsResponse;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

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
