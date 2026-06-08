package api.database;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Универсальный маппер ResultSet → POJO через рефлексию полей.
 * Упрощённая версия без аннотации @Column.
 */
public class ResultSetMapper {

    private static final Map<Class<?>, ResultSetMapper> CACHE = new ConcurrentHashMap<>();

    /**
     * Основной метод маппинга
     */
    public static <T> T map(ResultSet rs, Class<T> clazz) throws SQLException {
        if (!rs.next()) {
            return null;
        }

        ResultSetMapper mapper = CACHE.computeIfAbsent(clazz, ResultSetMapper::new);
        return mapper.mapRow(rs);
    }

    private final Class<?> clazz;
    private final Map<String, Field> fieldMap = new HashMap<>();

    private ResultSetMapper(Class<?> clazz) {
        this.clazz = clazz;
        initFieldMapping();
    }

    /**
     * Инициализируем соответствие колонок БД и полей класса
     */
    private void initFieldMapping() {
        for (Field field : clazz.getDeclaredFields()) {
            String columnName = toSnakeCase(field.getName());  // Автоматическое преобразование
            field.setAccessible(true);
            fieldMap.put(columnName.toLowerCase(), field);
        }
    }

    /**
     * Преобразует camelCase в snake_case
     * Пример: accountNumber → account_number
     */
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Маппинг одной строки ResultSet в объект
     */
    @SuppressWarnings("unchecked")
    private <T> T mapRow(ResultSet rs) {
        try {
            T instance = (T) clazz.getDeclaredConstructor().newInstance();

            for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                String columnName = entry.getKey();
                Field field = entry.getValue();

                Object value = getValueFromResultSet(rs, field.getType(), columnName);

                if (value != null) {
                    field.set(instance, value);
                }
            }

            return instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to map ResultSet to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Извлечение значения с правильной обработкой NULL
     */
    private Object getValueFromResultSet(ResultSet rs, Class<?> type, String columnName) throws SQLException {
        if (type == Long.class || type == long.class) {
            long value = rs.getLong(columnName);
            return rs.wasNull() ? null : value;
        }
        if (type == Integer.class || type == int.class) {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? null : value;
        }
        if (type == Float.class || type == float.class) {
            float value = rs.getFloat(columnName);
            return rs.wasNull() ? null : value;
        }
        if (type == Double.class || type == double.class) {
            double value = rs.getDouble(columnName);
            return rs.wasNull() ? null : value;
        }
        if (type == Boolean.class || type == boolean.class) {
            return rs.getBoolean(columnName);
        }
        if (type == String.class) {
            return rs.getString(columnName);
        }

        // Для остальных типов
        return rs.getObject(columnName);
    }
}