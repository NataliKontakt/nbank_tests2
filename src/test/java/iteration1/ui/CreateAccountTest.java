package iteration1.ui;

import api.models.Account;
import common.annotations.Browsers;
import common.annotations.Platforms;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.List;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("iteration-1")
public class CreateAccountTest extends BaseUiTest {

    @Test
    @DisplayName("Пользователь может создать аккаунт")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    public void userCanCreateAccountTest() {
        step("Пользователь создает аккаунт", () -> {
            new UserDashboard().open().createNewAccount();
        });

        List<Account> createdAccounts = SessionStorage.getSteps()
                .getAllAccounts();
        step("Проверка, что аккаунт создался на UI", () -> {
            new UserDashboard().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage(),
                    createdAccounts.getFirst().getAccountNumber());
        });
        step("Проверка, что аккаунт создался на API", () -> {
            assertThat(createdAccounts).hasSize(1);

            assertThat(createdAccounts.getFirst().getBalance()).isZero();
        });
    }
}
