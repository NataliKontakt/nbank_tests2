package common.extensions;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.requests.steps.UserSteps;
import common.annotations.PreparedAccount;
import common.storage.SessionStorage;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreparedAccountExtension implements BeforeEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        PreparedAccount annotation = context.getRequiredTestMethod()
                .getAnnotation(PreparedAccount.class);

        if (annotation != null) {
            int accountCount = annotation.count();
            float[] deposits = annotation.deposits();
            boolean randomDeposit = annotation.randomDeposit();

            if (SessionStorage.getSteps() == null) {
                throw new IllegalStateException("@PreparedAccount требует предварительно созданного пользователя через @UserSession");
            }

            // Генерируем случайный депозит если нужно
            if (randomDeposit && deposits.length == 0) {
                float randomDepositValue = RandomData.getDeposit();
                deposits = new float[]{randomDepositValue};
            }

            Map<Integer, List<CreateAccountResponse>> allPreparedAccounts = new HashMap<>();

            for (int userIndex = 1; userIndex <= SessionStorage.getUserCount(); userIndex++) {
                // НЕ переключаем UI сессию! Используем только API
                UserSteps userSteps = SessionStorage.getSteps(userIndex); // Используем метод с параметром

                List<CreateAccountResponse> userAccounts = new ArrayList<>();

                for (int accountIndex = 0; accountIndex < accountCount; accountIndex++) {
                    CreateAccountResponse account = userSteps.createAccount();

                    if (userIndex == 1 && deposits.length > accountIndex && deposits[accountIndex] > 0) {
                        userSteps.makeDeposit(account.getId(), deposits[accountIndex]);
                        account.setBalance(deposits[accountIndex]);
                    }

                    userAccounts.add(account);
                }

                allPreparedAccounts.put(userIndex, userAccounts);
            }

            SessionStorage.addPreparedAccounts(allPreparedAccounts);
        }
    }

}
