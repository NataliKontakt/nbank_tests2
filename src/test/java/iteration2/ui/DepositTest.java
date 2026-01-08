package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import api.specs.RequestSpec;
import com.codeborne.selenide.Selectors;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.LoginPage;
import ui.pages.UserDashboard;

import javax.security.auth.login.AccountNotFoundException;
import java.time.Duration;
import java.util.Arrays;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseUiTest {

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
        float deposit = RandomData.getDeposit();
        new DepositPage().deposit(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY, deposit, accountNumber);


        // ШАГ 7: проверка, что аккаунт пополнен на UI
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText("🔁 Transfer Again")).click();
        $("li.list-group-item.d-flex.justify-content-between span")
                .shouldBe(visible)
                .shouldHave(text("$" + deposit), Duration.ofSeconds(15));

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
        float deposit = RandomData.getDeposit();
        new DepositPage().depositWithoutSelectingAccount(deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_SELECT_AN_ACCOUNT);

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
        float deposit = RandomData.getDeposit() + 5000;
        new DepositPage().deposit(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000);

        // ШАГ 7: проверка, что аккаунт не был пополнен на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText("🔁 Transfer Again")).click();
        $$("li.list-group-item.d-flex.justify-content-between")
                .shouldHave(size(0));

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
        float deposit = RandomData.getDeposit() - 5000;
        new DepositPage().deposit(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_AMOUNT);

        // ШАГ 7: проверка, что аккаунт не был пополнен на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(Selectors.byText("🔁 Transfer Again")).click();
        $$("li.list-group-item.d-flex.justify-content-between")
                .shouldHave(size(0));

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
