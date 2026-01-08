package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositPage extends BasePage<DepositPage>{

    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    public DepositPage deposit(String accountNumber, float deposit){
        selectAccount.click();
        if (accountNumber != null && !accountNumber.isEmpty()) {
            $(Selectors.byText(accountNumber)).click();
        }
        enterAmountInput.sendKeys(String.valueOf(deposit));
        depositButton.click();
        return this;
    }

    public DepositPage depositWithoutSelectingAccount(float deposit){
        return deposit(null, deposit);
    }



}
