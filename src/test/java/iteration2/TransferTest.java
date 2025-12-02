package iteration2;

import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import iteration1.BaseTest;
import models.CreateUserRequest;
import models.CustomerAccountsResponse;
import models.DepositRequest;
import models.TransferRequest;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import requests.*;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static models.UserRole.USER;
import static org.hamcrest.Matchers.equalTo;

public class TransferTest extends BaseTest {
    CreateUserRequest user1;
    CreateUserRequest user2;
    CustomerAccountsResponse customerProfile;
    CustomerAccountsResponse customerProfile2;
    long id1;
    float balance1;
    float deposit1;
    /*long id2;
    float balance2;
    float deposit2;
*/
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


        id1 = customerProfile.getAccounts().get(0).getId();

        // вносим депозит на аккаунт 1 пользователя
        deposit1 = 500;
        balance1 = customerProfile.getAccounts().get(0).getBalance();
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


        long id2 = customerProfile2.getAccounts().get(1).getId();
        // вносим депозит на 2 счет того же пользователя
        float deposit2 = 300;
        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        float transfer = 250.75f;

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


        float expectedBalance1 = deposit1 - transfer;
        float expectedBalance2 = deposit2 + transfer;


        softly.assertThat(expectedBalance1).isEqualTo(customerProfileNew1.getAccounts().get(0).getBalance());
        softly.assertThat(expectedBalance2).isEqualTo(customerProfileNew1.getAccounts().get(1).getBalance());

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


        long id2 = customerProfile2.getAccounts().get(0).getId();
        float balance2 = customerProfile2.getAccounts().get(0).getBalance();

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
        float expectedBalance2 = deposit2 + transfer;


        softly.assertThat(expectedBalance1).isEqualTo(customerProfileNew1.getAccounts().get(0).getBalance());
        softly.assertThat(expectedBalance2).isEqualTo(customerProfileNew2.getAccounts().get(0).getBalance());

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

        softly.assertThat(deposit1).isEqualTo(customerProfileNew1.getAccounts().get(0).getBalance());

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
        // вносим депозит на 2 счет того же пользователя
        float deposit2 = 300;
        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        float transfer = 550.75f;

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(transferRequest);


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


        long id2 = customerProfile2.getAccounts().get(0).getId();
        float balance2 = customerProfile2.getAccounts().get(0).getBalance();

        float deposit2 = 300.75f;
        float transfer = 750;

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
        // вносим депозит на 2 счет того же пользователя
        float deposit2 = 300;
        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(DepositRequest.builder()
                        .id(id2)
                        .balance(deposit2)
                        .build());

        float transfer = -50.75f;

        TransferRequest transferRequest = TransferRequest.builder()
                .senderAccountId(id1)
                .receiverAccountId(id2)
                .amount(transfer)
                .build();

        new TransferRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .post(transferRequest);

    }

    @Test
    public void userCanNotMakeTransferToOtherOwnAccountNegativeSumTest() {

//        Создать пользователя1
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2045",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера1
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2045",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт пользователя 1 (счет)
        Response response = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        int id1 = response.path("id");
        System.out.println("ID = " + id1);

        // вносим депозит на аккаунт1
        float deposit1 = 500;

        String body = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id1, deposit1);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

//        Создать пользователя2
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "petr2045",
                          "password": "Petr2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера2
        String userAuthHeader2 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "petr2045",
                          "password": "Petr2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт2 (счет)
        Response response2 = given()
                .header("Authorization", userAuthHeader2)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        int id2 = response2.path("id");


        System.out.println("ID = " + id2);
//переводим деньги
        float transfer = -55;
        String bodyTransfer = String.format(Locale.US, """
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %f
                }
                """, id1, id2, transfer);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(bodyTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Transfer amount must be at least 0.01"));

    }

    @Test
    public void userCanNotMakeTransferToOnNotExistAccountTest() {

//        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2048",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2048",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт1 (счет)
        Response response = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        int id1 = response.path("id");
        float balance = response.path("balance");

        System.out.println("ID = " + id1);
        System.out.println("Balance = " + balance);

        // вносим депозит на аккаунт1
        float deposit1 = 500;

        String body = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id1, deposit1);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);


        //несуществующий аккаунт
        int id2 = 100500;

        float transfer = 50.75f;
        String bodyTransfer = String.format(Locale.US, """
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %f
                }
                """, id1, id2, transfer);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(bodyTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Invalid transfer: insufficient funds or invalid accounts"));


    }

    @Test
    public void userCanNotMakeTransferFromOnNotExistAccountTest() {

//        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2050",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2050",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт1 (счет)
        Response response = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        int id1 = response.path("id");


        //несуществующий аккаунт
        int id2 = 100500;

        float transfer = 50.75f;
        String bodyTransfer = String.format(Locale.US, """
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %f
                }
                """, id2, id1, transfer);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(bodyTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString("Unauthorized access to account"));


    }

    @Test
    public void userCanNotMakeTransferFromOtherOwnAccountTest() {

//        Создать пользователя1
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2051",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера1
        String userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2051",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт пользователя 1 (счет)
        Response response = given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        int id1 = response.path("id");
        System.out.println("ID = " + id1);

        // вносим депозит на аккаунт1
        float deposit1 = 500;

        String body = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id1, deposit1);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

//        Создать пользователя2
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "petr2051",
                          "password": "Petr2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера2
        String userAuthHeader2 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "petr2051",
                          "password": "Petr2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт2 (счет)
        Response response2 = given()
                .header("Authorization", userAuthHeader2)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        int id2 = response2.path("id");


        System.out.println("ID = " + id2);
//переводим деньги
        float transfer = 250;
        String bodyTransfer = String.format(Locale.US, """
                {
                  "senderAccountId": %d,
                  "receiverAccountId": %d,
                  "amount": %f
                }
                """, id2, id1, transfer);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(bodyTransfer)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString("Unauthorized access to account"));


    }


}

