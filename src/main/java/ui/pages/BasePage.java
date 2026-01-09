package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;

import java.util.Locale;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder","Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder","Password"));
    protected SelenideElement selectAccount = $((".account-selector"));
    protected SelenideElement enterAmountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement homeButton =  $(Selectors.byText("🏠 Home"));
    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(BankAlert bankAlert, Object... params) {
        Alert alert = switchTo().alert();
        String actualMessage = alert.getText();
        String expectedPattern = bankAlert.getMessage();

        String expectedMessage = params.length > 0
                ? String.format(Locale.US, expectedPattern, params) // Форматируем всё сообщение
                : expectedPattern;

        assertThat(actualMessage).contains(expectedMessage);
        alert.accept();

        return (T) this;
    }
    public UserDashboard switchToUserDashboard() {
        homeButton.click();
        return page(UserDashboard.class);
    }

}
