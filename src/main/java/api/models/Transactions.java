package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transactions extends BaseModel{
    private long id;
    private float amount;
    private String type;
    private String timestamp;
    private long relatedAccountId;
}
