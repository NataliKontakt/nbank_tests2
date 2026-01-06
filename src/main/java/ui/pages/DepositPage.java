package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class DepositPage extends BasePage<DepositPage>{
    private SelenideElement selectAccount = $((".account-selector"));
    private SelenideElement enterAmountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));
    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage deposit(String accountNumber, float deposit){
        selectAccount.click();
        $(Selectors.byText(accountNumber)).click();
        enterAmountInput.sendKeys(String.valueOf(deposit));
        depositButton.click();
        return this;
    }


}
