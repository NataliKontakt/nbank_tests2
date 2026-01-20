package api.generators;

import java.math.BigDecimal;

public class MoneyMath {
    private MoneyMath() {}

  //сложение
    public static float add(float a, float b) {
        return toBigDecimal(a).add(toBigDecimal(b)).floatValue();
    }

   //вычитание
    public static float subtract(float a, float b) {
        return toBigDecimal(a).subtract(toBigDecimal(b)).floatValue();
    }

   //Вспомогательный метод для преобразования float в BigDecimal
    private static BigDecimal toBigDecimal(float value) {
        return new BigDecimal(String.valueOf(value));
    }
}
