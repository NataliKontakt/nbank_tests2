package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Builder;

import java.time.Duration;
import java.util.Locale;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage>{
    private SelenideElement enterRecipientNameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement enterRecipientAccountNumberInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement confirmCheck = $("#confirmCheck");
    private SelenideElement sendTransferButton = $(Selectors.byText("🚀 Send Transfer"));
    private SelenideElement transferAgain = $(Selectors.byText("🔁 Transfer Again"));

    @Override
    public String url() {
        return "/transfer";
    }

    @Builder(builderMethodName = "transferBuilder", buildMethodName = "execute")
    public TransferPage performTransfer(
            String accountNumber,
            String recipientName,
            String accountRecipientNumber,
            Float transfer,
            Boolean withConfirmCheck
    ) {
        // Выбор счета отправителя
        if (accountNumber != null && !accountNumber.isEmpty()) {
            selectAccount.click();
            $(Selectors.byText(accountNumber)).click();
        }

        // Заполнение имени получателя
        if (recipientName != null && !recipientName.isEmpty()) {
            enterRecipientNameInput.val(recipientName);
        }

        // Заполнение счета получателя
        if (accountRecipientNumber != null && !accountRecipientNumber.isEmpty()) {
            enterRecipientAccountNumberInput.val(accountRecipientNumber);
        }

        // Заполнение суммы перевода
        if (transfer != null) {
            enterAmountInput.val(String.valueOf(transfer));
        }

        // Подтверждение перевода (по умолчанию true)
        if (withConfirmCheck == null || withConfirmCheck) {
            confirmCheck.click();
        }

        // Отправка перевода
        sendTransferButton.click();

        return this;
    }

    public TransferPage checkingAccountBalanceUi(float deposit){
        transferAgain.click();
        $("li.list-group-item.d-flex.justify-content-between span")
                .shouldBe(visible)
                .shouldHave(text("$" + String.format(Locale.US, "%.2f", deposit)), Duration.ofSeconds(15));
        return this;
    }
}
