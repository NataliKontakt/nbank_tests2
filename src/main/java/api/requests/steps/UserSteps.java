package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.*;

import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;
import java.util.Map;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public static CreateAccountResponse createAccount(String username, String password){
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpec.authSpec(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);
    }

    public static CustomerAccountsResponse getAccount(String username, String password){
        return new ValidatedCrudRequester<CustomerAccountsResponse>(
                RequestSpec.authSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .get();
    }

    public static DepositResponse makeDeposit(String username, String password, long id, float deposit){
        return new ValidatedCrudRequester<DepositResponse>(RequestSpec.authSpec(username, password),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(
                        DepositRequest.class,
                        Map.of("id", id, "balance", deposit)));
    }

    public static List<Account> getAllAccounts(String username, String password) {
        return new ValidatedCrudRequester<Account>(
                RequestSpec.authSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk())
                .getAll(Account[].class);
    }

    public  List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpec.authSpec(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpec.requestReturnsOk()).getAll(CreateAccountResponse[].class);
    }

    public static Account getAccountByNumber(String username, String password, String accountNumber) {
        return getAllAccounts(username, password).stream()
                .filter(account -> accountNumber.equals(account.getAccountNumber()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        String.format("Account %s not found for user %s", accountNumber, username)
                ));
    }

    public static CustomerProfileResponse getCustomerProfile(String username, String password){
        return new ValidatedCrudRequester<CustomerProfileResponse>(
                RequestSpec.authSpec(username, password),
                Endpoint.CUSTOMER_PROFILE_GET,
                ResponseSpec.requestReturnsOk())
                .get();
    }

}
