package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    DEPOSIT_SUCCESSFULLY("✅ Successfully deposited $%.2f to account %s!");
    private final String message;

    BankAlert(String message) {
        this.message = message;
    }



}
/*
 *
 * */