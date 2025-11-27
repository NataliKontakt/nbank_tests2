package iteration2;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;

public class DepositTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()));
    }

    String userAuthHeader;

    @Test
    public void userCanMakeDepositTest() {
//        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2014",
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
                          "username": "kate2014",
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
                          "username": "kate2019",
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
                          "username": "kate2019",
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

        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body2)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", equalTo(balance + deposit + deposit2));

    }
@Test
    public void depositCanNotBeNegativeTest(){
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
    public void depositCanNotBeMore5000Test(){
        //        Создать пользователя
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                          "username": "kate2018",
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
                          "username": "kate2018",
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
        float deposit = 50001;

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

}
