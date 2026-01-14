package iteration1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpec;
import com.codeborne.selenide.Condition;
import common.annotations.AdminSession;
import common.extensions.AdminSessionExtension;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserTest extends BaseUiTest {

   /* @Test
    @AdminSession
    public void adminCanCreateUserTest() {
        //решается аннотацией     @AdminSession
        // ШАГ 1: админ залогинился в банке
*//*        CreateUserRequest admin = CreateUserRequest.getAdmin();
        authAsUser(admin);*//*

        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);


        assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY)
                .getAllUsers().stream().anyMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        // ШАГ 5: проверка, что юзер создан на API

        CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(newUser.getUsername()))
                .findFirst().get();

        ModelAssertions.assertThatModels(newUser, createdUser).match();
    }

    @Test
    @AdminSession
    public void adminCannotCreateUserWithInvalidDataTest() {
        // ШАГ 2: админ создает юзера в банке
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername("a");

        assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_HARACTERS)
                .getAllUsers().stream().noneMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));

        // ШАГ 5: проверка, что юзер НЕ создан на API

        long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername())).count();

        assertThat(usersWithSameUsernameAsNewUser).isZero();
    }*/
}
