package common.storage;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.UserSteps;

import java.util.*;

import static ui.pages.BasePage.authAsUser;


public class SessionStorage {
    /* Thread Local - способ сделать SessionStorage потокобезопасным

    Каждый поток обращаясь к INSTANCE.get() получают свою КОПИЮ

    Map<Thread, SessionStorage>

    Тест1 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ1), работает с ними
    Тест2 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ2), работает с ними
    Тест3 : создал юзеров, положил в SessionStorage (СВОЯ КОПИЯ3), работает с ними
     */
    private static final ThreadLocal<SessionStorage> INSTANCE = ThreadLocal.withInitial(SessionStorage::new);

    private final LinkedHashMap<CreateUserRequest, UserSteps> userStepsMap = new LinkedHashMap<>();
    private final Map<Integer, List<CreateAccountResponse>> preparedAccountsByUser = new HashMap<>();
    // Текущий активный пользователь (индекс начиная с 1)
    private int currentUserIndex = 1;

    private SessionStorage() {}

    public static void addUsers(List<CreateUserRequest> users) {
        for (CreateUserRequest user: users) {
            INSTANCE.get().userStepsMap.put(user, new UserSteps(user.getUsername(), user.getPassword()));
        }
        // После добавления пользователей сразу установить первого как текущего
        if (!users.isEmpty()) {
            INSTANCE.get().currentUserIndex = 1;
        }
    }

    /**
     * Возвращаем объект CreateUserRequest по его порядковому номеру в списке созданных пользователей.
     * @param number Порядковый номер, начиная с 1 (а не с 0).
     * @return Объект CreateUserRequest, соответствующий указанному порядковому номеру.
     */
    public static CreateUserRequest getUser(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.keySet()).get(number-1);
    }

    public static CreateUserRequest getUser() {
        return getUser(INSTANCE.get().currentUserIndex);
    }

    public static UserSteps getSteps(int number) {
        return new ArrayList<>(INSTANCE.get().userStepsMap.values()).get(number-1);
    }

    public static UserSteps getSteps() {
        return getSteps(INSTANCE.get().currentUserIndex);
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
        return INSTANCE.get().userStepsMap.size();
    }

    /**
     * Добавляет подготовленные аккаунты для всех пользователей
     */
    public static void addPreparedAccounts(Map<Integer, List<CreateAccountResponse>> allAccounts) {
        INSTANCE.get().preparedAccountsByUser.clear();
        INSTANCE.get().preparedAccountsByUser.putAll(allAccounts);
    }

    /**
     * Возвращает подготовленный аккаунт по индексу для текущего пользователя
     */
    public static CreateAccountResponse getPreparedAccount(int accountIndex) {
        return getPreparedAccount(INSTANCE.get().currentUserIndex, accountIndex);
    }

    /**
     * Возвращает подготовленный аккаунт по индексу для указанного пользователя
     */
    public static CreateAccountResponse getPreparedAccount(int userIndex, int accountIndex) {
        List<CreateAccountResponse> accounts = INSTANCE.get().preparedAccountsByUser.get(userIndex);
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
        INSTANCE.get().userStepsMap.clear();
        INSTANCE.get().preparedAccountsByUser.clear();
        INSTANCE.get().currentUserIndex = 1;
    }
}
