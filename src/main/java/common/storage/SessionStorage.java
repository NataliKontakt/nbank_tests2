package common.storage;

import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static ui.pages.BasePage.authAsUser;


public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();
    // Текущий активный пользователь (индекс начиная с 1)
    private int currentUserIndex = 1;

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user: users) {
            INSTANCE.userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
        // После добавления пользователей сразу установить первого как текущего
        if (!users.isEmpty()) {
            INSTANCE.currentUserIndex = 1;
        }
    }

    /**
     * Возвращаем объект CreateUserRequest по его порядковому номеру в списке созданных пользователей.
     * @param number Порядковый номер, начиная с 1 (а не с 0).
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру.
     */
    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.keySet()).get(number-1);
    }

    public static CreateUserRequest getUser() {
        return getUser(INSTANCE.currentUserIndex);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.userStepsMap.values()).get(number-1);
    }

    public static UserSteps getSteps() {
        return getSteps(INSTANCE.currentUserIndex);
    }
    /**
     * Переключает текущую активную сессию на пользователя с указанным номером.
     * После этого все вызовы getSteps() / getUser() без параметров будут работать с этим пользователем.
     *
     * @param number номер пользователя начиная с 1
     */
    public static void switchToSession(int number) {
        CreateUserRequest user = getUser(number);
        authAsUser(user);
    }

    public static void clear() {
        INSTANCE.userStepsMap.clear();
        INSTANCE.currentUserIndex = 1;
    }
}
