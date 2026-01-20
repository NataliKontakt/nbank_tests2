package api.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfileResponse extends BaseModel{
    private long id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<Account> accounts;
}

    /*
    *     "id": 2,
    "username": "kate007",
    "password": "$2a$10$R2yzuinbMT1XEJdhmhZY/uvfedUhZWzPlu/KAMlTz9F8u3hVnTG/q",
    "name": "John Smith",
    "role": "USER",
    "accounts": [*/

