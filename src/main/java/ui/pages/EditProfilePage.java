package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

public class EditProfilePage extends BasePage<EditProfilePage>{
    private SelenideElement enterNewNameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveChangesButton =  $(Selectors.byText("💾 Save Changes"));

    @Override
    public String url() {
        return "/edit-profile";
    }

    public EditProfilePage changeName(String name) throws InterruptedException {

        Thread.sleep(1000);
        if (name != null && !name.isEmpty()) {
            enterNewNameInput.shouldBe(Condition.visible, Duration.ofSeconds(5)).val(name);
            enterNewNameInput.shouldHave(Condition.value(name), Duration.ofSeconds(5));
        }
        saveChangesButton.click();
        return this;
    }
    public EditProfilePage changeNameForEmptyName() throws InterruptedException {
        return changeName(null);
    }
}
