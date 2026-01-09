package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import java.util.Locale;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class DepositPage extends BasePage<DepositPage>{

    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));

    @Override
    public String url() {
        return "/deposit";
    }

    private void deposit(String accountNumber, float deposit) {
        selectAccount.click();
        if (accountNumber != null && !accountNumber.isEmpty()) {
            $(Selectors.byText(accountNumber)).click();
        }
        enterAmountInput.sendKeys(String.valueOf(deposit));
        depositButton.click();
    }

    public UserDashboard depositSuccess(String accountNumber, float deposit) {
        deposit(accountNumber, deposit);
        return page(UserDashboard.class);
    }

    public DepositPage depositUnSuccess(String accountNumber, float deposit) {
        deposit(accountNumber, deposit);
        return this;
    }

    public DepositPage depositWithoutSelectingAccount(float deposit){
        deposit(null, deposit);
        return this;
    }

    public DepositPage checkingAccountBalanceUi(String accountNumber, float deposit){

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber))
                .shouldHave(text("$" + String.format(Locale.US, "%.2f", deposit)));
        return this;
    }

}
