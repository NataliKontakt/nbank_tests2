package api.generators;

import com.github.curiousoddman.rgxgen.RgxGen;
import api.generators.annotations.GeneratingDoubleRule;
import api.generators.annotations.GeneratingStringRule;
import api.generators.annotations.Optional;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RandomModelGenerator {

    private static final Random random = new Random();

    // Метод для генерации без optional значений
    public static <T> T generate(Class<T> clazz) {
        return generateWithFixed(clazz, Collections.emptyMap());
    }

    // Метод с фиксированными полями по имени (самый удобный и рекомендуемый)
    public static <T> T generate(Class<T> clazz, Map<String, Object> fixedValues) {
        return generateWithFixed(clazz, fixedValues != null ? fixedValues : Collections.emptyMap());
    }

    // Старый varargs-метод оставлен для совместимости
    public static <T> T generate(Object... valuesAndClass) {
        if (valuesAndClass == null || valuesAndClass.length == 0) {
            throw new IllegalArgumentException("At least class must be provided");
        }
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) valuesAndClass[valuesAndClass.length - 1];
        Map<String, Object> fixed = new HashMap<>();
        List<Field> optionalFields = getOptionalFields(clazz);

        if (valuesAndClass.length - 1 > optionalFields.size()) {
            throw new IllegalArgumentException("Too many optional values provided");
        }

        for (int i = 0; i < valuesAndClass.length - 1; i++) {
            Field field = optionalFields.get(i);
            fixed.put(field.getName(), valuesAndClass[i]);
        }

        return generateWithFixed(clazz, fixed);
    }

    // Основной метод генерации с приоритетами
    private static <T> T generateWithFixed(Class<T> clazz, Map<String, Object> fixedValues) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            List<Field> fields = getAllFields(clazz);

            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                // 1. Самый высокий приоритет: значение из fixedValues (по имени поля)
                if (fixedValues.containsKey(fieldName)) {
                    field.set(instance, fixedValues.get(fieldName));
                    continue;
                }

                // 2. Если поле @Optional и значение не передано — генерируем как обычное поле
                // (никакого continue — просто идём дальше по логике генерации)

                // 3. Генерация по аннотациям или рандомно
                Object value;
                GeneratingStringRule stringRule = field.getAnnotation(GeneratingStringRule.class);
                if (stringRule != null) {
                    value = generateFromRegex(stringRule.regex(), field.getType());
                } else {
                    GeneratingDoubleRule doubleRule = field.getAnnotation(GeneratingDoubleRule.class);
                    value = doubleRule != null
                            ? generateFromDoubleRule(doubleRule, field.getType())
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
        double value = rule.min() + (rule.max() - rule.min()) * random.nextDouble();
        if (type.equals(Float.class) || type.equals(float.class)) {
            return (float) (Math.round(value * Math.pow(10, rule.precision())) / Math.pow(10, rule.precision()));
        }
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

    private static List<Field> getOptionalFields(Class<?> clazz) {
        List<Field> optional = new ArrayList<>();
        for (Field field : getAllFields(clazz)) {
            if (field.getAnnotation(Optional.class) != null) {
                optional.add(field);
            }
        }
        return optional;
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
            return generate(type); // вложенный объект
        }
    }

    private static Object generateFromRegex(String regex, Class<?> type) {
        RgxGen rgxGen = new RgxGen(regex);
        String result = rgxGen.generate();
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(result);
        } else if (type.equals(Long.class) || type.equals(long.class)) {
            return Long.parseLong(result);
        } else if (type.equals(Float.class) || type.equals(float.class)) {
            return Float.parseFloat(result);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(result);
        } else {
            return result;
        }
    }

    private static List<String> generateRandomList(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType pt && pt.getActualTypeArguments()[0] == String.class) {
            return List.of(
                    UUID.randomUUID().toString().substring(0, 5),
                    UUID.randomUUID().toString().substring(0, 5)
            );
        }
        return Collections.emptyList();
    }
}