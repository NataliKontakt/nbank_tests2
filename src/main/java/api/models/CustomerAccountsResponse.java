package api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Builder
public class CustomerAccountsResponse extends BaseModel{
    private List<Account> accounts;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public CustomerAccountsResponse(List<Account> accounts) {
        this.accounts = accounts;
    }

    // Метод для десериализации массива
    public static CustomerAccountsResponse fromArray(List<Account> accountList) {
        return new CustomerAccountsResponse(accountList);
    }
}
