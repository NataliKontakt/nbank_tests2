package iteration2.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.LoginRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import requests.steps.UserSteps;
import specs.RequestSpec;
import specs.ResponseSpec;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Arrays;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest {
    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub";
        Configuration.baseUrl = "http://192.168.0.249:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

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

        String userAuthHeader = new CrudRequester(
                RequestSpec.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpec.requestReturnsOk())
                .post(LoginRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        float deposit = RandomData.getDeposit();
        $(Selectors.byText("💰 Deposit Money")).click();
        $((".account-selector")).click();
        $(Selectors.byText(accountNumber)).click();
        $(Selectors.byAttribute("placeholder","Enter amount")).sendKeys(String.valueOf(deposit));
        $(Selectors.byText("💵 Deposit")).click();

        // ШАГ 6: проверка, что аккаунт пополнен на UI

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = String.format(
                "✅ Successfully deposited $%s to account %s!",
                deposit,
                accountNumber
        );
        assertThat(alertText).contains(expectedMessage);

        alert.accept();
        // ШАГ 7: проверка, что аккаунт был пополнен на API
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
}
