package iteration1;

import generators.RandomData;
import io.restassured.http.ContentType;
import models.CreateUserRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import static io.restassured.RestAssured.given;
import static models.UserRole.USER;
import static org.hamcrest.Matchers.hasItem;

public class CreateAccountTest extends BaseTest{

    @Test
    public void userCanCreateAccountTest() {
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


        // запросить все аккаунты пользователя и проверить, что наш аккаунт там
        given()
                .header("Authorization", userAuthHeader)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("accountNumber", hasItem(account));
    }
}
