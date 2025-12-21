package iteration1;

import models.CreateUserRequest;
import models.LoginRequest;
import models.LoginResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpec;
import specs.ResponseSpec;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginRequest userAdmin = LoginRequest.builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudRequester<LoginResponse>(
                RequestSpec.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpec.requestReturnsOk())
                .post(userAdmin);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        // создание объекта пользователя
        CreateUserRequest user1 = AdminSteps.createUser();

        // создание объекта для логирования
        LoginRequest userLogin = LoginRequest.builder()
                .username(user1.getUsername())
                .password(user1.getPassword())
                .build();

        // получаем токен юзера
        new CrudRequester(
                RequestSpec.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpec.requestReturnsOk())
                .post(userLogin)
                .header("Authorization", Matchers.notNullValue());

    }
}
