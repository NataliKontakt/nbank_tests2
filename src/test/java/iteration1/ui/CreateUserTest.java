package iteration1.ui;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AdminSteps;
import common.annotations.AdminSession;
import common.annotations.Browsers;
import common.annotations.Platforms;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.BankAlert;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("iteration-1")
public class CreateUserTest extends BaseUiTest {

    @Test
    @DisplayName("Админ может создать пользователя с валидными данными")
    @AdminSession
    @Platforms({"web"})
    @Browsers({"chrome"})
    public void adminCanCreateUserTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);

        step("Проверка, что пользователь создан на UI", () -> {
            new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                    .checkAlertMessageAndAccept(BankAlert.USER_CREATED_SUCCESSFULLY.getMessage());
        });
        step("Проверка, что пользователь создан на API", () -> {
            CreateUserResponse createdUser = AdminSteps.getAllUsers().stream()
                    .filter(user -> user.getUsername().equals(newUser.getUsername()))
                    .findFirst().get();
            ModelAssertions.assertThatModels(newUser, createdUser).match();

        });
    }

    @Test
    @DisplayName("Админ не может создать пользователя с не валидными данными")
    @AdminSession
    @Platforms({"web"})
    @Browsers({"chrome"})
    public void adminCannotCreateUserWithInvalidDataTest() {
        CreateUserRequest newUser = RandomModelGenerator.generate(CreateUserRequest.class);
        newUser.setUsername(RandomStringUtils.randomAlphabetic(1));
        step("Проверка, что пользователь не создан на UI", () -> {
            assertTrue(new AdminPanel().open().createUser(newUser.getUsername(), newUser.getPassword())
                    .checkAlertMessageAndAccept(BankAlert.USERNAME_MUST_BE_BETWEEN_3_AND_15_HARACTERS.getMessage())
                    .getAllUsers().stream().noneMatch(userBage -> userBage.getUsername().equals(newUser.getUsername())));
        });
        step("Проверка, что пользователь не создан на API", () -> {
            long usersWithSameUsernameAsNewUser = AdminSteps.getAllUsers().stream().filter(user -> user.getUsername().equals(newUser.getUsername())).count();

            assertThat(usersWithSameUsernameAsNewUser).isZero();
        });
    }
}
