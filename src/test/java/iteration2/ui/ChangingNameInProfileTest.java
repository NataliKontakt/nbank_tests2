package iteration2.ui;

import api.generators.RandomData;
import api.generators.RandomModelGenerator;
import api.models.CustomerProfileResponse;
import api.models.UpdateProfileRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import iteration1.ui.BaseUiTest;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.EditProfilePage;
import ui.pages.UserDashboard;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangingNameInProfileTest extends BaseUiTest {

    @Test
    @UserSession
    public void userCanChangeNameInProfileTest() throws InterruptedException {
        String name = RandomData.getName();
        //проверка, что есть аллерт на UI
        new EditProfilePage().open().changeName(name)
                .checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());

        //проверка, что имя изменилось на UI
        new UserDashboard().open().checkChangeNameUi(name);

        //проверка, что имя изменилось на API
        CustomerProfileResponse customerProfileResponse = SessionStorage.getSteps().getCustomerProfile();
        assertThat(customerProfileResponse.getName()).isEqualTo(name);

    }

    @Test
    @UserSession
    public void userCanNotChangeNameOnSameName() throws InterruptedException {
        String name = RandomData.getName();
        UpdateProfileRequest updateProfileRequest = RandomModelGenerator.generate(UpdateProfileRequest.class);
        updateProfileRequest.setName(name);

        new CrudRequester(RequestSpec.authSpec(SessionStorage.getUser().getUsername(), SessionStorage.getUser().getPassword()),
                Endpoint.CUSTOMER_PROFILE_UPDATE,
                ResponseSpec.requestReturnsOk())
                .update(updateProfileRequest);

        new EditProfilePage().open().changeName(name)
                .checkAlertMessageAndAccept(BankAlert.NEW_NAME_IS_THE_SAME_AS_THE_CURRENT_ONE.getMessage());

        new UserDashboard().open().checkChangeNameUi(name);

        CustomerProfileResponse customerProfileResponse = SessionStorage.getSteps().getCustomerProfile();
        assertThat(customerProfileResponse.getName()).isEqualTo(name);

    }

    @Test
    @UserSession
    public void userCanNotChangeNameOnEmptyNameTest() throws InterruptedException {

        new EditProfilePage().open().changeNameForEmptyName()
                .checkAlertMessageAndAccept(BankAlert.PLEASE_ENTER_A_VALID_NAME.getMessage());

        new UserDashboard().open().checkNotChangeNameUi();

        CustomerProfileResponse customerProfileResponse = SessionStorage.getSteps().getCustomerProfile();
        assertThat(customerProfileResponse.getName()).isNull();
    }

    @Test
    @UserSession
    public void userCanNotChangeNameOnInvalidNameTest() throws InterruptedException {

        String invalidName = RandomData.getName() + 1;
        new EditProfilePage().open().changeName(invalidName)
                .checkAlertMessageAndAccept(BankAlert.NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY.getMessage());

        new UserDashboard().open().checkNotChangeNameUi();

        CustomerProfileResponse customerProfileResponse = SessionStorage.getSteps().getCustomerProfile();
        assertThat(customerProfileResponse.getName()).isNull();
    }
}
