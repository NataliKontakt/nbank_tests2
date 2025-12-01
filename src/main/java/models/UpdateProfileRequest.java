package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest extends BaseModel{
    private String name;
}
/*
* {
  "customer": {
    "id": 2,
    "username": "kate007",
    "password": "$2a$10$R2yzuinbMT1XEJdhmhZY/uvfedUhZWzPlu/KAMlTz9F8u3hVnTG/q",
    "name": "John Smith",
    "role": "USER",
    "accounts": [
      {
        "id": 1,
        "accountNumber": "ACC1",
        "balance": 100.5,
        "transactions": [
          {
            "id": 1,
            "amount": 100.5,
            "type": "DEPOSIT",
            "timestamp": "Sat Nov 29 07:38:14 UTC 2025",
            "relatedAccountId": 1
          }
        ]
      }
    ]
  },
  "message": "Profile updated successfully"
}
* */