package iteration2.ui;

import api.generators.RandomData;
import api.models.Account;
import common.annotations.Browsers;
import common.annotations.Platforms;
import common.annotations.PreparedAccount;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;
import ui.pages.TransferPage;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseUiTest {
    float zeroBalance = 0;

    @Test


    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    @PreparedAccount
    public void userCanDepositAccountTest() {
        String accountNumber = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit = RandomData.getDeposit();
        // проверка, что есть аллерт на UI
        new DepositPage().open().depositSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY.getMessage(), deposit, accountNumber);
        // проверка, что аккаунт пополнен на UI
        new TransferPage().open()
                .checkingAccountBalanceUi(deposit);
        // проверка, что аккаунт был пополнен на API
        Account accountResponse = SessionStorage.getSteps().getAccountByNumber(accountNumber);
        assertThat(accountResponse.getBalance()).isEqualTo(deposit);
    }

    @Test
    @UserSession
    @PreparedAccount
    public void userCanNotDepositAccountTestWithoutSelectingAccount() {
        String accountNumber = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit = RandomData.getDeposit();

        new DepositPage().open().depositWithoutSelectingAccount(deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_SELECT_AN_ACCOUNT.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        Account accountResponse = SessionStorage.getSteps().getAccountByNumber(accountNumber);
        assertThat(accountResponse.getBalance()).isZero();
    }

    @Test
    @UserSession
    @PreparedAccount
    public void userCanNotDepositAccountTestMore5000() {
        String accountNumber = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit = RandomData.getDeposit() + 5000;

        new DepositPage().open().depositUnSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        Account accountResponse = SessionStorage.getSteps().getAccountByNumber(accountNumber);
        assertThat(accountResponse.getBalance()).isZero();
    }

    @Test
    @UserSession
    @PreparedAccount
    public void userCanNotDepositAccountTestLessOneCent() {
        String accountNumber = SessionStorage.getPreparedAccount(1, 1).getAccountNumber();
        float deposit = RandomData.getDeposit() - 5000;

        new DepositPage().open().depositUnSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_AMOUNT.getMessage());

        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        Account accountResponse = SessionStorage.getSteps().getAccountByNumber(accountNumber);
        assertThat(accountResponse.getBalance()).isZero();
    }
}
