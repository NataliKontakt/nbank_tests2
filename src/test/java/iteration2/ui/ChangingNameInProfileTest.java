package iteration2.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CustomerProfileResponse;
import models.LoginRequest;
import models.UpdateProfileRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import requests.steps.AdminSteps;
import specs.RequestSpec;
import specs.ResponseSpec;

import java.time.Duration;
import java.util.Map;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangingNameInProfileTest {
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
    public void userCanChangeNameInProfileTest() throws InterruptedException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
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
        // ШАГ 4: юзер изменяет свое имя
        String name = RandomData.getName();
        $(".user-name").click();
        Thread.sleep(300);
        $(Selectors.byAttribute("placeholder", "Enter new name")).sendKeys(name);
        $(Selectors.byText("💾 Save Changes")).click();
        // ШАГ 5: проверка, что есть аллерт на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "✅ Name updated successfully!";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 6: проверка, что имя изменилось на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byClassName("welcome-text")).should(Condition.visible, Duration.ofSeconds(10)).shouldHave(Condition.text(
                String.format("Welcome, %s!", name)));
        refresh();
        $(".user-name").should(Condition.visible).shouldHave(Condition.text(name));
        // ШАГ 7: проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();
        assertThat(customerProfileResponse.getName()).isEqualTo(name);

    }

    @Test
    public void userCanNotChangeNameOnSameName() throws InterruptedException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        String userAuthHeader = new CrudRequester(
                RequestSpec.unauthSpec(),
                Endpoint.LOGIN,
                ResponseSpec.requestReturnsOk())
                .post(LoginRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");
        String name = RandomData.getName();
        UpdateProfileRequest updateProfileRequest = RandomModelGenerator.generate(UpdateProfileRequest.class);
        updateProfileRequest.setName(name);
        //Изменяем имя
        new CrudRequester(RequestSpec.authSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpec.requestReturnsOk())
                .update(updateProfileRequest);

        Selenide.open("/");

        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");
        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет свое имя

        $(".user-name").click();
        Thread.sleep(300);
        $(Selectors.byAttribute("placeholder", "Enter new name")).val(name);
        $(Selectors.byText("💾 Save Changes")).click();
        // ШАГ 5: проверка, что есть аллерт на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "⚠️ New name is the same as the current one.";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 6: проверка, что имя изменилось на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byClassName("welcome-text")).should(Condition.visible, Duration.ofSeconds(10)).shouldHave(Condition.text(
                String.format("Welcome, %s!", name)));
        refresh();
        $(".user-name").should(Condition.visible).shouldHave(Condition.text(name));
        // ШАГ 7: проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();
        assertThat(customerProfileResponse.getName()).isEqualTo(name);

    }

    @Test
    public void userCanNotChangeNameOnEmptyNameTest() throws InterruptedException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
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
        // ШАГ 4: юзер изменяет свое имя
        $(".user-name").click();
        Thread.sleep(300);
        $(Selectors.byAttribute("placeholder", "Enter new name")).clear();
        $(Selectors.byText("💾 Save Changes")).click();
        // ШАГ 5: проверка, что есть аллерт на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "❌ Please enter a valid name.";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 6: проверка, что имя изменилось на UI
        String noname = "noname";
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byClassName("welcome-text")).should(Condition.visible, Duration.ofSeconds(10)).shouldHave(Condition.text(
                String.format("Welcome, %s!", noname)));
        refresh();
        $(".user-name").should(Condition.visible).shouldHave(Condition.text(noname));
        // ШАГ 7: проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();
        assertThat(customerProfileResponse.getName()).isNull();

    }

    @Test
    public void userCanNotChangeNameOnInvalidNameTest() throws InterruptedException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
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
        // ШАГ 4: юзер изменяет свое имя
        String invalidName = RandomData.getName() + 1;
        $(".user-name").click();
        Thread.sleep(300);
        $(Selectors.byAttribute("placeholder", "Enter new name")).val(invalidName);
        $(Selectors.byText("💾 Save Changes")).click();
        // ШАГ 5: проверка, что есть аллерт на UI
        Alert alert = switchTo().alert();
        String alertText = alert.getText();

        String expectedMessage = "Name must contain two words with letters only";
        assertThat(alertText).contains(expectedMessage);

        alert.accept();

        // ШАГ 6: проверка, что имя изменилось на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byClassName("welcome-text")).should(Condition.visible, Duration.ofSeconds(10)).shouldHave(Condition.text(
                "Welcome, noname!"));
        refresh();
        $(".user-name").should(Condition.visible).shouldHave(Condition.text("noname"));
        // ШАГ 7: проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();
        assertThat(customerProfileResponse.getName()).isNull();

    }
}
