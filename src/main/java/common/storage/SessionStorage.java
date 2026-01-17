package common.storage;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.*;

import static ui.pages.BasePage.authAsUser;


public class SessionStorage {
    private static final SessionStorage INSTANCE = new SessionStorage();

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();
    private final Map<Integer, List<CreateAccountResponse>> preparedAccountsByUser = new HashMap<>();
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

    /**
     * Возвращает количество созданных пользователей
     */
    public static int getUserCount() {
        return INSTANCE.userStepsMap.size();
    }

    /**
     * Добавляет подготовленные аккаунты для всех пользователей
     */
    public static void addPreparedAccounts(Map<Integer, List<CreateAccountResponse>> allAccounts) {
        INSTANCE.preparedAccountsByUser.clear();
        INSTANCE.preparedAccountsByUser.putAll(allAccounts);
    }

    /**
     * Возвращает подготовленный аккаунт по индексу для текущего пользователя
     */
    public static CreateAccountResponse getPreparedAccount(int accountIndex) {
        return getPreparedAccount(INSTANCE.currentUserIndex, accountIndex);
    }

    /**
     * Возвращает подготовленный аккаунт по индексу для указанного пользователя
     */
    public static CreateAccountResponse getPreparedAccount(int userIndex, int accountIndex) {
        List<CreateAccountResponse> accounts = INSTANCE.preparedAccountsByUser.get(userIndex);
        if (accounts == null || accounts.size() < accountIndex) {
            throw new IllegalStateException(
                    String.format("Подготовленный аккаунт %d для пользователя %d не найден",
                            accountIndex, userIndex)
            );
        }
        return accounts.get(accountIndex - 1);
    }

    /**
     * Полная очистка SessionStorage
     */
    public static void clear() {
        INSTANCE.userStepsMap.clear();
        INSTANCE.preparedAccountsByUser.clear();
        INSTANCE.currentUserIndex = 1;
    }
}
