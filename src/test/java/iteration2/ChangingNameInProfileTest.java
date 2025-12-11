package iteration2;

import generators.RandomData;
import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.CreateUserRequest;
import models.CustomerProfileResponse;
import models.UpdateProfileRequest;
import models.UpdateProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.util.stream.Stream;

import static models.UserRole.USER;
import static specs.ResponseSpec.errorNameMustContainTwoWords;


public class ChangingNameInProfileTest extends BaseTest {

    @Test
    public void userCanChangeNameInProfileTest() {
        //создание объекта пользователя
        CreateUserRequest user1 = RandomModelGenerator.generate(CreateUserRequest.class);
        // создание пользователя
        new CrudRequester(RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.entityWasCreatad())
                .post(user1);

        //Изменяем имя
        String expectedName = RandomData.getName();
        new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpec.requestReturnsOk())
                .update(UpdateProfileRequest.builder().name(expectedName).build());

        //Проверяем, что новое имя сохранилось
        CustomerProfileResponse customerProfile = new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();
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
        CreateUserRequest user1 = RandomModelGenerator.generate(CreateUserRequest.class);

        // создание пользователя
        new CrudRequester(RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.entityWasCreatad())
                .post(user1);

        //Изменяем имя
        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpec.requestReturnsBadRequest(errorNameMustContainTwoWords))
                .update(UpdateProfileRequest.builder().name(name).build());

        //Проверяем, что новое имя сохранилось
        CustomerProfileResponse customerProfile = new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(customerProfile.getName()).isNull();

    }
}
