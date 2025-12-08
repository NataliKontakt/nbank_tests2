package requests.skelethon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import models.*;

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

    CUSTOMER_PROFILE(
            "customer/profile",
            BaseModel.class,
            CustomerProfileResponse.class
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
