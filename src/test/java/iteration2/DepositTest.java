package iteration2;

import generators.RandomData;
import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.*;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.*;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;
import java.util.Map;

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

    @BeforeEach
    public void prepareData(TestInfo testInfo) {
        //создание объекта пользователя
        user1 = AdminSteps.createUser();

        // создаем аккаунт(счет)
        UserSteps.createAccount(user1.getUsername(), user1.getPassword());

        //через гет получаем номер аккаунта
        customerAccounts = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        id = customerAccounts.getAccounts().getFirst().getId();
        balance = customerAccounts.getAccounts().getFirst().getBalance();

        if (testInfo.getTags().contains("Negative")) {
            accountsNegativeResponse = UserSteps.getAccount(user1.getUsername(), user1.getPassword());
        }

    }

    @AfterEach
    public void assertTest(TestInfo testInfo) {
        //через гет получаем новый баланс и сверяем с ожидаемым

        customerAccountsNew = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        if (testInfo.getTags().contains("Positive")) {
            ModelAssertions.assertThatModels(depositResponse, customerAccountsNew).match();
            //softly.assertThat(expectedBalance).isEqualTo(accounts.getFirst().getBalance());
        } else if (testInfo.getTags().contains("Negative")) {
            ModelAssertions.assertThatModels(customerAccountsNew, accountsNegativeResponse).match();
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
        CreateUserRequest user2 = AdminSteps.createUser();

        // создаем аккаунт(счет) 2 пользователя
        UserSteps.createAccount(user2.getUsername(), user2.getPassword());

        //через гет получаем номер аккаунта 2 пользователя
        CustomerAccountsResponse customerAccounts2 = UserSteps.getAccount(user2.getUsername(), user2.getPassword());

        long id2 = customerAccounts2.getAccounts().getFirst().getId();

        // вносим депозит

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(RandomModelGenerator.generate(DepositRequest.class,
                        Map.of("id", id2)));


    }

}
