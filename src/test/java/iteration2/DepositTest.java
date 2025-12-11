package iteration2;

import generators.MoneyMath;
import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.*;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.UpdateCustomerProfileRequester;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;

import static models.UserRole.USER;
import static specs.ResponseSpec.errorDepositCannotExceed_5000;
import static specs.ResponseSpec.errorDepositLessZero;

public class DepositTest extends BaseTest {
    CreateUserRequest user1;
    CustomerAccountsResponse customerAccounts;
    CustomerAccountsResponse customerAccountsNew;
    long id;
    float balance;
    float expectedBalance;

    @BeforeEach
    public void prepareData() {
        //создание объекта пользователя
        user1 = CreateUserRequest.builder()
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
        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта
        customerAccounts = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        id = customerAccounts.getAccounts().getFirst().getId();
        balance = customerAccounts.getAccounts().getFirst().getBalance();
    }

    @AfterEach
    public void assertTest(TestInfo testInfo){
        //через гет получаем новый баланс и сверяем с ожидаемым
        customerAccountsNew = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();
        List<Account> accounts = customerAccountsNew.getAccounts();

        if (testInfo.getTags().contains("Positive")) {
            softly.assertThat(expectedBalance).isEqualTo(accounts.getFirst().getBalance());
        } else if (testInfo.getTags().contains("Negative")) {
            softly.assertThat(balance).isEqualTo(accounts.getFirst().getBalance());
        }

    }

    @Tag("Positive")
    @Test
    public void userCanMakeDepositTest() {

        // вносим депозит
        float deposit = RandomData.getDeposit();
        expectedBalance = MoneyMath.add(balance, deposit);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());
    }

    //проверяем сложение не нулевого баланса с депозитом и граничное значение 5000
    @Tag("Positive")
    @Test
    public void userCanMakeDepositNotZeroBalanceTest() {

        // вносим депозит
        float deposit = RandomData.getDeposit();

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());

        // вносим депозит еще
        float deposit2 = 5000;
        expectedBalance = balance + deposit + deposit2;

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit2)
                        .build());

    }

    @Tag("Negative")
    @Test
    public void depositCanNotBeNegativeTest() {
        // вносим депозит
        float deposit = -1;

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsBadRequest(errorDepositLessZero))
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());

    }

    @Tag("Negative")
    @Test
    public void depositCanNotBeMore5000Test() {
        // вносим депозит
        float deposit = 5001;

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsBadRequest(errorDepositCannotExceed_5000))
                .post(DepositRequest.builder()
                        .id(id)
                        .balance(deposit)
                        .build());

    }

    @Tag("Negative")
    @Test
    public void depositCanNotBeOnNotExistAccount() {

        // несуществующий id
        int nonExistingId = 100500;
        // вносим депозит
        float deposit = RandomData.getDeposit();

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(DepositRequest.builder()
                        .id(nonExistingId)
                        .balance(deposit)
                        .build());
    }

    @Tag("Negative")
    @Test
    public void depositToSomeoneAccountIsNotPossibleTest() {

        //создание объекта 2 пользователя
        CreateUserRequest user2 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание 2 пользователя
        new CrudRequester(RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.entityWasCreatad())
                .post(user2);

        // создаем аккаунт(счет) 2 пользователя
        new CrudRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта 2 пользователя
        CustomerAccountsResponse customerAccounts2 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();


        long id2 = customerAccounts2.getAccounts().getFirst().getId();

        // вносим депозит
        float deposit = RandomData.getDeposit();

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit)
                        .build());

    }

}
