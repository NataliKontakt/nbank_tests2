package generators;

import com.github.curiousoddman.rgxgen.RgxGen;
import generators.annotations.GeneratingDoubleRule;
import generators.annotations.GeneratingStringRule;
import generators.annotations.Optional;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RandomModelGenerator {

    private static final Random random = new Random();
    // Метод для генерации без optional значений
    public static <T> T generate(Class<T> clazz) {
        return generateInternal(clazz, new Object[0]);
    }

    // Метод для генерации с optional значениями
    public static <T> T generate(Object... valuesAndClass) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) valuesAndClass[valuesAndClass.length - 1];
        Object[] optionalValues = Arrays.copyOf(valuesAndClass, valuesAndClass.length - 1);
        return generateInternal(clazz, optionalValues);
    }
    public static <T> T generate(Class<T> clazz, Map<String, Object> fixedValues) {
        T instance = generate(clazz); // используем существующий метод без optional

        if (fixedValues != null && !fixedValues.isEmpty()) {
            for (Map.Entry<String, Object> entry : fixedValues.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                try {
                    Field field = findField(clazz, fieldName);
                    if (field == null) {
                        throw new NoSuchFieldException(fieldName);
                    }
                    field.setAccessible(true);
                    field.set(instance, value);
                } catch (Exception e) {
                    throw new RuntimeException(
                            String.format("Failed to set fixed value for field '%s' in class %s",
                                    fieldName, clazz.getSimpleName()), e);
                }
            }
        }
        return instance;
    }
    private static <T> T generateInternal(Class<T> clazz, Object[] optionalValues) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            List<Field> fields = getAllFields(clazz);

            List<Field> optionalFields = new ArrayList<>();
            for (Field field : fields) {
                if (field.getAnnotation(Optional.class) != null) {
                    optionalFields.add(field);
                }
            }

            if (optionalValues.length != optionalFields.size()) {
                throw new IllegalArgumentException(
                        String.format("Expected %d optional values for class %s, but got %d",
                                optionalFields.size(), clazz.getSimpleName(), optionalValues.length)
                );
            }

            int optionalIndex = 0;
            for (Field field : fields) {
                field.setAccessible(true);

                if (field.getAnnotation(Optional.class) != null) {
                    field.set(instance, optionalValues[optionalIndex++]);
                    continue;
                }
                Object value;
                GeneratingStringRule stringRule = field.getAnnotation(GeneratingStringRule.class);
                if (stringRule != null) {
                    value = generateFromRegex(stringRule.regex(), field.getType());
                } else {
                    // ← 2. ДОБАВЛЕНА ПРОВЕРКА НА GeneratingDoubleRule
                    GeneratingDoubleRule doubleRule = field.getAnnotation(GeneratingDoubleRule.class);
                    value = doubleRule != null
                            ? generateFromDoubleRule(doubleRule, field.getType()) // ← вызов нового метода
                            : generateRandomValue(field);
                }

                field.set(instance, value);
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate entity", e);
        }
    }

    private static Object generateFromDoubleRule(GeneratingDoubleRule rule, Class<?> type) {
        // Генерируем случайное число в диапазоне [min, max]
        double value = rule.min() + (rule.max() - rule.min()) * random.nextDouble();

        // Округляем до указанной точности
        if (type.equals(Float.class) || type.equals(float.class)) {
            return (float) Math.round(value * Math.pow(10, rule.precision())) / (float) Math.pow(10, rule.precision());
        }

        // Для double и других типов
        return Math.round(value * Math.pow(10, rule.precision())) / Math.pow(10, rule.precision());
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static Object generateRandomValue(Field field) {
        Class<?> type = field.getType();
        if (type.equals(String.class)) {
            return UUID.randomUUID().toString().substring(0, 8);
        } else if (type.equals(Integer.class) || type.equals(int.class)) {
            return random.nextInt(1000);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return random.nextLong();
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return random.nextDouble() * 100;
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return random.nextFloat() * 100;
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return random.nextBoolean();
        } else if (type.equals(List.class)) {
            return generateRandomList(field);
        } else if (type.equals(Date.class)) {
            return new Date(System.currentTimeMillis() - random.nextInt(1000000000));
        } else {
            // Вложенный объект
            return generate(type);
        }
    }

    private static Object generateFromRegex(String regex, Class<?> type) {
        RgxGen rgxGen = new RgxGen(regex);
        String result = rgxGen.generate();
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(result);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(result);
        } else if (type.equals(Float.class) || type.equals(float.class)) { // ДОБАВЛЕНА ПОДДЕРЖКА FLOAT
            return Float.parseFloat(result);
        } else if (type.equals(Double.class) || type.equals(double.class)) { //  ДОБАВЛЕНА ПОДДЕРЖКА DOUBLE
            return Double.parseDouble(result);
        } else {
            return result;
        }
    }

    private static List<String> generateRandomList(Field field) {
        // Пытаемся определить generic-параметр списка
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type actualType = pt.getActualTypeArguments()[0];
            if (actualType == String.class) {
                return List.of(UUID.randomUUID().toString().substring(0, 5),
                        UUID.randomUUID().toString().substring(0, 5));
            }
        }
        return Collections.emptyList();
    }
    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
