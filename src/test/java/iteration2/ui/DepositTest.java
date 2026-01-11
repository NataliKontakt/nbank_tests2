package iteration2.ui;

import api.generators.RandomData;
import api.models.Account;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import common.annotations.UserSession;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositTest extends BaseUiTest {
    float zeroBalance = 0;

    @Test
    @UserSession
    public void userCanDepositAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = account.getAccountNumber();

        authAsUser(user);
        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        // ШАГ 6: проверка, что есть аллерт на UI
        float deposit = RandomData.getDeposit();
        new DepositPage().open().depositSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULLY, deposit, accountNumber);
        // ШАГ 7: проверка, что аккаунт пополнен на UI
        new TransferPage().open()
                .checkingAccountBalanceUi(deposit);

        // ШАГ 8: проверка, что аккаунт был пополнен на API
        Account accountResponse = UserSteps.getAccountByNumber(user.getUsername(), user.getPassword(), accountNumber);

        assertThat(accountResponse.getBalance()).isEqualTo(deposit);

    }

    @Test
    public void userCanNotDepositAccountTestWithoutSelectingAccount() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = account.getAccountNumber();

        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        // ШАГ 6: проверка, что ошибка ❌ Please select an account.
        float deposit = RandomData.getDeposit();
        new DepositPage().open().depositWithoutSelectingAccount(deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_SELECT_AN_ACCOUNT);
        // ШАГ 7: проверка, что аккаунт пополнен на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        // ШАГ 7: проверка, что баланс аккаунта равен нулю на API
        Account accountResponse = UserSteps.getAccountByNumber(user.getUsername(), user.getPassword(), accountNumber);

        assertThat(accountResponse.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositAccountTestMore5000() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = account.getAccountNumber();
        authAsUser(user);
        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        // ШАГ 6: проверка, что ошибка ❌ Please deposit less or equal to 5000$.
        float deposit = RandomData.getDeposit() + 5000;
        new DepositPage().open().depositUnSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000);
        // ШАГ 7: проверка, что аккаунт пополнен на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        // ШАГ 8: проверка, что баланс аккаунта равен нулю на API
        Account accountResponse = UserSteps.getAccountByNumber(user.getUsername(), user.getPassword(), accountNumber);

        assertThat(accountResponse.getBalance()).isZero();
    }

    @Test
    public void userCanNotDepositAccountTestLessOneCent() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber = account.getAccountNumber();

        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 5: юзер нажимает 💰 Deposit Money
        // ШАГ 6: проверка, что ошибка ❌ Please enter a valid amount.
        float deposit = RandomData.getDeposit() - 5000;
        new DepositPage().open().depositUnSuccess(accountNumber, deposit)
                .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_AMOUNT);
        // ШАГ 7: проверка, что аккаунт пополнен на UI
        new DepositPage().open()
                .checkingAccountBalanceUi(accountNumber, zeroBalance);

        // ШАГ 8: проверка, что баланс аккаунта равен нулю на API
        Account accountResponse = UserSteps.getAccountByNumber(user.getUsername(), user.getPassword(), accountNumber);

        assertThat(accountResponse.getBalance()).isZero();
    }
}
