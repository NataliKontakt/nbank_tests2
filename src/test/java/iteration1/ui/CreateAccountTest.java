package iteration1.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).createNewAccount()
                .checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED,
                        new UserSteps(user.getUsername(), user.getPassword())
                                .getAllAccounts().getFirst().getAccountNumber());

        List<CreateAccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();

        assertThat(createdAccounts).hasSize(1);

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}
