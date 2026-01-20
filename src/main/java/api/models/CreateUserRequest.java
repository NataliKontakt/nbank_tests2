package api.models;

import api.configs.Config;
import api.generators.annotations.GeneratingStringRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel{
    @GeneratingStringRule(regex = "^[A-Za-z0-9]{3,15}$")
    private String username;
    @GeneratingStringRule(regex = "^[A-Z]{3}[a-z]{4}[0-9]{3}[$%&]{2}$")
    private String password;
    @GeneratingStringRule(regex = "^USER$")
    private String role;

    public static CreateUserRequest getAdmin(){
        return CreateUserRequest.builder().username(Config.getProperty("admin.username"))
                .password(Config.getProperty("admin.password")).build();
    }
}
