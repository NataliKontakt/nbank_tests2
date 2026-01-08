package iteration2.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.LoginPage;
import ui.pages.TransferPage;
import ui.pages.UserDashboard;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class TransferTest extends BaseUiTest {

    @Test
    public void userCanMakeTransferToYourOwnAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();

        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ✅ Successfully transferred $%s to account %s!
        float transfer = deposit1 - 1;
        String recipientName = RandomData.getName();

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY, transfer, accountNumber2);


        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1 - transfer);
        String expectedBalance2 = usdFormat.format(transfer);

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanMakeTransferToAnotherUserAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает 2 юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: первый юзер создает аккаунт и пополняет его
        // ШАГ 5: второй юзер создает аккаунт

        CreateUserRequest user1 = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user1.getUsername(), user1.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user1.getUsername(), user1.getPassword(), account1.getId(), deposit1);

        CreateUserRequest user2 = AdminSteps.createUser();
        CreateAccountResponse account2 = UserSteps.createAccount(user2.getUsername(), user2.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user1);

        new LoginPage().open().login(user1.getUsername(), user1.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ✅ Successfully transferred $%s to account %s!
        float transfer = deposit1 - 1;

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY, transfer, accountNumber2);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        // первого пользователя
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1 - transfer);
        String expectedBalance2 = usdFormat.format(transfer);

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        //второго пользователя
        authAsUser(user2);

        Selenide.open("/deposit");
        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanMakeTransferWitEmptyName() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer, не заполняет имя и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ✅ Successfully transferred $%s to account %s!
        float transfer = deposit1 - 1;

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULLY, transfer, accountNumber2);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1 - transfer);
        String expectedBalance2 = usdFormat.format(transfer);

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferAccountNotSelectedTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.
        float transfer = deposit1 - 1;

        String recipientName = RandomData.getName();

        new TransferPage().transferBuilder()
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferRecipientAccountEmptyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.
        float transfer = deposit1 - 1;
        String recipientName = RandomData.getName();

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferRecipientAccountNotExistTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        String accountNotExist = "ACC100500";

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ No user found with this account number.
        float transfer = deposit1 - 1;
        String recipientName = RandomData.getName();

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNotExist)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

    }

    @Test
    public void userCanNotMakeTransferEmptyTransferSumTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.

        String recipientName = RandomData.getName();

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferIfTransferSumMoreDepositTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Error: Invalid transfer: insufficient funds or invalid accounts
        float transfer = deposit1 + 1;
        String recipientName = RandomData.getName();

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNTS);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferIfTransferSumMore10000Test() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        float transfer = 10001;
        String recipientName = RandomData.getName();

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

    @Test
    public void userCanNotMakeTransferCheckEmptyTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        // ШАГ 4: юзер создает первый аккаунт и пополняет его
        // ШАГ 5: юзер создает второй аккаунт

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account1 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber1 = account1.getAccountNumber();
        float deposit1 = RandomData.getDeposit();
        UserSteps.makeDeposit(user.getUsername(), user.getPassword(), account1.getId(), deposit1);

        CreateAccountResponse account2 = UserSteps.createAccount(user.getUsername(), user.getPassword());
        String accountNumber2 = account2.getAccountNumber();

        authAsUser(user);

        new LoginPage().open().login(user.getUsername(), user.getPassword())
                .getPage(UserDashboard.class).switchToTransfer();

        // ШАГИ ТЕСТА
        // ШАГ 6: юзер нажимает 🔄 Make a Transfer и делает перевод
        // ШАГ 7: проверка, что есть аллерт на UI ❌ Please fill all fields and confirm.
        float transfer = deposit1 - 1;
        String recipientName = RandomData.getName();

        new TransferPage().transferBuilder()
                .accountNumber(accountNumber1)
                .recipientName(recipientName)
                .accountRecipientNumber(accountNumber2)
                .transfer(transfer)
                .withConfirmCheck(false)
                .execute()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_FILL_ALL_FIELDS_AND_CONFIRM);

        // ШАГ 7: проверка, что балансы аккаунтов изменились на UI
        $(Selectors.byText("🏠 Home")).click();
        $(Selectors.byText("💰 Deposit Money")).click();

// Проверка: ищем option, содержащий номер аккаунта, и проверяем баланс в нём
        // Формируем строку баланса в американском формате: всегда с точкой и двумя знаками после неё
        DecimalFormat usdFormat = new DecimalFormat("$#.00", DecimalFormatSymbols.getInstance(Locale.US));
        String expectedBalance1 = usdFormat.format(deposit1);
        String expectedBalance2 = "0.00";

        $("select.account-selector")
                .$$("option")                                   // все option внутри селекта
                .filterBy(text(accountNumber1))        // оставляем только тот, где есть нужный аккаунт
                .shouldHave(size(1))    // убеждаемся, что такой аккаунт найден (и только один)
                .first()                                        // берём найденный option
                .shouldBe(visible)
                .shouldHave(text(accountNumber1))
                .shouldHave(text(expectedBalance1));

        $("select.account-selector")
                .$$("option")
                .filterBy(text(accountNumber2))
                .shouldHave(size(1))
                .first()
                .shouldBe(visible)
                .shouldHave(text(accountNumber2))
                .shouldHave(text(expectedBalance2));
    }

}
