package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpec;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Arrays;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseUiTest {
    float zeroBalance = 0;
    @Test
    public void userCanDepositAccountTest() throws AccountNotFoundException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = account.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToDeposit();

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        // ШАГ 6: проверка, что есть аллерт на UI
        // ШАГ 7: проверка, что аккаунт пополнен на UI
        float deposit = RandomData.getDeposit();
        new DepositPage().depositSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY, deposit, accountNumber)
                .switchToTransfer()
                .checkingAccountBalanceUi(deposit);

        // ШАГ 8: проверка, что аккаунт был пополнен на API
        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpec.authSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountResponse = Arrays.stream(existingUserAccounts).filter(
                        accounts -> accounts.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Счет не найден: " + accountNumber));

        assertThat(accountResponse.getBalance()).isEqualTo(deposit);

    }

    @Test
    public void userCanNotDepositAccountTestWithoutSelectingAccount() throws AccountNotFoundException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = account.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToDeposit();

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        // ШАГ 6: проверка, что ошибка ❌ Please select an account.
        // ШАГ 7: проверка, что аккаунт не был пополнен на UI
        float deposit = RandomData.getDeposit();
        new DepositPage().depositWithoutSelectingAccount(deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_SELECT_AN_ACCOUNT)
                .switchToUserDashboard()
                .switchToDeposit()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        // ШАГ 7: проверка, что баланс аккаунта равен нулю на API
        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpec.authSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountResponse = Arrays.stream(existingUserAccounts).filter(
                        accounts -> accounts.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Счет не найден: " + accountNumber));

        assertThat(accountResponse.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositAccountTestMore5000() throws AccountNotFoundException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = account.getAccountNumber();

        authAsUser(user);
        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToDeposit();

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        // ШАГ 6: проверка, что ошибка ❌ Please deposit less or equal to 5000$.
        // ШАГ 7: проверка, что аккаунт не был пополнен на UI
        float deposit = RandomData.getDeposit() + 5000;
        new DepositPage().depositUnSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000)
                .switchToUserDashboard()
                .switchToDeposit()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        // ШАГ 7: проверка, что баланс аккаунта равен нулю на API
        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpec.authSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountResponse = Arrays.stream(existingUserAccounts).filter(
                        accounts -> accounts.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Счет не найден: " + accountNumber));

        assertThat(accountResponse.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositAccountTestLessOneCent() throws AccountNotFoundException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = account.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToDeposit();

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        // ШАГ 6: проверка, что ошибка ❌ Please enter a valid amount.
        // ШАГ 7: проверка, что аккаунт не был пополнен на UI
        float deposit = RandomData.getDeposit() - 5000;
        new DepositPage().depositUnSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_AMOUNT)
                .switchToUserDashboard()
                .switchToDeposit()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        // ШАГ 7: проверка, что баланс аккаунта равен нулю на API
        CreateAccountResponse[] existingUserAccounts = given()
                .spec(RequestSpec.authSpec(user.getUsername(), user.getPassword()))
                .get("http://localhost:4111/api/v1/customer/accounts")
                .then().assertThat()
                .extract().as(CreateAccountResponse[].class);

        CreateAccountResponse accountResponse = Arrays.stream(existingUserAccounts).filter(
                        accounts -> accounts.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Счет не найден: " + accountNumber));

        assertThat(accountResponse.getBalance()).isZero();
    }
}
