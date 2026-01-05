package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class UserDashboard extends BasePage<UserDashboard>{
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    @Override
    public String url() {
        return "/dashboard";
    }
}
/*
* $(Selectors.byClassName("welcome-text")).should(Condition.visible).shouldHave(Condition.text("Welcome, noname!"));
* */