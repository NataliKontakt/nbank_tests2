package common.extensions;


import common.annotations.Browsers;
import common.annotations.Platforms;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

public class PlatformMatchExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        Platforms annotation = extensionContext.getElement()
                .map(el -> el.getAnnotation(Platforms.class))
                .orElse(null);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет ограничений к платформе");
        }

        // Берем platform из системной переменной, по умолчанию "web"
        String currentPlatform = System.getProperty("platform", "web");

        boolean matches = Arrays.stream(annotation.value())
                .anyMatch(platform -> platform.equalsIgnoreCase(currentPlatform));

        if (matches) {
            return ConditionEvaluationResult.enabled("Текущая платформа удовлетворяет условию: " + currentPlatform);
        } else {
            return ConditionEvaluationResult.disabled(
                    "Тест пропущен, так как текущая платформа " + currentPlatform +
                            " не находится в списке допустимых платформ для теста: " + Arrays.toString(annotation.value())
            );
        }
    }
}
