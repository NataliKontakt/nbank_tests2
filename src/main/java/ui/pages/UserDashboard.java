package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

@Getter
public class UserDashboard extends BasePage<UserDashboard>{
    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));
    private SelenideElement depositMoneyButton = $(Selectors.byText("💰 Deposit Money"));
    private SelenideElement transferMoneyButton = $(Selectors.byText("🔄 Make a Transfer"));
    private SelenideElement createNewAccountButton = $(Selectors.byText("➕ Create New Account"));
    //private SelenideElement userInfo = $(Selectors.byText("@")).shouldBe(Condition.visible);
    private final SelenideElement userNameText = $(Selectors.byClassName("user-name")).shouldBe(Condition.visible);
    private final SelenideElement userUserNameText = $(Selectors.byClassName("user-username")).shouldBe(Condition.visible);
    private String noName = "Noname";

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount(){
        createNewAccountButton.click();
        return  this;
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
        userUserNameText.click();
        return page(EditProfilePage.class);
    }

    public UserDashboard checkChangeNameUi(String name){
        welcomeText.should(Condition.visible, Duration.ofSeconds(10)).shouldHave(Condition.text(
                String.format("Welcome, %s!", name)));
        refresh();
        userNameText.should(Condition.visible).shouldHave(Condition.text(name));
        return this;
    }

    public UserDashboard checkNotChangeNameUi(){
        checkChangeNameUi(noName);
        return this;
    }
}
