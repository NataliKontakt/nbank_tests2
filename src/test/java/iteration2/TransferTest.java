package iteration2;

import generators.MoneyMath;
import generators.RandomData;
import io.restassured.response.ValidatableResponse;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.*;
import specs.RequestSpec;
import specs.ResponseSpec;

import static models.UserRole.USER;
import static specs.ResponseSpec.errorInvalidTransfer;
import static specs.ResponseSpec.errorTranslationLessZero;

public class TransferTest extends BaseTest {
    CreateUserRequest user1;
    CreateUserRequest user2;
    int id1;
    float balance1;
    float deposit1;
    int nonExistingId = 100500;

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
        ValidatableResponse createAcc1 = new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        id1 = createAcc1.extract().path("id");
        balance1 = createAcc1.extract().path("balance");

        // вносим депозит на аккаунт 1 пользователя
        deposit1 = RandomData.getDeposit();

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id1)
                        .balance(deposit1)
                        .build());
    }

    @Test
    public void userCanMakeTransferToYourOwnAccountTest() {
        // создаем второй аккаунт(счет) того же пользователя
        ValidatableResponse validatableResponse = new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);
        int id2 = validatableResponse.extract().path("id");

        // вносим депозит на 2 счет того же пользователя
        float deposit2 = RandomData.getDeposit();
        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        float transfer = MoneyMath.subtract(deposit1, 1);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();
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
        user2 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание 2 пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user2);

        // создаем аккаунт(счет) 2 пользователя
        ValidatableResponse validatableResponse = new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);
        int id2 = validatableResponse.extract().path("id");

        float deposit2 = RandomData.getDeposit();
        float transfer = MoneyMath.subtract(deposit1, 1);

        new DepositRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);
        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        CustomerAccountsResponse response2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();
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

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id1)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

    }

    @Test
    public void userCanNotMakeTransferToYourOwnAccountMoreThenBalanseTest() {
        // создаем второй аккаунт(счет) того же пользователя
        ValidatableResponse validatableResponse = new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);
        int id2 = validatableResponse.extract().path("id");
        float balance2 = validatableResponse.extract().path("balance");

        float transfer = MoneyMath.add(deposit1, RandomData.getDeposit());

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

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
        user2 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание 2 пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user2);

        // создаем аккаунт(счет) 2 пользователя
        ValidatableResponse validatableResponse = new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);
        int id2 = validatableResponse.extract().path("id");

        float deposit2 = RandomData.getDeposit();
        float transfer = MoneyMath.add(deposit1, RandomData.getDeposit());

        new DepositRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        CustomerAccountsResponse response2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

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
        ValidatableResponse validatableResponse = new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);
        int id2 = validatableResponse.extract().path("id");
        float balance2 = validatableResponse.extract().path("balance");

        float transfer = -RandomData.getDeposit();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorTranslationLessZero))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

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
        user2 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание 2 пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user2);

        // создаем аккаунт(счет) 2 пользователя
        ValidatableResponse validatableResponse = new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);
        int id2 = validatableResponse.extract().path("id");

        float deposit2 = RandomData.getDeposit();
        float transfer = -RandomData.getDeposit();

        new DepositRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorTranslationLessZero))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        CustomerAccountsResponse response2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

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

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(nonExistingId)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorInvalidTransfer))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);
    }

    @Test
    public void userCanNotMakeTransferFromOnNotExistAccountTest() {
        float transfer = RandomData.getDeposit();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(nonExistingId)
                .receiverAccountId(id1)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);
    }

    @Test
    public void userCanNotMakeTransferFromOtherOwnAccountTest() {

        //создание объекта 2 пользователя
        user2 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание 2 пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user2);

        // создаем аккаунт(счет) 2 пользователя
        ValidatableResponse validatableResponse = new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);
        int id2 = validatableResponse.extract().path("id");

        float deposit2 = RandomData.getDeposit();
        float transfer = RandomData.getDeposit();

        new DepositRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id2)
                .receiverAccountId(id1)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(response1.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

    }

}

