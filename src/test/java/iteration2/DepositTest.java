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
import models.CustomerProfileResponse;
import models.DepositRequest;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositRequester;
import requests.UpdateCustomerProfileRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static models.UserRole.USER;
import static org.hamcrest.Matchers.equalTo;

public class DepositTest extends BaseTest {


    String userAuthHeader;

    @Test
    public void userCanMakeDepositTest() {

        //создание объекта пользователя
        CreateUserRequest user1 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user1);


        // создаем аккаунт(счет)
        new CreateAccountRequester(RequestSpec.authSpec(user1.getUsername(),user1.getPassword()),
                ResponseSpec.entityWasCreatad())
                .post(null);

        //через гет получаем номер аккаунта
        CustomerAccountsResponse customerProfile = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();


        long id = customerProfile.getAccounts().get(0).getId();
        float balance = customerProfile.getAccounts().get(0).getBalance();

        // вносим депозит
        float deposit = 50;
        float expectedBalance = balance + deposit;
        //Создаем объект депозита
        DepositRequest accountDeposite = DepositRequest.builder()
                .id(id)
                .balance(deposit)
                .build();

        new DepositRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .post(accountDeposite);

        //через гет получаем новый баланс и сверяем с ожидаемым

        CustomerAccountsResponse customerProfileNew = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getAccounts();

        softly.assertThat(expectedBalance).isEqualTo(customerProfileNew.getAccounts().get(0).getBalance());

    }

    //проверяем сложение не нулевого баланса с депозитом и граничное значение 5000
    @Test
    public void userCanMakeDepositNotZeroBalanceTest() {
//        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2070",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера
        userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2070",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет)
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

        int id = response.path("id");
        float balance = response.path("balance");

        System.out.println("ID = " + id);
        System.out.println("Balance = " + balance);

        // вносим депозит
        float deposit = 50;

        String body = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id, deposit);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", equalTo(balance + deposit));
        // вносим депозит еще
        float deposit2 = 5000;
        String body2 = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id, deposit2);
        float expectedBalance = balance + deposit + deposit2;
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body2)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", equalTo(expectedBalance));

        //проверяем сохранившийся баланс
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("find { it.id == " + id + " }.balance", equalTo(expectedBalance));

    }

    @Test
    public void depositCanNotBeNegativeTest() {
        //        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2016",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера
        userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2016",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет)
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

        int id = response.path("id");
        float balance = response.path("balance");

        System.out.println("ID = " + id);
        System.out.println("Balance = " + balance);

        // вносим депозит
        float deposit = -1;

        String body = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id, deposit);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Deposit amount must be at least 0.01"));
    }

    @Test
    public void depositCanNotBeMore5000Test() {
        //        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2030",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера
        userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2030",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет)
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

        int id = response.path("id");
        float balance = response.path("balance");

        System.out.println("ID = " + id);
        System.out.println("Balance = " + balance);

        // вносим депозит
        float deposit = 5001;

        String body = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id, deposit);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(Matchers.containsString("Deposit amount cannot exceed 5000"));
    }

    @Test
    public void depositCanNotBeOnNotExistAccount() {
        //        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2023",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера
        userAuthHeader = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2023",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет)
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);

        // несуществующий id
        int id = 100500;
        // вносим депозит
        float deposit = 500;

        String body = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id, deposit);
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString("Unauthorized access to account"));
    }

    @Test
    public void depositToSomeoneAccountIsNotPossibleTest() {

        //        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2029",
                          "password": "Kate2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен юзера
        String userAuthHeader1 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "kate2029",
                          "password": "Kate2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");

        // создаем аккаунт(счет)
        Response response = given()
                .header("Authorization", userAuthHeader1)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("http://localhost:4111/api/v1/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .response();

        int id = response.path("id");
        float balance = response.path("balance");

        System.out.println("ID = " + id);
        System.out.println("Balance = " + balance);

        //        Создать второго пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "petr2024",
                          "password": "Petr2000#",
                          "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED);
        // получаем токен второго пользователя
        String userAuthHeader2 = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "petr2024",
                          "password": "Petr2000#"
                        }
                        """)
                .post("http://localhost:4111/api/v1/auth/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header("Authorization");


        // вносим депозит
        float deposit = 500;

        String body = String.format(Locale.US, """
                {
                  "id": %d,
                  "balance": %f
                }
                """, id, deposit);
        given()
                .header("Authorization", userAuthHeader2)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body(Matchers.containsString("Unauthorized access to account"));
    }

}
