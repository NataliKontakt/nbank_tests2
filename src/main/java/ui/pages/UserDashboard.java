package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

@Getter
public class UserDashboard extends BasePage<UserDashboard>{
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement depositMoneyButton = $(Selectors.byText("💰 Deposit Money"));
    private SelenideElement transferMoneyButton = $(Selectors.byText("🔄 Make a Transfer"));
    private SelenideElement userInfo = $(Selectors.byText("@")).shouldBe(Condition.visible);
    @Override
    public String url() {
        return "/dashboard";
    }

    public DepositPage switchToDeposit(){
        depositMoneyButton.click();
        return page(DepositPage.class);
    }

    public TransferPage switchToTransfer(){
        transferMoneyButton.click();
        return page(TransferPage.class);
    }

    public EditProfilePage switchToEditProfile() {
        userInfo.click();
        return page(EditProfilePage.class);
    }

}
