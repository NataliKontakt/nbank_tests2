package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateAccountResponse;
import api.models.CustomerAccountsResponse;
import api.models.DepositRequest;
import api.models.DepositResponse;

import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

import java.util.Map;

public class UserSteps {
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

    /*
    * new CrudRequester(RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpec.requestReturnsOk())
                .post(RandomModelGenerator.generate(
                        DepositRequest.class,
                        Map.of("id", id1, "balance", deposit1)));*/
}
