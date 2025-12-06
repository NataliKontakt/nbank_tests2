package iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.LoginRequest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.AdminCreateUserRequester;
import requests.LoginUserRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import static models.UserRole.USER;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginRequest userAdmin = LoginRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new LoginUserRequester(RequestSpec.unauthSpec(),
                ResponseSpec.requestReturnsOk())
                .post(userAdmin);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        // создание объекта пользователя
        CreateUserRequest user1 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();
        // создание пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user1);

        // создание объекта для логирования
        LoginRequest userLogin = LoginRequest.builder()
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();

        // получаем токен юзера
        new LoginUserRequester(RequestSpec.unauthSpec(),
                ResponseSpec.requestReturnsOk())
                .post(userLogin)
                .header("Authorization", Matchers.notNullValue());

    }
}
