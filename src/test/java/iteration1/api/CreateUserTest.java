package iteration1.api;

import api.dao.CountDao;
import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

import java.util.List;
import java.util.stream.Stream;

import static api.specs.ResponseSpec.*;
@Tag("api")
@Tag("iteration-1")
public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectData() {

        //создание объекта пользователя
        CreateUserRequest user1 = RandomModelGenerator.generate(CreateUserRequest.class);

        // создание пользователя
        CreateUserResponse actualUser = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.entityWasCreatad())
                .post(user1);

        ModelAssertions.assertThatModels(user1,actualUser).match();


        UserDao userDao = DataBaseSteps.getUserByUsername(user1.getUsername());
        DaoAndModelAssertions.assertThat(actualUser, userDao).match();
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of(" ", "Password23#", "USER", "username",
                        List.of(
                                errorUsernameMustContain,
                                errorUsernameMustBeLength,
                                errorUsernameCanNotBeBlank
                        )),
                Arguments.of("ad", "Password23#", "USER", "username", List.of(
                        errorUsernameMustBeLength
                )),
                Arguments.of("ad1!", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1@", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1$", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1%", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1^", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1&", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1*", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1(", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1)", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1=", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                )),
                Arguments.of("ad1+", "Password23#", "USER", "username", List.of(
                        errorUsernameMustContain
                ))

        );

    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidData(String username, String password, String role, String errorKey, List<String> errorValue) {
        CountDao userRowsExpected = DataBaseSteps.countRowsOfTable(DataBaseSteps.Table.CUSTOMERS);
        //создание объекта пользователя
        CreateUserRequest user1 = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        // создание пользователя
        new CrudRequester(
                RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.requestReturnsBadRequest(errorKey, errorValue))
                .post(user1);

        CountDao userRowsActual = DataBaseSteps.countRowsOfTable(DataBaseSteps.Table.CUSTOMERS);
        softly.assertThat(userRowsActual).isEqualTo(userRowsExpected);
    }
}