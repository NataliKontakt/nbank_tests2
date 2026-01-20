package api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import java.util.List;

public class ResponseSpec {
    public static String errorInvalidTransfer = "Invalid transfer: insufficient funds or invalid accounts";
    public static String errorTranslationLessZero = "Transfer amount must be at least 0.01";
    public static String errorDepositLessZero = "Deposit amount must be at least 0.01";
    public static String errorDepositCannotExceed_5000 = "Deposit amount cannot exceed 5000";
    public static String errorNameMustContainTwoWords = "Name must contain two words with letters only";
    public static String errorUsernameMustContain = "Username must contain only letters, digits, dashes, underscores, and dots";
    public static String errorUsernameMustBeLength = "Username must be between 3 and 15 characters";
    public static String errorUsernameCanNotBeBlank = "Username cannot be blank";

    private ResponseSpec(){}

    private static ResponseSpecBuilder defaultResponseBuilder(){
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreatad(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOk(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(String errorKey, List<String> errorValue){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.containsInAnyOrder(errorValue.toArray()))
                .build();

    }

    public static ResponseSpecification requestReturnsBadRequest(String  errorValue){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.equalTo(errorValue))
                .build();
    }

    public static ResponseSpecification requestReturnsForbiddenRequest(){
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(Matchers.equalTo("Unauthorized access to account"))
                .build();
    }
}
