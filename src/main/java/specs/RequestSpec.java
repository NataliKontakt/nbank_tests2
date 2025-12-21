package specs;

import configs.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import models.LoginRequest;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpec {
    private static Map<String, String> authHeaders = new HashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));
    private RequestSpec(){};

    private static RequestSpecBuilder defaultRequestBuilder(){
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter()))
                .setBaseUri(Config.getProperty("server") + Config.getProperty("apiVersion"));

    }

    public static RequestSpecification unauthSpec(){
        return defaultRequestBuilder().build();
    }

    public static RequestSpecification adminSpec(){
        return defaultRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification authSpec(String username, String password) {
        String userAuthHeader;

        if (!authHeaders.containsKey(username)) {
            userAuthHeader = new CrudRequester(
                    RequestSpec.unauthSpec(),
                    Endpoint.LOGIN,
                    ResponseSpec.requestReturnsOk())
                    .post(LoginRequest.builder().username(username).password(password).build())
                    .extract()
                    .header("Authorization");

            authHeaders.put(username, userAuthHeader);
        } else {
            userAuthHeader = authHeaders.get(username);
        }

        return defaultRequestBuilder()
                .addHeader("Authorization", userAuthHeader)
                .build();
    }
}
