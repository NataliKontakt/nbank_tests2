package iteration2;

import generators.MoneyMath;
import generators.RandomData;
import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.Account;
import models.CreateUserRequest;
import models.CustomerAccountsResponse;
import models.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
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
    CustomerAccountsResponse castomerAccount1;

    @BeforeEach
    public void prepareData() {
        //создание объекта пользователя
        user1 = AdminSteps.createUser();

        // создаем аккаунт(счет)
        UserSteps.createAccount(user1.getUsername(), user1.getPassword());

        //через гет получаем номер аккаунта
        castomerAccount1 = UserSteps.getAccount(user1.getUsername(), user1.getPassword());
        id1 = castomerAccount1.getAccounts().getFirst().getId();
        balance1 = castomerAccount1.getAccounts().getFirst().getBalance();

        // вносим депозит на аккаунт 1 пользователя
        deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user1.getUsername(), user1.getPassword(), id1, deposit1);

    }

    @Test
    public void userCanMakeTransferToYourOwnAccountTest() {
        // создаем второй аккаунт(счет) того же пользователя
        UserSteps.createAccount(user1.getUsername(), user1.getPassword());

        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        List<Account> accounts = customerProfile.getAccounts();
        // Находим индекс известного аккаунта
        int indexId1 = accounts.getFirst().getId() == id1 ? 0 : 1;
        int indexId2 = 1 - indexId1; // если 0 то 1, если 1 то 0

        long id2 = customerProfile.getAccounts().get(indexId2).getId();

        // вносим депозит на 2 счет того же пользователя
        float deposit2 = RandomData.getDeposit();
        UserSteps.makeDeposit(user1.getUsername(), user1.getPassword(), id2, deposit2);

        float transfer = MoneyMath.subtract(deposit1, 1);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);


        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = UserSteps.getAccount(user1.getUsername(), user1.getPassword());
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
        //создание 2 пользователя
        user2 = AdminSteps.createUser();

        // создаем аккаунт(счет) 2 пользователя
        UserSteps.createAccount(user2.getUsername(), user2.getPassword());
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = UserSteps.getAccount(user2.getUsername(), user2.getPassword());

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = MoneyMath.subtract(deposit1, 1);

        UserSteps.makeDeposit(user2.getUsername(), user2.getPassword(), id2, deposit2);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);
        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        CustomerAccountsResponse response2 = UserSteps.getAccount(user2.getUsername(), user2.getPassword());
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

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id1);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

    }

    @Test
    public void userCanNotMakeTransferToYourOwnAccountMoreThenBalanseTest() {
        // создаем второй аккаунт(счет) того же пользователя
        UserSteps.createAccount(user1.getUsername(), user1.getPassword());
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        List<Account> accounts = customerProfile.getAccounts();
        // Находим индекс известного аккаунта
        int indexId1 = accounts.getFirst().getId() == id1 ? 0 : 1;
        int indexId2 = 1 - indexId1; // если 0 то 1, если 1 то 0

        long id2 = customerProfile.getAccounts().get(indexId2).getId();
        float balance2 = customerProfile.getAccounts().get(indexId2).getBalance();

        float transfer = MoneyMath.add(deposit1, RandomData.getDeposit());

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

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
        user2 = AdminSteps.createUser();
        // создаем аккаунт(счет) 2 пользователя
        UserSteps.createAccount(user2.getUsername(), user2.getPassword());
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = UserSteps.getAccount(user2.getUsername(), user2.getPassword());
        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = MoneyMath.add(deposit1, RandomData.getDeposit());

        UserSteps.makeDeposit(user2.getUsername(), user2.getPassword(), id2, deposit2);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        CustomerAccountsResponse response2 = UserSteps.getAccount(user2.getUsername(), user2.getPassword());

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
        UserSteps.createAccount(user1.getUsername(), user1.getPassword());
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        List<Account> accounts = customerProfile.getAccounts();
        // Находим индекс известного аккаунта
        int indexId1 = accounts.getFirst().getId() == id1 ? 0 : 1;
        int indexId2 = 1 - indexId1; // если 0 то 1, если 1 то 0

        long id2 = customerProfile.getAccounts().get(indexId2).getId();
        float balance2 = customerProfile.getAccounts().get(indexId2).getBalance();

        float transfer = -RandomData.getDeposit();

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorTranslationLessZero))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

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
        user2 = AdminSteps.createUser();

        // создаем аккаунт(счет) 2 пользователя
        UserSteps.createAccount(user2.getUsername(), user2.getPassword());
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = UserSteps.getAccount(user2.getUsername(), user2.getPassword());

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = -RandomData.getDeposit();

        UserSteps.makeDeposit(user2.getUsername(), user2.getPassword(), id2, deposit2);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorTranslationLessZero))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        CustomerAccountsResponse response2 = UserSteps.getAccount(user2.getUsername(), user2.getPassword());

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
        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(nonExistingId);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);
    }

    @Test
    public void userCanNotMakeTransferFromOnNotExistAccountTest() {
        float transfer = RandomData.getDeposit();
        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(nonExistingId);
        transferRequest.setReceiverAccountId(id1);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);
    }

    @Test
    public void userCanNotMakeTransferFromOtherOwnAccountTest() {

        //создание объекта 2 пользователя
        user2 = AdminSteps.createUser();

        // создаем аккаунт(счет) 2 пользователя
        UserSteps.createAccount(user2.getUsername(), user2.getPassword());
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = UserSteps.getAccount(user2.getUsername(), user2.getPassword());

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = RandomData.getDeposit();

        UserSteps.makeDeposit(user2.getUsername(), user2.getPassword(), id2, deposit2);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id2);
        transferRequest.setReceiverAccountId(id1);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = UserSteps.getAccount(user1.getUsername(), user1.getPassword());

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

    }

}

