package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

@Getter
public class UserDashboard extends BasePage<UserDashboard>{
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement depositMoney = $(Selectors.byText("💰 Deposit Money"));
    @Override
    public String url() {
        return "/dashboard";
    }

    public DepositPage switchToDeposit(){
        depositMoney.click();
        return page(DepositPage.class);
    }

}
