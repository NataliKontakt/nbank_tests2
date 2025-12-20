package models.comparison;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelComparator {

    public static <A, B> ComparisonResult compareFields(A request, B response, Map<String, String> fieldMappings) {
        List<Mismatch> mismatches = new ArrayList<>();

        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
            String requestField = entry.getKey();
            String responseField = entry.getValue();

            Object value1 = getFieldValue(request, requestField);
            Object value2 = getFieldValue(response, responseField);

            if (!Objects.equals(String.valueOf(value1), String.valueOf(value2))) {
                mismatches.add(new Mismatch(requestField + " -> " + responseField, value1, value2));
            }
        }

        return new ComparisonResult(mismatches);
    }

    private static Object getFieldValue(Object obj, String fieldName) {
        // Разделяем на части по точке
        String[] parts = fieldName.split("\\.");
        Object current = obj;

        for (String part : parts) {
            if (current == null) return null;

            // Если часть содержит индекс [n]
            if (part.contains("[")) {
                current = getIndexedValue(current, part);
            } else {
                current = getDirectFieldValue(current, part);
            }
        }

        return current;
    }

    private static Object getIndexedValue(Object obj, String fieldWithIndex) {
        // Извлекаем имя поля и индекс
        int bracketIndex = fieldWithIndex.indexOf('[');
        String fieldName = fieldWithIndex.substring(0, bracketIndex);
        int index = Integer.parseInt(
                fieldWithIndex.substring(bracketIndex + 1, fieldWithIndex.indexOf(']'))
        );

        // Получаем коллекцию/массив
        Object collection = getDirectFieldValue(obj, fieldName);
        if (collection == null) return null;

        // Обрабатываем список
        if (collection instanceof List) {
            List<?> list = (List<?>) collection;
            return index >= 0 && index < list.size() ? list.get(index) : null;
        }

        // Обрабатываем массив
        if (collection.getClass().isArray()) {
            Object[] array = (Object[]) collection;
            return index >= 0 && index < array.length ? array[index] : null;
        }

        throw new RuntimeException("Field '" + fieldName + "' is not a collection or array");
    }

    private static Object getDirectFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + fieldName, e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName + " in class " + obj.getClass().getName());
    }

    public static class ComparisonResult {
        private final List<Mismatch> mismatches;

        public ComparisonResult(List<Mismatch> mismatches) {
            this.mismatches = mismatches;
        }

        public boolean isSuccess() {
            return mismatches.isEmpty();
        }

        public List<Mismatch> getMismatches() {
            return mismatches;
        }

        @Override
        public String toString() {
            if (isSuccess()) {
                return "All fields match.";
            }
            StringBuilder sb = new StringBuilder("Mismatched fields:\n");
            for (Mismatch m : mismatches) {
                sb.append("- ").append(m.fieldName)
                        .append(": expected=").append(m.expected)
                        .append(", actual=").append(m.actual).append("\n");
            }
            return sb.toString();
        }
    }

    public static class Mismatch {
        public final String fieldName;
        public final Object expected;
        public final Object actual;

        public Mismatch(String fieldName, Object expected, Object actual) {
            this.fieldName = fieldName;
            this.expected = expected;
            this.actual = actual;
        }
    }
}
