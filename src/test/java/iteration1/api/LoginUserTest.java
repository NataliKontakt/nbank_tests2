package iteration1.api;

import api.models.CreateUserRequest;
import api.models.LoginRequest;
import api.models.LoginResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

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
