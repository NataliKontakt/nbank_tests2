package iteration2.ui;

import api.generators.RandomData;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CustomerProfileResponse;
import api.models.UpdateProfileRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

import static api.requests.steps.UserSteps.getCustomerProfile;
import static org.assertj.core.api.Assertions.assertThat;

public class ChangingNameInProfileTest extends BaseUiTest {

    @Test
    public void userCanChangeNameInProfileTest() throws InterruptedException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет свое имя
        // ШАГ 5: проверка, что есть аллерт на UI ✅ Name updated successfully!
        // ШАГ 6: проверка, что имя изменилось на UI
        String name = RandomData.getName();

        new EditProfilePage().open().changeName(name)
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY);

        // ШАГ 6: проверка, что имя изменилось на UI
        new UserDashboard().open().checkChangeNameUi(name);

        // ШАГ 7: проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = getCustomerProfile(user.getUsername(), user.getPassword());
        assertThat(customerProfileResponse.getName()).isEqualTo(name);

    }

    @Test
    public void userCanNotChangeNameOnSameName() throws InterruptedException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user);

        String name = RandomData.getName();
        UpdateProfileRequest updateProfileRequest = RandomModelGenerator.generate(UpdateProfileRequest.class);
        updateProfileRequest.setName(name);
        //Изменяем имя
        new CrudRequester(RequestSpec.authSpec(user.getUsername(), user.getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpec.requestReturnsOk())
                .update(updateProfileRequest);

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет свое имя на такое же
        // ШАГ 5: проверка, что есть аллерт на UI ⚠️ New name is the same as the current one.
        new EditProfilePage().open().changeName(name)
                .checkAlertMessageAndAccept(BankAlert.NEW_NAME_IS_THE_SAME_AS_THE_CURRENT_ONE);

        // ШАГ 6: проверка, что имя изменилось на UI
        new UserDashboard().open().checkChangeNameUi(name);

        // ШАГ 7: проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = getCustomerProfile(user.getUsername(), user.getPassword());
        assertThat(customerProfileResponse.getName()).isEqualTo(name);

    }

    @Test
    public void userCanNotChangeNameOnEmptyNameTest() throws InterruptedException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет свое имя - пустое поле
        // ШАГ 5: проверка, что есть аллерт на UI ❌ Please enter a valid name.
        new EditProfilePage().open().changeNameForEmptyName()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_NAME);

        // ШАГ 6: проверка, что имя изменилось на UI
        new UserDashboard().open().checkNotChangeNameUi();

        // ШАГ 7: проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = getCustomerProfile(user.getUsername(), user.getPassword());
        assertThat(customerProfileResponse.getName()).isNull();

    }

    @Test
    public void userCanNotChangeNameOnInvalidNameTest() throws InterruptedException {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке
        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        // ШАГИ ТЕСТА
        // ШАГ 4: юзер изменяет свое имя
        // ШАГ 5: проверка, что есть аллерт на UI "Name must contain two words with letters only"

        String invalidName = RandomData.getName() + 1;
        new EditProfilePage().open().changeName(invalidName)
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY);

        // ШАГ 6: проверка, что имя изменилось на UI
        new UserDashboard().open().checkNotChangeNameUi();

        // ШАГ 7: проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = getCustomerProfile(user.getUsername(), user.getPassword());
        assertThat(customerProfileResponse.getName()).isNull();

    }
}
