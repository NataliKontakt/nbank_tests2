package iteration2.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.MoneyMath;
import api.generators.RandomData;
import api.generators.RandomModelGenerator;
import api.requests.steps.DataBaseSteps;
import iteration1.api.BaseTest;
import api.models.Account;
import api.models.CreateUserRequest;
import api.models.CustomerAccountsResponse;
import api.models.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

import java.util.List;
import java.util.Optional;

import static api.specs.ResponseSpec.errorInvalidTransfer;
import static api.specs.ResponseSpec.errorTranslationLessZero;
@Tag("api")
@Tag("iteration-2")
public class TransferTest extends BaseTest {
    CreateUserRequest user1;
    CreateUserRequest user2;
    long id1;
    float balance1;
    float deposit1;
    int nonExistingId = 100500;
    CustomerAccountsResponse castomerAccount1;
    UserSteps userSteps;

    @BeforeEach
    public void prepareData() {
        //создание объекта пользователя
        user1 = AdminSteps.createUser();
        userSteps = new UserSteps(user1.getUsername(), user1.getPassword());

        // создаем аккаунт(счет)
        userSteps.createAccount();

        //через гет получаем номер аккаунта
        castomerAccount1 = userSteps.getAccount();
        id1 = castomerAccount1.getAccounts().getFirst().getId();
        balance1 = castomerAccount1.getAccounts().getFirst().getBalance();

        // вносим депозит на аккаунт 1 пользователя
        deposit1 = RandomData.getDeposit();
        userSteps.makeDeposit(id1, deposit1);

    }

    @Test
    public void userCanMakeTransferToYourOwnAccountTest() {
        // создаем второй аккаунт(счет) того же пользователя
        userSteps.createAccount();

        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = userSteps.getAccount();

        List<Account> accounts = customerProfile.getAccounts();
        // Находим индекс известного аккаунта
        int indexId1 = accounts.getFirst().getId() == id1 ? 0 : 1;
        int indexId2 = 1 - indexId1; // если 0 то 1, если 1 то 0

        long id2 = customerProfile.getAccounts().get(indexId2).getId();

        // вносим депозит на 2 счет того же пользователя
        float deposit2 = RandomData.getDeposit();
        userSteps.makeDeposit(id2, deposit2);

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
        CustomerAccountsResponse response = userSteps.getAccount();
        float expectedBalance1 = MoneyMath.subtract(deposit1, transfer);
        float expectedBalance2 = MoneyMath.add(deposit2, transfer);

        Account actualAccount1 = response.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        Account actualAccount2 = response.getAccounts().stream()
                .filter(account -> account.getId() == id2)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(expectedBalance1);
        softly.assertThat(actualAccount2.getBalance()).isEqualTo(expectedBalance2);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        AccountDao accountDao2 = DataBaseSteps.getAccountById(id2);

        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();
        DaoAndModelAssertions.assertThat(actualAccount2, accountDao2).match();
    }

    @Test
    public void userCanMakeTransferToOtherOwnAccountTest() {
        //создание 2 пользователя
        user2 = AdminSteps.createUser();
        UserSteps userSteps2 = new UserSteps(user2.getUsername(), user2.getPassword());
        // создаем аккаунт(счет) 2 пользователя
        userSteps2.createAccount();
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = userSteps2.getAccount();

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = MoneyMath.subtract(deposit1, 1);

        userSteps2.makeDeposit(id2, deposit2);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);
        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = userSteps.getAccount();

        CustomerAccountsResponse response2 = userSteps2.getAccount();
        float expectedBalance1 = MoneyMath.subtract(deposit1, transfer);
        float expectedBalance2 = MoneyMath.add(deposit2, transfer);

        Account actualAccount1 = response1.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        Account actualAccount2 = response2.getAccounts().stream()
                .filter(account -> account.getId() == id2)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(expectedBalance1);
        softly.assertThat(actualAccount2.getBalance()).isEqualTo(expectedBalance2);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        AccountDao accountDao2 = DataBaseSteps.getAccountById(id2);

        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();
        DaoAndModelAssertions.assertThat(actualAccount2, accountDao2).match();
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
        CustomerAccountsResponse response1 = userSteps.getAccount();

        Account actualAccount1 = response1.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(deposit1);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);

        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();
    }

    @Test
    public void userCanNotMakeTransferToYourOwnAccountMoreThenBalanseTest() {
        // создаем второй аккаунт(счет) того же пользователя
        userSteps.createAccount();
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = userSteps.getAccount();

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
        CustomerAccountsResponse response = userSteps.getAccount();

        Account actualAccount1 = response.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(deposit1);

        Account actualAccount2 = response.getAccounts().stream()
                .filter(account -> account.getId() == id2)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount2.getBalance()).isEqualTo(balance2);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();

        AccountDao accountDao2 = DataBaseSteps.getAccountById(id2);
        DaoAndModelAssertions.assertThat(actualAccount2, accountDao2).match();
    }

    @Test
    public void userCanNotMakeTransferToOtherOwnAccountMoreThenBalansTest() {
        //создание объекта 2 пользователя
        user2 = AdminSteps.createUser();
        UserSteps userSteps2 = new UserSteps(user2.getUsername(), user2.getPassword());
        // создаем аккаунт(счет) 2 пользователя
        userSteps2.createAccount();
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = userSteps2.getAccount();
        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = MoneyMath.add(deposit1, RandomData.getDeposit());

        userSteps2.makeDeposit(id2, deposit2);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = userSteps.getAccount();

        CustomerAccountsResponse response2 = userSteps2.getAccount();

        Account actualAccount1 = response1.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(deposit1);

        Account actualAccount2 = response2.getAccounts().stream()
                .filter(account -> account.getId() == id2)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount2.getBalance()).isEqualTo(deposit2);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();

        AccountDao accountDao2 = DataBaseSteps.getAccountById(id2);
        DaoAndModelAssertions.assertThat(actualAccount2, accountDao2).match();

    }

    @Test
    public void userCanNotMakeTransferToYourOwnAccountNegativeSumTest() {
        // создаем второй аккаунт(счет) того же пользователя
        userSteps.createAccount();
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = userSteps.getAccount();

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
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = userSteps.getAccount();

        Account actualAccount1 = response.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(deposit1);

        Account actualAccount2 = response.getAccounts().stream()
                .filter(account -> account.getId() == id2)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount2.getBalance()).isEqualTo(balance2);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();

        AccountDao accountDao2 = DataBaseSteps.getAccountById(id2);
        DaoAndModelAssertions.assertThat(actualAccount2, accountDao2).match();
    }

    @Test
    public void userCanNotMakeTransferToOtherOwnAccountNegativeSumTest() {
        //создание объекта 2 пользователя
        user2 = AdminSteps.createUser();
        UserSteps userSteps2 = new UserSteps(user2.getUsername(), user2.getPassword());
        // создаем аккаунт(счет) 2 пользователя
        userSteps2.createAccount();
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = userSteps2.getAccount();

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = -RandomData.getDeposit();

        userSteps2.makeDeposit(id2, deposit2);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id1);
        transferRequest.setReceiverAccountId(id2);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = userSteps.getAccount();

        CustomerAccountsResponse response2 = userSteps2.getAccount();

        Account actualAccount1 = response1.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(deposit1);

        Account actualAccount2 = response2.getAccounts().stream()
                .filter(account -> account.getId() == id2)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount2.getBalance()).isEqualTo(deposit2);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();

        AccountDao accountDao2 = DataBaseSteps.getAccountById(id2);
        DaoAndModelAssertions.assertThat(actualAccount2, accountDao2).match();
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
        CustomerAccountsResponse response1 = userSteps.getAccount();

        Account actualAccount1 = response1.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(deposit1);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();
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
        CustomerAccountsResponse response1 = userSteps.getAccount();

        Account actualAccount1 = response1.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(deposit1);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();
    }

    @Test
    public void userCanNotMakeTransferFromOtherOwnAccountTest() {

        //создание объекта 2 пользователя
        user2 = AdminSteps.createUser();
        UserSteps userSteps2 = new UserSteps(user2.getUsername(), user2.getPassword());
        // создаем аккаунт(счет) 2 пользователя
        userSteps2.createAccount();
        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = userSteps2.getAccount();

        long id2 = customerProfile.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = RandomData.getDeposit();

        userSteps2.makeDeposit(id2, deposit2);

        TransferRequest transferRequest = RandomModelGenerator.generate(TransferRequest.class);
        transferRequest.setSenderAccountId(id2);
        transferRequest.setReceiverAccountId(id1);
        transferRequest.setAmount(transfer);

        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = userSteps.getAccount();

        Account actualAccount1 = response1.getAccounts().stream()
                .filter(account -> account.getId() == id1)
                .findFirst()
                .orElse(null);

        softly.assertThat(actualAccount1.getBalance()).isEqualTo(deposit1);

        AccountDao accountDao1 = DataBaseSteps.getAccountById(id1);
        DaoAndModelAssertions.assertThat(actualAccount1, accountDao1).match();

    }

}

