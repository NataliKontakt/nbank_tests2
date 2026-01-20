package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

import java.util.List;

public class AdminSteps {
    public static CreateUserRequest createUser(){
        CreateUserRequest user = RandomModelGenerator.generate(CreateUserRequest.class);

        // создание пользователя
        new ValidatedCrudRequester<CreateUserRequest>(RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.entityWasCreatad())
                .post(user);
        return user;
    }

    public static List<CreateUserResponse> getAllUsers() {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.requestReturnsOk()).getAll(CreateUserResponse[].class);
    }
}
