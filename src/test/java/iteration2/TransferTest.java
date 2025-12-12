package iteration2;

import generators.MoneyMath;
import generators.RandomData;
import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;

import static specs.ResponseSpec.errorInvalidTransfer;
import static specs.ResponseSpec.errorTranslationLessZero;

public class TransferTest extends BaseTest {
    CreateUserRequest user1;
    CreateUserRequest user2;
    long id1;
    float balance1;
    float deposit1;
    int nonExistingId = 100500;
    CustomerAccountsResponse customerProfile1;

    @BeforeEach
    public void prepareData() {
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
        customerProfile1 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        id1 = customerProfile1.getAccounts().getFirst().getId();
        balance1 = customerProfile1.getAccounts().getFirst().getBalance();

        // вносим депозит на аккаунт 1 пользователя
        deposit1 = RandomData.getDeposit();

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(id1, deposit1, DepositRequest.class));

    }

    @Test
    public void userCanMakeTransferToYourOwnAccountTest() {
        // создаем второй аккаунт(счет) того же пользователя
        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        List<Account> accounts = customerProfile.getAccounts();
        // Находим индекс известного аккаунта
        int indexId1 = accounts.getFirst().getId() == id1 ? 0 : 1;
        int indexId2 = 1 - indexId1; // если 0 то 1, если 1 то 0

        long id2 = customerProfile.getAccounts().get(indexId2).getId();

        // вносим депозит на 2 счет того же пользователя
        float deposit2 = RandomData.getDeposit();
        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(id2, deposit2, DepositRequest.class));

        float transfer = MoneyMath.subtract(deposit1, 1);

        TransferRequest transferRequest = RandomModelGenerator.generate(id1, id2, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();
        float expectedBalance1 = MoneyMath.subtract(deposit1, transfer);
        float expectedBalance2 = MoneyMath.add(deposit2, transfer);

        softly.assertThat(response.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(expectedBalance1);

        softly.assertThat(response.getAccounts())
                .filteredOn(account -> account.getId() == id2)
                .extracting(Account::getBalance)
                .containsExactly(expectedBalance2);

    }

    @Test
    public void userCanMakeTransferToOtherOwnAccountTest() {

        //создание объекта 2 пользователя
        user2 = RandomModelGenerator.generate(CreateUserRequest.class);
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
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = MoneyMath.subtract(deposit1, 1);

        new CrudRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(id2, deposit2, DepositRequest.class));

        TransferRequest transferRequest = RandomModelGenerator.generate(id1, id2, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);
        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        CustomerAccountsResponse response2 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();
        float expectedBalance1 = MoneyMath.subtract(deposit1, transfer);
        float expectedBalance2 = MoneyMath.add(deposit2, transfer);

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(expectedBalance1);

        softly.assertThat(response2.getAccounts())
                .filteredOn(account -> account.getId() == id2)
                .extracting(Account::getBalance)
                .containsExactly(expectedBalance2);
    }

    @Test
    public void userCanMakeTransferToSameAccountTest() {

        float transfer = MoneyMath.subtract(deposit1, 1);

        TransferRequest transferRequest = RandomModelGenerator.generate(id1, id1, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

    }

    @Test
    public void userCanNotMakeTransferToYourOwnAccountMoreThenBalanseTest() {
        // создаем второй аккаунт(счет) того же пользователя
        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        List<Account> accounts = customerProfile.getAccounts();
        // Находим индекс известного аккаунта
        int indexId1 = accounts.getFirst().getId() == id1 ? 0 : 1;
        int indexId2 = 1 - indexId1; // если 0 то 1, если 1 то 0

        long id2 = customerProfile.getAccounts().get(indexId2).getId();
        float balance2 = customerProfile.getAccounts().get(indexId2).getBalance();

        float transfer = MoneyMath.add(deposit1, RandomData.getDeposit());

        TransferRequest transferRequest = RandomModelGenerator.generate(id1, id2, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(response.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

        softly.assertThat(response.getAccounts())
                .filteredOn(account -> account.getId() == id2)
                .extracting(Account::getBalance)
                .containsExactly(balance2);
    }

    @Test
    public void userCanNotMakeTransferToOtherOwnAccountMoreThenBalansTest() {
        //создание объекта 2 пользователя
        user2 = RandomModelGenerator.generate(CreateUserRequest.class);
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

        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = MoneyMath.add(deposit1, RandomData.getDeposit());

        new CrudRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(id2, deposit2, DepositRequest.class));

        TransferRequest transferRequest = RandomModelGenerator.generate(id1, id2, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        CustomerAccountsResponse response2 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

        softly.assertThat(response2.getAccounts())
                .filteredOn(account -> account.getId() == id2)
                .extracting(Account::getBalance)
                .containsExactly(deposit2);
    }

    @Test
    public void userCanNotMakeTransferToYourOwnAccountNegativeSumTest() {
        // создаем второй аккаунт(счет) того же пользователя
        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        List<Account> accounts = customerProfile.getAccounts();
        // Находим индекс известного аккаунта
        int indexId1 = accounts.getFirst().getId() == id1 ? 0 : 1;
        int indexId2 = 1 - indexId1; // если 0 то 1, если 1 то 0

        long id2 = customerProfile.getAccounts().get(indexId2).getId();
        float balance2 = customerProfile.getAccounts().get(indexId2).getBalance();

        float transfer = -RandomData.getDeposit();

        TransferRequest transferRequest = RandomModelGenerator.generate(id1, id2, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorTranslationLessZero))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(response.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

        softly.assertThat(response.getAccounts())
                .filteredOn(account -> account.getId() == id2)
                .extracting(Account::getBalance)
                .containsExactly(balance2);
    }

    @Test
    public void userCanNotMakeTransferToOtherOwnAccountNegativeSumTest() {
        //создание объекта 2 пользователя
        user2 = RandomModelGenerator.generate(CreateUserRequest.class);
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
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = -RandomData.getDeposit();

        new CrudRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(id2, deposit2, DepositRequest.class));

        TransferRequest transferRequest = RandomModelGenerator.generate(id1, id2, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorTranslationLessZero))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        CustomerAccountsResponse response2 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

        softly.assertThat(response2.getAccounts())
                .filteredOn(account -> account.getId() == id2)
                .extracting(Account::getBalance)
                .containsExactly(deposit2);
    }

    @Test
    public void userCanNotMakeTransferToOnNotExistAccountTest() {
        float transfer = RandomData.getDeposit();

        TransferRequest transferRequest = RandomModelGenerator.generate(id1, nonExistingId, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);
    }

    @Test
    public void userCanNotMakeTransferFromOnNotExistAccountTest() {
        float transfer = RandomData.getDeposit();

        TransferRequest transferRequest = RandomModelGenerator.generate(nonExistingId, id1, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);
    }

    @Test
    public void userCanNotMakeTransferFromOtherOwnAccountTest() {

        //создание объекта 2 пользователя
        user2 = RandomModelGenerator.generate(CreateUserRequest.class);
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
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = RandomData.getDeposit();

        new CrudRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(id2, deposit2, DepositRequest.class));

        TransferRequest transferRequest = RandomModelGenerator.generate(id2, id1, transfer, TransferRequest.class);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

    }

}

