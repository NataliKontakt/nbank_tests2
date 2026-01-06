package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel>{
    private SelenideElement adminPanelText = $(Selectors.byText("Admin Panel"));
    private SelenideElement addUserButton = $(Selectors.byText("Add User"));
    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password){
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserButton.click();
        return  this;
    }

    /*
    * $(Selectors.byAttribute("placeholder","Username")).sendKeys(newUser.getUsername());
        $(Selectors.byAttribute("placeholder","Password")).sendKeys(newUser.getPassword());
        // ШАГ 3: проверка, что алерт "✅ User created successfully!"
        $(Selectors.byText("Add User")).click();
    * */
}
