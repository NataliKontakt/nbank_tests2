package api.requests.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginRequest.class,
            LoginResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    DEPOSIT("accounts/deposit",
            DepositRequest.class,
            DepositResponse.class
    ),

    TRANSFER("accounts/transfer",
            TransferRequest.class,
            TransferResponse.class
    ),

    CUSTOMER_PROFILE_GET(
            "customer/profile",
            BaseModel.class,
            CustomerProfileResponse.class
    ),
    CUSTOMER_PROFILE_UPDATE(
            "customer/profile",
            BaseModel.class,
            UpdateProfileResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "customer/accounts",
            BaseModel.class,
            CustomerAccountsResponse.class
    );

    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
