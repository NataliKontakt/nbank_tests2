package iteration2;

import generators.MoneyMath;
import generators.RandomData;
import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.*;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;
import java.util.Map;

import static models.UserRole.USER;
import static specs.ResponseSpec.errorDepositCannotExceed_5000;
import static specs.ResponseSpec.errorDepositLessZero;

public class DepositTest extends BaseTest {
    CreateUserRequest user1;
    CustomerAccountsResponse customerAccounts;
    CustomerAccountsResponse customerAccountsNew;
    CustomerAccountsResponse accountsNegativeResponse;
    DepositResponse depositResponse;
    long id;
    float balance;
    float expectedBalance;

    @BeforeEach
    public void prepareData(TestInfo testInfo) {
        //создание объекта пользователя
        user1 = RandomModelGenerator.generate(CreateUserRequest.class);
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

        if (testInfo.getTags().contains("Negative")) {
            accountsNegativeResponse = new ValidatedCrudRequester<CustomerAccountsResponse>(
                    RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                    Endpoint.CUSTOMER_ACCOUNTS,
                    ResponseSpec.requestReturnsOk())
                    .get();
        }

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
            ModelAssertions.assertThatModels(depositResponse, customerAccountsNew).match();
            //softly.assertThat(expectedBalance).isEqualTo(accounts.getFirst().getBalance());
        } else if (testInfo.getTags().contains("Negative")) {
            ModelAssertions.assertThatModels(customerAccountsNew,accountsNegativeResponse).match();
            //softly.assertThat(balance).isEqualTo(customerAccounts.getAccounts().getFirst().getBalance());
        }

    }

    @Tag("Positive")
    @Test
    public void userCanMakeDepositTest() {

        depositResponse = new ValidatedCrudRequester<DepositResponse>(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(
                        DepositRequest.class,
                        Map.of("id", id)));

    }

    //проверяем сложение не нулевого баланса с депозитом и граничное значение 5000
    @Tag("Positive")
    @Test
    public void userCanMakeDepositNotZeroBalanceTest() {

        // вносим депозит
        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(
                        DepositRequest.class,
                        Map.of("id", id)));

        // вносим депозит еще
        float deposit2 = 5000;

        depositResponse = new ValidatedCrudRequester<DepositResponse>(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(
                        DepositRequest.class,
                        Map.of("id", id, "balance", deposit2)));

    }

    @Tag("Negative")
    @Test
    public void depositCanNotBeNegativeTest() {
        // вносим депозит
        float deposit = -1;

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsBadRequest(errorDepositLessZero))
                .post(RandomModelGenerator.generate(
                        DepositRequest.class,
                        Map.of("id", id, "balance", deposit)));

    }

    @Tag("Negative")
    @Test
    public void depositCanNotBeMore5000Test() {
        // вносим депозит
        float deposit = 5001;

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsBadRequest(errorDepositCannotExceed_5000))
                .post(RandomModelGenerator.generate(DepositRequest.class,
                        Map.of("id", id, "balance", deposit)));

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
                .post(RandomModelGenerator.generate(DepositRequest.class,
                        Map.of("id", nonExistingId)));

    }

    @Tag("Negative")
    @Test
    public void depositToSomeoneAccountIsNotPossibleTest() {

        //создание объекта 2 пользователя
        CreateUserRequest user2 = RandomModelGenerator.generate(CreateUserRequest.class);
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

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(RandomModelGenerator.generate(DepositRequest.class,
                        Map.of("id", id2)));


    }

}
