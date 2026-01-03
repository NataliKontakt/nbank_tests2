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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest {
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
    public void userCanMakeTransferToYourOwnAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = deposit1 - 1;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));
        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что усть аллерт на UI

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = String.format(
                "✅ Successfully transferred $%s to account %s!",
                transfer,
                accountNumber2
        );
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1 - transfer);
        String expectedBalance2 = usdFormat.format(transfer);

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanMakeTransferToAnotherUserAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает 2 юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: первый юзер создает аккаунт и пополняет его
        // ШАГ 5: второй юзер создает аккаунт

        CreateUserRequest user1 = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user1.getUsername(), user1.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user1.getUsername(), user1.getPassword(), account1.getId(), deposit1);

        CreateUserRequest user2 = AdminSteps.createUser();
        CreateAccountResponse account2 = UserSteps.createAccount(user2.getUsername(), user2.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        String userAuthHeader = new CrudRequester(
                RequestSpec.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpec.requestReturnsOk())
                .post(LoginRequest.builder().username(user1.getUsername()).password(user1.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = deposit1 - 1;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));
        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что усть аллерт на UI

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = String.format(
                "✅ Successfully transferred $%s to account %s!",
                transfer,
                accountNumber2
        );
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        // первого пользователя
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1 - transfer);
        String expectedBalance2 = usdFormat.format(transfer);

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        //второго пользователя
        String userAuthHeader2 = new CrudRequester(
                RequestSpec.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpec.requestReturnsOk())
                .post(LoginRequest.builder().username(user2.getUsername()).password(user2.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader2);

        Selenide.open("/deposit");
        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanMakeTransferWitEmptyName() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer, не заполняет имя и делает перевод
        float transfer = deposit1 - 1;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();

        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));
        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что усть аллерт на UI

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = String.format(
                "✅ Successfully transferred $%s to account %s!",
                transfer,
                accountNumber2
        );
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1 - transfer);
        String expectedBalance2 = usdFormat.format(transfer);

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferAccountNotSelectedTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = deposit1 - 1;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();

        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));
        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "❌ Please fill all fields and confirm.";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferRecipientAccountEmptyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = deposit1 - 1;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());

        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));
        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "❌ Please fill all fields and confirm.";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferRecipientAccountNotExistTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        String accountNotExist = "ACC100500";

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = deposit1 - 1;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNotExist);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));
        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что есть аллерт на UI ❌ No user found with this account number.

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "❌ No user found with this account number.";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

    }

    @Test
    public void userCanNotMakeTransferEmptyTransferSumTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);

        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "❌ Please fill all fields and confirm.";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferIfTransferSumMoreDepositTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = deposit1 + 1;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));
        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что есть аллерт на UI ❌ Error: Invalid transfer: insufficient funds or invalid accounts

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "❌ Error: Invalid transfer: insufficient funds or invalid accounts";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferIfTransferSumMore10000Test() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = 10001;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));
        $("#confirmCheck").click();
        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что есть аллерт на UI ❌ Error: Transfer amount cannot exceed 10000

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "❌ Error: Transfer amount cannot exceed 10000";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferCheckEmptyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

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
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = deposit1 - 1;
        $(Selectors.byText("🔄 Make a Transfer")).click();
        $(".account-selector").click();
        $(Selectors.byText(accountNumber1)).click();
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys(RandomData.getName());
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(String.valueOf(transfer));

        $(Selectors.byText("🚀 Send Transfer")).click();


        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.

        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "❌ Please fill all fields and confirm.";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }



}
