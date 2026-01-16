package iteration2.ui;

import api.generators.RandomData;
import api.models.Account;
import api.models.CreateAccountResponse;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.TransferPage;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferTest extends BaseUiTest {
    float zeroBalance = 0;

    @Test
    @UserSession
    public void userCanMakeTransferToYourOwnAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();

        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps().createAccount();
        String accountNumber2 = account2.getAccountNumber();
        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ✅ Successfully transferred $%s to account %s!

        float transfer = deposit1 - 1;
        float expectedBalance1 = deposit1 - transfer;
        String recipientName = RandomData.getName();

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

    @Test
    @UserSession(2)
    public void userCanMakeTransferToAnotherUserAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает 2 юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: первый юзер создает аккаунт и пополняет его
        // ШАГ 5: второй юзер создает аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps(1).createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps(2).createAccount();
        String accountNumber2 = account2.getAccountNumber();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ✅ Successfully transferred $%s to account %s!

        float transfer = deposit1 - 1;
        float expectedBalance1 = deposit1 - transfer;

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), transfer, accountNumber2);
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, expectedBalance1);
        SessionStorage.switchToSession(2);
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber2, transfer);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps(1).getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps(2).getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(expectedBalance1);
        assertThat(accountResponse2.getBalance()).isEqualTo(transfer);

    }

    @Test
    @UserSession
    public void userCanMakeTransferWitEmptyName() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps().createAccount();
        String accountNumber2 = account2.getAccountNumber();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer, не заполняет имя и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ✅ Successfully transferred $%s to account %s!
        float transfer = deposit1 - 1;
        float expectedBalance1 = deposit1 - transfer;

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
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

    @Test
    @UserSession
    public void userCanNotMakeTransferAccountNotSelectedTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps().createAccount();
        String accountNumber2 = account2.getAccountNumber();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        float transfer = deposit1 - 1;

        String recipientName = RandomData.getName();

        new TransferPage().open().transferBuilder()
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    public void userCanNotMakeTransferRecipientAccountEmptyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps().createAccount();
        String accountNumber2 = account2.getAccountNumber();


        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        float transfer = deposit1 - 1;
        String recipientName = RandomData.getName();

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    public void userCanNotMakeTransferRecipientAccountNotExistTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        String accountNotExist = "ACC100500";

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть алерт на UI ❌ No user found with this account number.
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        float transfer = deposit1 - 1;
        String recipientName = RandomData.getName();

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNotExist)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER.getMessage());
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);

    }

    @Test
    @UserSession
    public void userCanNotMakeTransferEmptyTransferSumTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps().createAccount();
        String accountNumber2 = account2.getAccountNumber();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI

        String recipientName = RandomData.getName();

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    public void userCanNotMakeTransferIfTransferSumMoreDepositTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps().createAccount();
        String accountNumber2 = account2.getAccountNumber();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Error: Invalid transfer: insufficient funds or invalid accounts
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        float transfer = deposit1 + 1;
        String recipientName = RandomData.getName();

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNTS.getMessage());
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    public void userCanNotMakeTransferIfTransferSumMore10000Test() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps().createAccount();
        String accountNumber2 = account2.getAccountNumber();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Error: Transfer amount cannot exceed 10000
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        float transfer = 10001;
        String recipientName = RandomData.getName();

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

    @Test
    @UserSession
    public void userCanNotMakeTransferCheckEmptyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateAccountResponse account1 = SessionStorage.getSteps().createAccount();
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        SessionStorage.getSteps().makeDeposit(account1.getId(), deposit1);

        CreateAccountResponse account2 = SessionStorage.getSteps().createAccount();
        String accountNumber2 = account2.getAccountNumber();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        float transfer = deposit1 - 1;
        String recipientName = RandomData.getName();

        new TransferPage().open().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .withConfirmCheck(false)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());
        // ШАГ 8: проверка, что балансы аккаунтов изменились на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber1, deposit1)
                .checkingAccountBalanceUi(accountNumber2, zeroBalance);

        // ШАГ 9: проверка, что аккаунт был пополнен на API
        Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
        Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
        assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
    }

}
