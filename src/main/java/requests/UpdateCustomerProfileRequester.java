package requests;

import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.Account;
import models.CustomerAccountsResponse;
import models.CustomerProfileResponse;
import models.UpdateProfileRequest;

import java.util.List;

import static io.restassured.RestAssured.given;

public class UpdateCustomerProfileRequester extends Request<UpdateProfileRequest> {
    public UpdateCustomerProfileRequester(RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(UpdateProfileRequest model) {
        return null;
    }

    public ValidatableResponse put(UpdateProfileRequest model) {
        return  given()
                .spec(requestSpecification)
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    public CustomerProfileResponse getProfile() {
        return  given()
                .spec(requestSpecification)
                .get("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification)
                .extract().body().as(CustomerProfileResponse.class);

    }
    public CustomerAccountsResponse getAccounts() {

        List<Account> accounts = given()
                .spec(requestSpecification)
                .get("/api/v1/customer/accounts")
                .then()
                .assertThat()
                .spec(responseSpecification)
                .extract().body().as(new TypeRef<List<Account>>() {});

        return CustomerAccountsResponse.fromArray(accounts);

    }



}
