package api.requests.steps;

import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpec;
import api.specs.ResponseSpec;

public class AdminSteps {
    public static CreateUserRequest createUser(){
        CreateUserRequest user1 = RandomModelGenerator.generate(CreateUserRequest.class);

        // создание пользователя
        new ValidatedCrudRequester<CreateUserRequest>(RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.entityWasCreatad())
                .post(user1);
        return user1;
    }
}
/*
* //создание объекта пользователя
        CreateUserRequest user1 = RandomModelGenerator.generate(CreateUserRequest.class);

        // создание пользователя
        new CrudRequester(RequestSpec.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpec.entityWasCreatad())
                .post(user1);

        // создаем аккаунт(счет)
        CreateAccountResponse response =  new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpec.authSpec(user1.getUsername(), user1.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpec.entityWasCreatad())
                .post(null);*/