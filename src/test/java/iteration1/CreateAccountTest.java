package iteration1;

import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.CustomerAccountsResponse;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.UpdateCustomerProfileRequester;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import static models.UserRole.USER;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {

        //создание объекта пользователя
        CreateUserRequest user1 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание пользователя
        new CrudRequester(RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.entityWasCreatad())
                .post(user1);

        // создаем аккаунт(счет)
        CreateAccountResponse response =  new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);
        String accountNumber = response.getAccountNumber();

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там

        CustomerAccountsResponse customerProfileNew = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(accountNumber).isEqualTo(customerProfileNew.getAccounts().getFirst().getAccountNumber());

    }
}
