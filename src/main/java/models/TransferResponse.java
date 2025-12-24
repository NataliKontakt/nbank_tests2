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
public class TransferResponse extends BaseModel{
    private long id;
    private String accountNumber;
    private float balance;
    private List<Transactions> transactions;

}
