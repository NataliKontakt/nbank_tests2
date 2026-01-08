package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Builder;

import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage>{
    private SelenideElement enterRecipientNameInput = $(Selectors.byAttribute("placeholder", "Enter recipient name"));
    private SelenideElement enterRecipientAccountNumberInput = $(Selectors.byAttribute("placeholder", "Enter recipient account number"));
    private SelenideElement confirmCheck = $("#confirmCheck");
    private SelenideElement sendTransferButton = $(Selectors.byText("🚀 Send Transfer"));

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
   /* private TransferPage transferFull(String accountNumber, String recipientName, String accountRecipientNumber, Float transfer, boolean withConfirmCheck){
        selectAccount.click();
        if (accountNumber != null && !accountNumber.isEmpty()) {
            $(Selectors.byText(accountNumber)).click();
        }
        // Заполняем имя получателя, если оно передано
        if (recipientName != null && !recipientName.isEmpty()) {
            enterRecipientNameInput.val(recipientName);
        }
        if (accountRecipientNumber != null && !accountRecipientNumber.isEmpty()) {
            enterRecipientAccountNumberInput.val(accountRecipientNumber);
        }
        String transferString = String.valueOf(transfer);
        if (transferString != null && !transferString.isEmpty()) {
            enterAmountInput.val(transferString);
        }
        if (withConfirmCheck) {
            confirmCheck.click();
        }
        sendTransferButton.click();
        return this;
    }

    public TransferPage transfer(String accountNumber, String recipientName, String accountRecipientNumber, Float transfer, boolean withConfirmCheck) {
        return transferFull(accountNumber, recipientName, accountRecipientNumber, transfer, true);
    }
    // Перегруженный метод для случая без имени получателя
    public TransferPage transferWithoutRecipientName(String accountNumber, String accountRecipientNumber, float transfer, boolean withConfirmCheck) {
        return transferFull(accountNumber, null, accountRecipientNumber, transfer, true);
    }

    public TransferPage transferWithoutSelectingAccount(String accountRecipientNumber, String recipientName, float transfer, boolean withConfirmCheck){
        return transferFull(null, recipientName , accountRecipientNumber, transfer, true);
    }

    public TransferPage transferWithoutRecipientNumber(String accountNumber, String recipientName,  float transfer, boolean withConfirmCheck){
        return transferFull(accountNumber, recipientName, null, transfer, true);
    }

    public TransferPage transferWithoutTransferSum(String accountNumber, String recipientName,  String accountRecipientNumber, boolean withConfirmCheck){
        return transferFull(accountNumber, recipientName, accountRecipientNumber, null, true);
    }

    public TransferPage transferWithoutConfirmCheck(String accountNumber, String recipientName, String accountRecipientNumber,
                                                    Float transfer, boolean withConfirmCheck) {
        return transferFull(accountNumber, recipientName, accountRecipientNumber, transfer, false);
    }
*/
}
