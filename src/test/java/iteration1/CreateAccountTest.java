package iteration1;

import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import models.CreateUserRequest;
import models.CustomerAccountsResponse;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.UpdateCustomerProfileRequester;
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
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user1);

        // создаем аккаунт(счет)
        ValidatableResponse response =  new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);
        String accountNumber = response.extract().jsonPath().getString("accountNumber");

        // запросить все аккаунты пользователя и проверить, что наш аккаунт там

        CustomerAccountsResponse customerProfileNew = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(accountNumber).isEqualTo(customerProfileNew.getAccounts().getFirst().getAccountNumber());

    }
}
