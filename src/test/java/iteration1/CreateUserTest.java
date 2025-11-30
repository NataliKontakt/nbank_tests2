package iteration1;

import generators.RandomData;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import models.CreateUserRequest;
import models.CreateUserResponse;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static models.UserRole.USER;

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
                                "Username must contain only letters, digits, dashes, underscores, and dots",
                                "Username must be between 3 and 15 characters",
                                "Username cannot be blank"
                        )),
                Arguments.of("ad", "Password23#", "USER", "username", List.of(
                        "Username must be between 3 and 15 characters"
                )),
                Arguments.of("ad1!", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1@", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1$", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1%", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1^", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1&", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1*", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1(", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1)", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1=", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
                )),
                Arguments.of("ad1+", "Password23#", "USER", "username", List.of(
                        "Username must contain only letters, digits, dashes, underscores, and dots"
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