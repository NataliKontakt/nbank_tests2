package models;

import generators.annotations.GeneratingDoubleRule;
import generators.annotations.GeneratingStringRule;
import generators.annotations.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositRequest extends BaseModel{
    private long id;
    //@Optional
    @GeneratingDoubleRule(min = 0.01, max = 5000.0, precision = 2)
    private float balance;
}
