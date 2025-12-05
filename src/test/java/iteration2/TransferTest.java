package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.*;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;

import static models.UserRole.USER;

public class TransferTest extends BaseTest {
    CreateUserRequest user1;
    CreateUserRequest user2;
    CustomerAccountsResponse customerProfile;
    CustomerAccountsResponse customerProfile2;
    long id1;
    float balance1;
    float deposit1;

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


        id1 = customerProfile.getAccounts().getFirst().getId();

        // вносим депозит на аккаунт 1 пользователя
        deposit1 = RandomData.getDeposit();
        balance1 = customerProfile.getAccounts().getFirst().getBalance();
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
        new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта
        customerProfile2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        long id2 = customerProfile2.getAccounts().getFirst().getId();
        // вносим депозит на 2 счет того же пользователя
        float deposit2 = RandomData.getDeposit();
        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        float transfer = deposit1 - 1;

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
        float expectedBalance1 = deposit1 - transfer;
        float expectedBalance2 = deposit2 + transfer;

        // Проверяем напрямую из response
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
        new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта 2 пользователя
        customerProfile2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        long id2 = customerProfile2.getAccounts().getFirst().getId();

        float deposit2 = 300.75f;
        float transfer = 250;

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
        CustomerAccountsResponse customerProfileNew1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        CustomerAccountsResponse customerProfileNew2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        float expectedBalance1 = deposit1 - transfer;
        System.out.println("expectedBalance1 " + expectedBalance1 );
        float expectedBalance2 = deposit2 + transfer;
        System.out.println("expectedBalance2 " + expectedBalance2 );


        softly.assertThat(expectedBalance1).isEqualTo(customerProfileNew1.getAccounts().getFirst().getBalance());
        softly.assertThat(expectedBalance2).isEqualTo(customerProfileNew2.getAccounts().getFirst().getBalance());

    }

    @Test
    public void userCanMakeTransferToSameAccountTest() {

        float transfer = 250.75f;

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id1)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse customerProfileNew1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(deposit1).isEqualTo(customerProfileNew1.getAccounts().getFirst().getBalance());

    }

    @Test
    public void userCanNotMakeTransferToYourOwnAccountMoreThenBalansTest() {
        String errorValue = "Invalid transfer: insufficient funds or invalid accounts";

        // создаем второй аккаунт(счет) того же пользователя
        new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта
        customerProfile2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        long id2 = customerProfile2.getAccounts().get(1).getId();
        float balance = customerProfile2.getAccounts().get(1).getBalance();
        /*// вносим депозит на 2 счет того же пользователя
        float deposit2 = RandomData.getDeposit();
        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());*/

        float transfer = balance1 + RandomData.getDeposit();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse customerProfileNew1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(deposit1).isEqualTo(customerProfileNew1.getAccounts().get(0).getBalance());
        softly.assertThat(balance).isEqualTo(customerProfileNew1.getAccounts().get(1).getBalance());
    }

    @Test
    public void userCanNotMakeTransferToOtherOwnAccountMoreThenBalansTest() {
        String errorValue = "Invalid transfer: insufficient funds or invalid accounts";
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
        new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта 2 пользователя
        customerProfile2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        long id2 = customerProfile2.getAccounts().getFirst().getId();

        float deposit2 = RandomData.getDeposit();
        float transfer = deposit1 + RandomData.getDeposit();

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
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse customerProfileNew1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        CustomerAccountsResponse customerProfileNew2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(deposit1).isEqualTo(customerProfileNew1.getAccounts().getFirst().getBalance());
        softly.assertThat(deposit2).isEqualTo(customerProfileNew2.getAccounts().getFirst().getBalance());

    }

    @Test
    public void userCanNotMakeTransferToYourOwnAccountNegativeSumTest() {

        String errorValue = "Transfer amount must be at least 0.01";

        // создаем второй аккаунт(счет) того же пользователя
        new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта
        customerProfile2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        long id2 = customerProfile2.getAccounts().get(1).getId();
        float balance = customerProfile2.getAccounts().get(1).getBalance();

        float transfer = -RandomData.getDeposit();
        System.out.println("transfer " + transfer);

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse response = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

// Проверяем напрямую из response
        softly.assertThat(response.getAccounts())
                .filteredOn(account -> account.getId() == id1)
                .extracting(Account::getBalance)
                .containsExactly(deposit1);

        softly.assertThat(response.getAccounts())
                .filteredOn(account -> account.getId() == id2)
                .extracting(Account::getBalance)
                .containsExactly(balance);
    }

    @Test
    public void userCanNotMakeTransferToOtherOwnAccountNegativeSumTest() {

        String errorValue = "Transfer amount must be at least 0.01";
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
        new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта 2 пользователя
        customerProfile2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        long id2 = customerProfile2.getAccounts().getFirst().getId();

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
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse customerProfileNew1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        CustomerAccountsResponse customerProfileNew2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(deposit1).isEqualTo(customerProfileNew1.getAccounts().getFirst().getBalance());
        softly.assertThat(deposit2).isEqualTo(customerProfileNew2.getAccounts().getFirst().getBalance());
    }

    @Test
    public void userCanNotMakeTransferToOnNotExistAccountTest() {
        String errorValue = "Invalid transfer: insufficient funds or invalid accounts";
        long id2 = 100500;
        float transfer = RandomData.getDeposit();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse customerProfileNew1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(deposit1).isEqualTo(customerProfileNew1.getAccounts().getFirst().getBalance());

    }

    @Test
    public void userCanNotMakeTransferFromOnNotExistAccountTest() {
        long id2 = 100500;
        float transfer = RandomData.getDeposit();

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id2)
                .receiverAccountId(id1)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsForbiddenRequest())
                .post(transferRequest);

        //через гет получаем новый баланс и сверяем с ожидаемым
        CustomerAccountsResponse customerProfileNew1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(deposit1).isEqualTo(customerProfileNew1.getAccounts().getFirst().getBalance());
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
        new CreateAccountRequester(RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта 2 пользователя
        customerProfile2 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user2.getUsername(), user2.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        long id2 = customerProfile2.getAccounts().getFirst().getId();

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
        CustomerAccountsResponse customerProfileNew1 = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(deposit1).isEqualTo(customerProfileNew1.getAccounts().getFirst().getBalance());

    }

}

