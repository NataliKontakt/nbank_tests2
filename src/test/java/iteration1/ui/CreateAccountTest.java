package iteration1.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(AdminSteps.createUser());

        new UserDashboard().createNewAccount()
                .checkAlertMessageAndAccept
                        (BankAlert.NEW_ACCOUNT_CREATED,
                                new UserSteps(user.getUsername(), user.getPassword())
                                        .getAllCreatedAccounts().getFirst().getAccountNumber());

        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllCreatedAccounts();

        assertThat(createdAccounts).hasSize(1);

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}
