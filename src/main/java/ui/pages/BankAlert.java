package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_HARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number:"),
    DEPOSIT_SUCCESSFULLY("✅ Successfully deposited $%.2f to account %s!"),
    PLEASE_SELECT_AN_ACCOUNT("❌ Please select an account."),
    PLEASE_DEPOSIT_LESS_OR_EQUAL_TO_5000("❌ Please deposit less or equal to 5000$."),
    PLEASE_ENTER_A_VALID_AMOUNT("❌ Please enter a valid amount."),
    TRANSFER_SUCCESSFULLY("✅ Successfully transferred $%s to account %s!"),
    PLEASE_FILL_ALL_FIELDS_AND_CONFIRM("❌ Please fill all fields and confirm"),
    NO_USER_FOUND_WITH_THIS_ACCOUNT_NUMBER("❌ No user found with this account number."),
    ERROR_INVALID_TRANSFER_INSUFFICIENT_FUNDS_OR_INVALID_ACCOUNTS("❌ Error: Invalid transfer: insufficient funds or invalid accounts"),
    ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NEW_NAME_IS_THE_SAME_AS_THE_CURRENT_ONE("⚠️ New name is the same as the current one."),
    PLEASE_ENTER_A_VALID_NAME("❌ Please enter a valid name."),
    NAME_MUST_CONTAIN_TWO_WORDS_WITH_LETTERS_ONLY("Name must contain two words with letters only");
    private final String message;

    BankAlert(String message) {
        this.message = message;
    }

}
