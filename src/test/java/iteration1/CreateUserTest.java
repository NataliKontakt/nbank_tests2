package iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;
import java.util.stream.Stream;

import static models.UserRole.USER;
import static specs.ResponseSpec.*;

public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectData() {

        //создание объекта пользователя
        CreateUserRequest user1 = CreateUserRequest.builder()
                .username(RandomData.getUserName())
                .password(RandomData.getUserPassword())
                .role(USER.toString())
                .build();

        // создание пользователя
        CreateUserResponse actualUser = new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.entityWasCreatad())
                .post(user1)
                        .extract()
                                .as(CreateUserResponse.class);
        softly.assertThat(user1.getUsername()).isEqualTo(actualUser.getUsername());
        softly.assertThat(user1.getPassword()).isNotEqualTo(actualUser.getPassword());
        softly.assertThat(user1.getRole()).isEqualTo(actualUser.getRole());
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

        //создание объекта пользователя
        CreateUserRequest user1 = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        // создание пользователя
        new AdminCreateUserRequester(RequestSpec.adminSpec(),
                ResponseSpec.requestReturnsBadRequest(errorKey, errorValue))
                .post(user1);
    }
}