package iteration1.ui;

import api.configs.Config;
import api.models.CreateUserRequest;
import api.specs.RequestSpec;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import common.extensions.*;
import iteration1.api.BaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;

import static com.codeborne.selenide.Selenide.executeJavaScript;
@ExtendWith(BrowserMatchExtension.class)
@ExtendWith(PlatformMatchExtension.class)
@ExtendWith(AdminSessionExtension.class)
@ExtendWith(UserSessionExtension.class)
@ExtendWith(PreparedAccountExtension.class)
@Tag("ui")
public class BaseUiTest extends BaseTest {

    @BeforeAll
    public static void setupSelenoid() {

        Configuration.remote = Config.getProperty("uiRemote");
        Configuration.baseUrl = Config.getProperty("uiBaseUrl");
        Configuration.browser = Config.getProperty("browser");
        Configuration.browserSize = Config.getProperty("browserSize");
        String platform = Config.getProperty("platform");
        System.setProperty("platform", platform);
        Configuration.headless = true;
        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

    public void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpec.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }

    @AfterEach
    public void tearDown() {
        // Принудительно закрываем браузер и очищаем
        Selenide.closeWebDriver();

        // Дополнительно: убиваем процессы Chrome
        killChromeProcesses();
    }

    private void killChromeProcesses() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                Runtime.getRuntime().exec("taskkill /F /IM chrome.exe");
                Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe");
            } else {
                Runtime.getRuntime().exec("pkill -f chrome");
                Runtime.getRuntime().exec("pkill -f chromedriver");
            }
            Thread.sleep(1000); // Даем время на завершение
        } catch (Exception e) {
            // Игнорируем ошибки при убийстве процессов
        }
        }


}
