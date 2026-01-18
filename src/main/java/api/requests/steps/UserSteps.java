package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.*;

import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

import java.util.List;
import java.util.Map;

import static io.qameta.allure.Allure.step;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
      this.username = username;
        this.password = password;
    }


    public CreateAccountResponse createAccount(){

        return step("Пользователь создает аккаунт", () -> {
            return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpec.authSpec(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);
        });
    }

    public CustomerAccountsResponse getAccount(){
        return new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();
    }

    public DepositResponse makeDeposit(long id, float deposit){
        return step("Пользователь пополняет депозит", () -> {
        return new ValidatedCrudRequester<DepositResponse>(RequestSpec.authSpec(username, password),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(
                        DepositRequest.class,
                        Map.of("id", id, "balance", deposit)));
        });
    }

    public List<Account> getAllAccounts() {
        return new ValidatedCrudRequester<Account>(
                RequestSpec.authSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .getAll(Account[].class);
    }

    public  List<CreateAccountResponse> getAllCreatedAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpec.authSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk()).getAll(CreateAccountResponse[].class);
    }

    public Account getAccountByNumber(String accountNumber) {
        return getAllAccounts().stream()
                .filter(account -> accountNumber.equals(account.getAccountNumber()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Account %s not found for user %s", accountNumber, username)
                ));
    }

    public CustomerProfileResponse getCustomerProfile(){
        return new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(username, password),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();
    }

}
