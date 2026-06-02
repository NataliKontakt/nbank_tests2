package api.models;

import lombok.*;

import java.util.List;



@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account extends BaseModel {
    private long id;
    private String accountNumber;
    private float balance;
    private List<Transactions> transactions;
}
