package models;

import generators.annotations.GeneratingStringRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest extends BaseModel{
    @GeneratingStringRule(regex = "^[A-Z][a-z]{1,15} [A-Z][a-z]{1,15}$")
    private String name;
}
