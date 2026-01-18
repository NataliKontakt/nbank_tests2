package iteration2.ui;

import api.generators.RandomData;
import api.models.Account;
import common.annotations.Browsers;
import common.annotations.Platforms;
import common.annotations.PreparedAccount;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.TransferPage;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("iteration-2")
public class DepositTest extends BaseUiTest {
    float zeroBalance = 0;

    @Test
    @DisplayName("Пользователь может пополнить свой аккаунт")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    @PreparedAccount
    public void userCanDepositAccountTest() {
        String accountNumber = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit = RandomData.getDeposit();
        step("Пользователь пополняет аккаунт", () -> {
            new DepositPage().open().depositSuccess(accountNumber, deposit)
                    .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage(), deposit, accountNumber);
        });
        step("Проверка, что аккаунт пополнен на UI", () -> {
            new TransferPage().open()
                    .checkingAccountBalanceUi(deposit);
        });
        step("Проверка, что аккаунт пополнен на API", () -> {
            Account accountResponse = SessionStorage.getSteps().getAccountByNumber(accountNumber);
            assertThat(accountResponse.getBalance()).isEqualTo(deposit);
        });
    }

    @Test
    @DisplayName("Пользователь не может пополнить аккаунт, не выбрав его из списка")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    @PreparedAccount
    public void userCanNotDepositAccountTestWithoutSelectingAccount() {
        String accountNumber = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit = RandomData.getDeposit();
        step("Пользователь пополняет аккаунт, не выбрав его из списка", () -> {
            new DepositPage().open().depositWithoutSelectingAccount(deposit)
                    .checkAlertMessageAndAccept(BankAlert.PLEASE_SELECT_AN_ACCOUNT.getMessage());
        });
        step("Проверка, что аккаунт не пополнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber, zeroBalance);
        });
        step("Проверка, что аккаунт не пополнен на API", () -> {
            Account accountResponse = SessionStorage.getSteps().getAccountByNumber(accountNumber);
            assertThat(accountResponse.getBalance()).isZero();
        });
    }

    @Test
    @DisplayName("Пользователь не может пополнить аккаунт на сумму более 5000")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    @PreparedAccount
    public void userCanNotDepositAccountTestMore5000() {
        String accountNumber = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit = RandomData.getDeposit() + 5000;
        step("Пользователь пополняет аккаунт на сумму более 5000", () -> {
            new DepositPage().open().depositUnSuccess(accountNumber, deposit)
                    .checkAlertMessageAndAccept(BankAlert.PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000.getMessage());
        });
        step("Проверка, что аккаунт не пополнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber, zeroBalance);
        });
        step("Проверка, что аккаунт не пополнен на API", () -> {
            Account accountResponse = SessionStorage.getSteps().getAccountByNumber(accountNumber);
            assertThat(accountResponse.getBalance()).isZero();
        });
    }

    @Test
    @DisplayName("Пользователь не может пополнить аккаунт на сумму менее 0,01")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    @PreparedAccount
    public void userCanNotDepositAccountTestLessOneCent() {
        String accountNumber = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit = RandomData.getDeposit() - 5000;
        step("Пользователь пополняет аккаунт на сумму менее 0,01", () -> {
            new DepositPage().open().depositUnSuccess(accountNumber, deposit)
                    .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_AMOUNT.getMessage());
        });
        step("Проверка, что аккаунт не пополнен на UI", () -> {
            new DepositPage().open()
                    .checkingAccountBalanceUi(accountNumber, zeroBalance);
        });
        step("Проверка, что аккаунт не пополнен на API", () -> {
            Account accountResponse = SessionStorage.getSteps().getAccountByNumber(accountNumber);
            assertThat(accountResponse.getBalance()).isZero();
        });
    }
}
