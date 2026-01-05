package iteration2.api;

import api.generators.RandomModelGenerator;
import iteration1.api.BaseTest;
import api.models.CreateUserRequest;
import api.models.CustomerProfileResponse;
import api.models.UpdateProfileRequest;
import api.models.UpdateProfileResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

import java.util.stream.Stream;

import static api.specs.ResponseSpec.errorNameMustContainTwoWords;


public class ChangingNameInProfileTest extends BaseTest {

    @Test
    public void userCanChangeNameInProfileTest() {
        //создание объекта пользователя
        CreateUserRequest user1 = AdminSteps.createUser();

        //Изменяем имя
        UpdateProfileResponse customerProfileRequest = new ValidatedCrudRequester<UpdateProfileResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpec.requestReturnsOk())
                .update(RandomModelGenerator.generate(UpdateProfileRequest.class));

        //Проверяем, что новое имя сохранилось
        CustomerProfileResponse customerProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();
        ModelAssertions.assertThatModels(customerProfileRequest, customerProfileResponse).match();
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
        CreateUserRequest user1 = AdminSteps.createUser();
        UpdateProfileRequest updateProfileRequest = RandomModelGenerator.generate(UpdateProfileRequest.class);
        updateProfileRequest.setName(name);
        //Изменяем имя
        new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpec.requestReturnsBadRequest(errorNameMustContainTwoWords))
                .update(updateProfileRequest);

        //Проверяем, что новое имя сохранилось
        CustomerProfileResponse customerProfile = new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();

        softly.assertThat(customerProfile.getName()).isNull();

    }
}
