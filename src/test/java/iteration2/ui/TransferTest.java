package iteration2.ui;

import api.generators.RandomData;
import api.models.Account;
import common.annotations.PreparedAccount;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.TransferPage;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseUiTest {
    float zeroBalance = 0;
    String accountNumber1;
    float deposit1;
    String accountNumber2;
    float transfer;
    float expectedBalance1;
    String recipientName;

    @BeforeEach
    public void prepareData(TestInfo testInfo) {
        if (!testInfo.getTags().contains("Two_users")) {
            accountNumber1 = SessionStorage.getPreparedAccount(1).getAccountNumber();
            deposit1 = SessionStorage.getPreparedAccount(1).getBalance();
            accountNumber2 = SessionStorage.getPreparedAccount(2).getAccountNumber();
            transfer = deposit1 - 1;
            expectedBalance1 = deposit1 - transfer;
            recipientName = RandomData.getName();
        }

    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanMakeTransferToYourOwnAccountTest() {
        //проверка, что есть аллерт на UI ✅ Successfully transferred $%s to account %s!
        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), transfer, accountNumber2);
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, expectedBalance1)
                .checkingAccountBalanceUi(accountNumber2, transfer);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(expectedBalance1);
        assertThat(accountResponse2.getBalance()).isEqualTo(transfer);
    }

    @Tag("Two_users")
    @Test
    @UserSession(2)
    @PreparedAccount(randomDeposit = true)
    public void userCanMakeTransferToAnotherUserAccountTest() {
        String accountNumber1 = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit1 = SessionStorage.getPreparedAccount(1).getBalance();
        String accountNumber2 = SessionStorage.getPreparedAccount(2, 1).getAccountNumber();
        float transfer = deposit1 - 1;
        float expectedBalance1 = deposit1 - transfer;

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), transfer, accountNumber2);

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, expectedBalance1);
        SessionStorage.switchToSession(2);
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber2, transfer);

        Account accountResponse1 = SessionStorage.getSteps(1).getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps(2).getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(expectedBalance1);
        assertThat(accountResponse2.getBalance()).isEqualTo(transfer);

    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanMakeTransferWitEmptyName() {
        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), transfer, accountNumber2);

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, expectedBalance1)
                .checkingAccountBalanceUi(accountNumber2, transfer);

        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(expectedBalance1);
        assertThat(accountResponse2.getBalance()).isEqualTo(transfer);

    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferAccountNotSelectedTest() {
        new TransferPage().open().transferBuilder()
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferRecipientAccountEmptyTest() {
        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferRecipientAccountNotExistTest() {
        String accountNotExist = "ACC100500";

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNotExist)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1);

        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferEmptyTransferSumTest() {
        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferIfTransferSumMoreDepositTest() {
        float transfer = deposit1 + 1;

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNTS.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferIfTransferSumMore10000Test() {
        float transfer = 10001;

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferCheckEmptyTest() {
        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .withConfirmCheck(false)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

}
