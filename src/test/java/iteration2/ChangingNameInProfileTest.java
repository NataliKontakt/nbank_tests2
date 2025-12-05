package iteration2;

import generators.RandomData;
import iteration1.BaseTest;
import models.CreateUserRequest;
import models.CustomerProfileResponse;
import models.UpdateProfileRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.UpdateCustomerProfileRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.stream.Stream;
import static models.UserRole.USER;


public class ChangingNameInProfileTest extends BaseTest {

    @Test
    public void userCanChangeNameInProfileTest() {
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

        //Изменяем имя
        String expectedName = RandomData.getName();
        new UpdateCustomerProfileRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .put(UpdateProfileRequest.builder().name(expectedName).build());

        //Проверяем, что новое имя сохранилось
        CustomerProfileResponse customerProfile = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getProfile();
        softly.assertThat(expectedName).isEqualTo(customerProfile.getName());
    }

    public static Stream<Arguments> userInvalidName() {
        return Stream.of(
                // name field validation
                Arguments.of("   "),
                Arguments.of("ab"),
                Arguments.of("Андрей Рублев"),
                Arguments.of("abc abc abc"),
                Arguments.of("ab2 ab2"),
                Arguments.of("abc% abc%")
        );

    }

    @MethodSource("userInvalidName")
    @ParameterizedTest
    public void nameInProfileMustContainTwoWordsTest(String name) {
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

        //Изменяем имя
        String errorValue = "Name must contain two words with letters only";
        new UpdateCustomerProfileRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsBadRequest(errorValue))
                .put(UpdateProfileRequest.builder().name(name).build());

        //Проверяем, что новое имя сохранилось
        CustomerProfileResponse customerProfile = new UpdateCustomerProfileRequester(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                ResponseSpec.requestReturnsOk())
                .getProfile();

        softly.assertThat(customerProfile.getName()).isNull();

    }
}
