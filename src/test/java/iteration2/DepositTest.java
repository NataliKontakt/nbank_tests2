package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.CreateUserRequest;
import models.CustomerAccountsResponse;
import models.DepositRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.UpdateCustomerProfileRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import static models.UserRole.USER;

public class DepositTest extends BaseTest {
    CreateUserRequest user1;
    CustomerAccountsResponse customerProfile;
    long id;
    float balance;

    @BeforeEach
    public void prepareData() {
        //создание объекта пользователя
        user1 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user1);

        // создаем аккаунт(счет)
        new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта
        customerProfile = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        id = customerProfile.getAccounts().getFirst().getId();
        balance = customerProfile.getAccounts().getFirst().getBalance();
    }

    @Test
    public void userCanMakeDepositTest() {

        // вносим депозит
        float deposit = 50;
        float expectedBalance = balance + deposit;

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse customerProfileNew = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(expectedBalance).isEqualTo(customerProfileNew.getAccounts().getFirst().getBalance());

    }

    //проверяем сложение не нулевого баланса с депозитом и граничное значение 5000
    @Test
    public void userCanMakeDepositNotZeroBalanceTest() {

        // вносим депозит
        float deposit = 50;

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());

        // вносим депозит еще
        float deposit2 = 5000;
        float expectedBalance = balance + deposit + deposit2;

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit2)
                        .build());

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse customerProfileNew = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(expectedBalance).isEqualTo(customerProfileNew.getAccounts().getFirst().getBalance());

    }

    @Test
    public void depositCanNotBeNegativeTest() {

        // вносим депозит
        float deposit = -1;
        String errorValue = "Deposit amount must be at least 0.01";

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());

    }

    @Test
    public void depositCanNotBeMore5000Test() {

        // вносим депозит
        float deposit = 5001;

        String errorValue = "Deposit amount cannot exceed 5000";

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());
    }

    @Test
    public void depositCanNotBeOnNotExistAccount() {

        // несуществующий id
        int id = 100500;
        // вносим депозит
        float deposit = 500;

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());
    }

    @Test
    public void depositToSomeoneAccountIsNotPossibleTest() {

        //создание объекта 2 пользователя
        CreateUserRequest user2 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание 2 пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user2);


        // создаем аккаунт(счет) 2 пользователя
        new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта 2 пользователя
        CustomerAccountsResponse customerProfile2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        long id2 = customerProfile2.getAccounts().getFirst().getId();

        // вносим депозит
        float deposit = 500;

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit)
                        .build());

    }

}
