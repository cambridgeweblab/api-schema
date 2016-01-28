package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Contains methods for manipulating the JsonValueFormat enum.
 *
 * @since 05/12/15
 */
public class JsonValueFormatHelper {
    private Logger log = LoggerFactory.getLogger(getClass());
    private static Map<Class, EnumBuster> enumBusterMap = new ConcurrentHashMap<>();

    public JsonValueFormat getStandardJsonValueFormat(BeanProperty prop, SerializerProvider provider, JsonValueFormat jsonValueFormat) {
        final JsonValueFormat format = jsonValueFormat;
        if (updateValueFormatEnumSerializer(prop, provider, format, false)) {
            log.info("Updated Jackson value format mappings to use JsonValueFormat.toString().");
        }
        ;
        return format;
    }

    public JsonValueFormat getCustomJsonValueFormat(final String format) {
        JsonValueFormat customFormat;
        try {
            customFormat = JsonValueFormat.valueOf(format);
        } catch (IllegalArgumentException e) {
            customFormat = fakeEnumValue(JsonValueFormat.class, format);
            log.info("Registered new custom format '" + format + "' with enum only.");
        }
        return customFormat;
    }

    public JsonValueFormat getCustomJsonValueFormat(BeanProperty prop, SerializerProvider provider, final String format) {
        JsonValueFormat customFormat;
        try {
            customFormat = JsonValueFormat.valueOf(format);
        } catch (IllegalArgumentException e) {
            customFormat = fakeEnumValue(JsonValueFormat.class, format);
            if (updateValueFormatEnumSerializer(prop, provider, customFormat, true)) {
                log.info("Registered new custom format '" + format + "' with Jackson.");
            }
        }
        return customFormat;
    }

    private boolean updateValueFormatEnumSerializer(final BeanProperty prop, final SerializerProvider provider, final JsonValueFormat valueFormat, final boolean alwaysUpdate) {
        Function<Class<?>, Optional<EnumSerializer>> propertySerializerSource = (clazz) -> {
            try {
                return Optional.of((EnumSerializer) (JsonSerializer) provider.findPrimaryPropertySerializer(clazz, prop));
            } catch (JsonMappingException e) {
                log.warn("Format '" + valueFormat + "' could not be added to Jackson for mapping", e);
            }
            return Optional.empty();
        };
        return updateValueFormatEnumSerializer(propertySerializerSource, valueFormat, alwaysUpdate);
    }

    private boolean updateValueFormatEnumSerializer(Function<Class<?>, Optional<EnumSerializer>> propertySerializerSource, JsonValueFormat valueFormat, boolean alwaysUpdate) {
        try {
            final Optional<EnumSerializer> enumSerializer = propertySerializerSource.apply(JsonValueFormat.class);
            if (enumSerializer.isPresent()) {
                final EnumMap enumMap = enumSerializer.get().getEnumValues().internalMap();
                if (alwaysUpdate || !valueFormat.toString().equals(enumMap.get(valueFormat).toString())) {
                    // Replace the enumMap content for this and future serializations.
                    enumMap.clear();
                    // Update the keyUniverse and vals
                    final Field keyUniverse = ReflectionUtils.findField(EnumMap.class, "keyUniverse");
                    final Field vals = ReflectionUtils.findField(EnumMap.class, "vals");
                    ReflectionUtils.makeAccessible(keyUniverse);
                    ReflectionUtils.makeAccessible(vals);
                    ReflectionUtils.setField(keyUniverse, enumMap, JsonValueFormat.values());
                    ReflectionUtils.setField(vals, enumMap, new Object[JsonValueFormat.values().length]);
                    for (Enum<?> en : JsonValueFormat.values()) {
                        enumMap.put(en, new SerializedString(en.toString()));
                    }
                    for (Enum<?> en : JsonValueFormat.values()) {
                        // We also need to replace the enumMap for any other classes of values of JsonValueFormat, since it uses anonymous subclasses for each standard format.
                        // These don't need an extended universe to work with.
                        if (!en.getClass().equals(JsonValueFormat.class)) {
                            final Optional<EnumSerializer> subEnumSerializer = propertySerializerSource.apply(en.getClass());
                            if (subEnumSerializer.isPresent() && subEnumSerializer != enumSerializer) {
                                final EnumMap subEnumMap = subEnumSerializer.get().getEnumValues().internalMap();
                                subEnumMap.clear();
                                subEnumMap.putAll(enumMap);
                                log.debug("Copy serializer enumMap from JsonValueFormat.class to " + en.getClass());
                            }
                        }
                    }
                    return true;
                }
            }
        } catch (NullPointerException e2) {
            log.debug("Format '" + valueFormat + "' should self-register as it is the first time Jackson's seen the enum", e2);
        }
        return false;
    }

    /**
     * We can safely construct and return a fake enum value because we only need its
     * {@link Enum#toString()} value, really. {@link EnumBuster} does allow us to
     * properly modify enums so that switch statements etc all keep working, but that
     * really would be truly evil.
     */
    private static <E extends Enum<E>> E fakeEnumValue(Class<E> enumClass, String value) {
        EnumBuster<E> enumBuster = getEnumBuster(enumClass);

        E made = enumBuster.make(value);
        enumBuster.addByValue(made);
        final Field enumConstants = ReflectionUtils.findField(enumClass.getClass(), "enumConstants");
        final Field enumConstantDirectory = ReflectionUtils.findField(enumClass.getClass(), "enumConstantDirectory");
        ReflectionUtils.makeAccessible(enumConstants);
        ReflectionUtils.makeAccessible(enumConstantDirectory);
        ReflectionUtils.setField(enumConstants, enumClass, null); // Force refetch from values()
        ReflectionUtils.setField(enumConstantDirectory, enumClass, null); // Force refetch from values()
        return made;
    }

    static <E extends Enum<E>> EnumBuster<E> getEnumBuster(Class<E> enumClass) {
        return enumBusterMap.computeIfAbsent(enumClass, e -> new EnumBuster(e));
    }
}
