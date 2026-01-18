package iteration2.ui;

import api.generators.RandomData;
import api.models.Account;
import common.annotations.Browsers;
import common.annotations.Platforms;
import common.annotations.PreparedAccount;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.*;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.TransferPage;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("iteration-2")
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
    @DisplayName("Пользователь может сделать перевод на свой аккаунт")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanMakeTransferToYourOwnAccountTest() {
        step("Пользователь делает перевод", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .recipientName(recipientName)
                    .accountRecipientNumber(accountNumber2)
                    .transfer(transfer)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), transfer, accountNumber2);
        });
        step("Проверка, что перевод выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, expectedBalance1)
                    .checkingAccountBalanceUi(accountNumber2, transfer);
        });
        step("Проверка, что перевод выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(expectedBalance1);
            assertThat(accountResponse2.getBalance()).isEqualTo(transfer);
        });
    }

    @Tag("Two_users")
    @Test
    @DisplayName("Пользователь может сделать перевод на чужой аккаунт")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession(2)
    @PreparedAccount(randomDeposit = true)
    public void userCanMakeTransferToAnotherUserAccountTest() {
        String accountNumber1 = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit1 = SessionStorage.getPreparedAccount(1).getBalance();
        String accountNumber2 = SessionStorage.getPreparedAccount(2, 1).getAccountNumber();
        float transfer = deposit1 - 1;
        float expectedBalance1 = deposit1 - transfer;
        step("Пользователь делает перевод", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .accountRecipientNumber(accountNumber2)
                    .transfer(transfer)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), transfer, accountNumber2);
        });
        step("Проверка, что перевод выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, expectedBalance1);
            SessionStorage.switchToSession(2);
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber2, transfer);
        });
        step("Проверка, что перевод выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps(1).getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps(2).getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(expectedBalance1);
            assertThat(accountResponse2.getBalance()).isEqualTo(transfer);
        });
    }

    @Test
    @DisplayName("Пользователь может сделать перевод не заполняя поле Recipient Name")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanMakeTransferWitEmptyName() {
        step("Пользователь делает перевод, не заполняя поле Recipient Name", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .accountRecipientNumber(accountNumber2)
                    .transfer(transfer)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY.getMessage(), transfer, accountNumber2);
        });
        step("Проверка, что перевод выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, expectedBalance1)
                    .checkingAccountBalanceUi(accountNumber2, transfer);
        });
        step("Проверка, что перевод выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(expectedBalance1);
            assertThat(accountResponse2.getBalance()).isEqualTo(transfer);
        });
    }

    @Test
    @DisplayName("Пользователь не может сделать перевод не выбрав аккаунт")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferAccountNotSelectedTest() {
        step("Пользователь делает перевод, не выбрав аккаунт", () -> {
            new TransferPage().open().transferBuilder()
                    .recipientName(recipientName)
                    .accountRecipientNumber(accountNumber2)
                    .transfer(transfer)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());
        });
        step("Проверка, что перевод не выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, deposit1)
                    .checkingAccountBalanceUi(accountNumber2, zeroBalance);
        });
        step("Проверка, что перевод не выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
            assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
        });
    }

    @Test
    @DisplayName("Пользователь не может сделать перевод не заполнив Recipient Account Number")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferRecipientAccountEmptyTest() {
        step("Пользователь делает перевод, не заполнив Recipient Account Number", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .recipientName(recipientName)
                    .transfer(transfer)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());
        });
        step("Проверка, что перевод не выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, deposit1)
                    .checkingAccountBalanceUi(accountNumber2, zeroBalance);
        });
        step("Проверка, что перевод не выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
            assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
        });
    }

    @Test
    @DisplayName("Пользователь не может сделать перевод на не существующий аккаунт")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferRecipientAccountNotExistTest() {
        String accountNotExist = "ACC100500";
        step("Пользователь делает перевод, на не существующий аккаунт", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .recipientName(recipientName)
                    .accountRecipientNumber(accountNotExist)
                    .transfer(transfer)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER.getMessage());
        });
        step("Проверка, что перевод не выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, deposit1);
        });
        step("Проверка, что перевод не выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
        });
    }

    @Test
    @DisplayName("Пользователь не может сделать перевод не заполнив сумму перевода")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferEmptyTransferSumTest() {
        step("Пользователь делает перевод, не заполнив сумму перевода", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .recipientName(recipientName)
                    .accountRecipientNumber(accountNumber2)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());
        });
        step("Проверка, что перевод не выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, deposit1)
                    .checkingAccountBalanceUi(accountNumber2, zeroBalance);
        });
        step("Проверка, что перевод не выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
            assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
        });
    }

    @Test
    @DisplayName("Пользователь не может сделать перевод на сумму больше депозита")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferIfTransferSumMoreDepositTest() {
        float transfer = deposit1 + 1;
        step("Пользователь делает перевод на сумму больше депозита", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .recipientName(recipientName)
                    .accountRecipientNumber(accountNumber2)
                    .transfer(transfer)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNTS.getMessage());
        });
        step("Проверка, что перевод не выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, deposit1)
                    .checkingAccountBalanceUi(accountNumber2, zeroBalance);
        });
        step("Проверка, что перевод не выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
            assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
        });
    }

    @Test
    @DisplayName("Пользователь не может сделать перевод на сумму больше 10000")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferIfTransferSumMore10000Test() {
        float transfer = 10001;
        step("Пользователь делает перевод на сумму больше 10000", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .recipientName(recipientName)
                    .accountRecipientNumber(accountNumber2)
                    .transfer(transfer)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000.getMessage());
        });
        step("Проверка, что перевод не выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, deposit1)
                    .checkingAccountBalanceUi(accountNumber2, zeroBalance);
        });
        step("Проверка, что перевод не выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
            assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
        });
    }

    @Test
    @DisplayName("Пользователь не может сделать перевод без галочки Confirm details are correct")
    @Platforms({"web"})
    @Browsers({"chrome"})
    @UserSession
    @PreparedAccount(count = 2, randomDeposit = true)
    public void userCanNotMakeTransferCheckEmptyTest() {
        step("Пользователь делает перевод без галочки Confirm details are correct", () -> {
            new TransferPage().open().transferBuilder()
                    .accountNumber(accountNumber1)
                    .recipientName(recipientName)
                    .accountRecipientNumber(accountNumber2)
                    .transfer(transfer)
                    .withConfirmCheck(false)
                    .execute()
                    .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM.getMessage());
        });
        step("Проверка, что перевод не выполнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber1, deposit1)
                    .checkingAccountBalanceUi(accountNumber2, zeroBalance);
        });
        step("Проверка, что перевод не выполнен на API", () -> {
            Account accountResponse1 = SessionStorage.getSteps().getAccountByNumber(accountNumber1);
            Account accountResponse2 = SessionStorage.getSteps().getAccountByNumber(accountNumber2);
            assertThat(accountResponse1.getBalance()).isEqualTo(deposit1);
            assertThat(accountResponse2.getBalance()).isEqualTo(zeroBalance);
        });
    }

}
