package iteration2.ui;

import api.generators.RandomData;
import api.generators.RandomModelGenerator;
import api.models.CustomerProfileResponse;
import api.models.UpdateProfileRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;
import common.annotations.Browsers;
import common.annotations.Platforms;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("iteration-2")
public class ChangingNameInProfileTest extends BaseUiTest {

    @Test
    @DisplayName("Пользователь может изменить имя в профиле с валидными данными")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    public void userCanChangeNameInProfileTest() {
        String name = RandomData.getName();
        step("Пользователь меняет имя в профиле с валидными данными", () -> {
            new EditProfilePage().open().changeName(name)
                    .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());
        });
        step("Проверка, что имя изменилось на UI", () -> {
            new UserDashboard().open().checkChangeNameUi(name);
        });
        step("Проверка, что имя изменилось на API", () -> {
            CustomerProfileResponse customerProfileResponse = SessionStorage.getSteps().getCustomerProfile();
            assertThat(customerProfileResponse.getName()).isEqualTo(name);
        });
    }

    @Test
    @DisplayName("Пользователь не может изменить имя в профиле на то же самое")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    public void userCanNotChangeNameOnSameName() {
        String name = RandomData.getName();
        UpdateProfileRequest updateProfileRequest = RandomModelGenerator.generate(UpdateProfileRequest.class);
        updateProfileRequest.setName(name);
        step("Пользователь меняет имя в профиле с валидными данными", () -> {
            new CrudRequester(RequestSpec.authSpec(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                    Endpoint.CUSTOMER_PROFILE_UPDATE,
                    ResponseSpec.requestReturnsOk())
                    .update(updateProfileRequest);
        });
        step("Пользователь меняет имя в профиле на то же самое", () -> {
            new EditProfilePage().open().changeName(name)
                    .checkAlertMessageAndAccept(BankAlert.NEW_NAME_IS_THE_SAME_AS_THE_CURRENT_ONE.getMessage());
        });
        step("Проверка, что имя не изменилось на UI", () -> {
            new UserDashboard().open().checkChangeNameUi(name);
        });
        step("Проверка, что имя не изменилось на API", () -> {
            CustomerProfileResponse customerProfileResponse = SessionStorage.getSteps().getCustomerProfile();
            assertThat(customerProfileResponse.getName()).isEqualTo(name);
        });
    }

    @Test
    @DisplayName("Пользователь не может изменить имя в профиле не заполнив поле")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    public void userCanNotChangeNameOnEmptyNameTest() {
        step("Пользователь меняет имя в профиле не заполнив поле", () -> {
            new EditProfilePage().open().changeNameForEmptyName()
                    .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_NAME.getMessage());
        });
        step("Проверка, что имя не изменилось на UI", () -> {
            new UserDashboard().open().checkNotChangeNameUi();
        });
        step("Проверка, что имя не изменилось на API", () -> {
            CustomerProfileResponse customerProfileResponse = SessionStorage.getSteps().getCustomerProfile();
            assertThat(customerProfileResponse.getName()).isNull();
        });
    }

    @Test
    @DisplayName("Пользователь не может изменить имя в профиле с не валидными данными")
    @Browsers({"chrome"})
    @Platforms({"web"})
    @UserSession
    public void userCanNotChangeNameOnInvalidNameTest() {
        String invalidName = RandomData.getName() + 1;
        step("Пользователь меняет имя в профиле с не валидными данными", () -> {
            new EditProfilePage().open().changeName(invalidName)
                    .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage());
        });
        step("Проверка, что имя не изменилось на UI", () -> {
            new UserDashboard().open().checkNotChangeNameUi();
        });
        step("Проверка, что имя не изменилось на API", () -> {
            CustomerProfileResponse customerProfileResponse = SessionStorage.getSteps().getCustomerProfile();
            assertThat(customerProfileResponse.getName()).isNull();
        });
    }
}
