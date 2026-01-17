package common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreparedAccount {
    /**
     * Количество аккаунтов для создания для каждого пользователя
     * По умолчанию создается 1 аккаунт
     */
    int count() default 1;

    /**
     * Депозиты только для первого пользователя
     * Для второго и последующих пользователей ВСЕ аккаунты всегда с балансом 0
     */
    float[] deposits() default {};

    /**
     * Если true, будет использовать RandomData.getDeposit()
     * для первого аккаунта первого пользователя
     */
    boolean randomDeposit() default false;
}
