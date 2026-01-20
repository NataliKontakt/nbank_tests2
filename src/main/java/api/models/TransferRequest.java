package api.models;

import api.generators.annotations.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest extends BaseModel{
    @Optional
    private long senderAccountId;
    @Optional
    private long receiverAccountId;
    @Optional
    private float amount;
}
