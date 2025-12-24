package requests.steps;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import specs.RequestSpec;
import specs.ResponseSpec;

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