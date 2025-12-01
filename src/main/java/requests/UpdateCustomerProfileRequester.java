package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.CustomerProfileResponse;
import models.UpdateProfileRequest;

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
                .spec((requestSpecification))
                .body(model)
                .put("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    public CustomerProfileResponse get() {
        return  given()
                .spec((requestSpecification))
                .get("/api/v1/customer/profile")
                .then()
                .assertThat()
                .spec(responseSpecification)
                .extract().body().as(CustomerProfileResponse.class);

    }


}
