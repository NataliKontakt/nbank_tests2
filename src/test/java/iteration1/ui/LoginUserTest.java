package iteration1.ui;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import com.codeborne.selenide.Condition;
import common.annotations.Browsers;
import common.annotations.Platforms;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.AdminPanel;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import static io.qameta.allure.Allure.step;

@Tag("iteration-1")
public class LoginUserTest extends BaseUiTest {

    @Test
    @DisplayName("Админ может авторизоваться с валидными данными")
    @Browsers({"chrome"})
    @Platforms({"web"})
    public void adminCanLoginWithCorrectDataTest() {
        CreateUserRequest admin = step("Создание пользователя с ролью Admin", () -> {
            return CreateUserRequest.getAdmin();
        });

        step("Вход пользователя с ролью Админ и Проверка открытия панели админа на UI", () -> {
            new LoginPage().open().login(admin.getUsername(), admin.getPassword())
                    .getPage(AdminPanel.class).getAdminPanelText().should(Condition.visible);
        });

    }

    @Test
    @DisplayName("User может авторизоваться с валидными данными")
    @Browsers({"chrome"})
    @Platforms({"web"})
    public void userCanLoginWithCorrectDataTest() {
        CreateUserRequest user = step("Создание пользователя с ролью User", () -> {
            return AdminSteps.createUser();
        });
        step("Вход пользователя с ролью User и Проверка открытия UserDashboard на UI", () -> {
            new LoginPage().open().login(user.getUsername(), user.getPassword())
                    .getPage(UserDashboard.class).getWelcomeText()
                    .should(Condition.visible).shouldHave(Condition.text(LoginPage.getWelcomeText()));
        });

    }
}

