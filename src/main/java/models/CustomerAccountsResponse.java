package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountsResponse extends BaseModel{
    private List<Account> accounts;

    // Метод для десериализации массива
    public static CustomerAccountsResponse fromArray(List<Account> accountList) {
        return new CustomerAccountsResponse(accountList);
    }
}
/*
* [
  {
    "id": 1,
    "accountNumber": "ACC1",
    "balance": 0,
    "transactions": []
  }
]*/