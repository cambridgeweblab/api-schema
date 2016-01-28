package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;
import com.fasterxml.jackson.databind.util.EnumValues;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @since 15/10/15
 */
public class AdditionalConstraintResolverTest {
    private AdditionalConstraintResolver additionalConstraintResolver = new AdditionalConstraintResolver();

    static class DummyBean {
        @JsonSchema(customFormat = "loopy")
        private String loopyField;

        @JsonSchema(customFormat = "squiggly")
        private String squigglyField;

        @JsonSchema(format = JsonValueFormat.DATE_TIME)
        private LocalDateTime temperamentalField;

        @JsonSchema(enumValues = {
                @EnumConstant("strawberry"),
                @EnumConstant("raspberry")
        })
        private String pickyField;

        @JsonSchema(enumValues = {
                @EnumConstant(value = "blueberry", title = "Muffin"),
                @EnumConstant(value = "gooseberry", title = "Crumble"),
                @EnumConstant(value = "blackcurrant", title = "Tart")
        })
        private String fruityField;

        public String getLoopyField() {
            return loopyField;
        }

        public String getSquigglyField() {
            return squigglyField;
        }

        public LocalDateTime getTemperamentalField() {
            return temperamentalField;
        }

        public String getPickyField() {
            return pickyField;
        }

        public String getFruityField() {
            return fruityField;
        }
    }

    @Before
    public void resetEnum() {
        final EnumBuster<JsonValueFormat> enumBuster = JsonValueFormatHelper.getEnumBuster(JsonValueFormat.class);
        //noinspection StatementWithEmptyBody
        while (enumBuster.undo()) {
        }
        ;
        Assume.assumeThat("Expect enum to be reset", JsonValueFormat.values().length, CoreMatchers.equalTo(13));
    }

    @Test
    public void whenNewCustomFormatSpecifiedThenEnumManipulatedAtRuntime() throws NoSuchFieldException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("loopyField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);
        final Optional<JsonValueFormat> result = additionalConstraintResolver.getValueFormat(prop, provider);
        assertTrue("Expect value format returned", result.isPresent());
        final JsonValueFormat valueFormat = result.get();
        assertEquals("Expect value format to match annotation", annotation.customFormat(), valueFormat.toString());

        assertThat("Expect enum values to include new value", Arrays.asList(JsonValueFormat.values()), hasItem(valueFormat));
        assertThat("Expect enum class to include new value", Arrays.asList(JsonValueFormat.class.getEnumConstants()), hasItem(valueFormat));
        assertEquals("Expect valueOf() to work", valueFormat, JsonValueFormat.valueOf(annotation.customFormat()));
    }

    @Test
    public void whenNewCustomFormatSpecifiedThenEnumSerializerWorks() throws NoSuchFieldException, IOException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        MapperConfig<?> config = Mockito.mock(MapperConfig.class);
        JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("squigglyField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);
        when(config.getAnnotationIntrospector()).thenReturn(new JacksonAnnotationIntrospector());
        when(config.compileString(anyString())).thenAnswer(call -> new SerializedString((String) call.getArguments()[0]));
        final EnumSerializer enumSerializer = new EnumSerializer(EnumValues.constructFromName(config, (Class) JsonValueFormat.class), false);
        when(provider.findPrimaryPropertySerializer(JsonValueFormat.class, prop)).thenReturn((JsonSerializer) enumSerializer);

        final Optional<JsonValueFormat> result = additionalConstraintResolver.getValueFormat(prop, provider);
        assumeTrue("Assume value format returned", result.isPresent());
        final JsonValueFormat valueFormat = result.get();
        assumeTrue("Assume value format to match annotation", annotation.customFormat().equals(valueFormat.toString()));

        enumSerializer.serialize(valueFormat, jsonGenerator, provider);

        verify(jsonGenerator).writeString(new SerializedString(annotation.customFormat()));
    }

    @Test
    public void whenStandardFormatSpecifiedThenEnumSerializerWorks() throws NoSuchFieldException, IOException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        MapperConfig<?> config = Mockito.mock(MapperConfig.class);
        JsonGenerator jsonGenerator = Mockito.mock(JsonGenerator.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("temperamentalField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);
        when(config.getAnnotationIntrospector()).thenReturn(new JacksonAnnotationIntrospector());
        when(config.compileString(anyString())).thenAnswer(call -> new SerializedString((String) call.getArguments()[0]));
        final EnumSerializer enumSerializer = new EnumSerializer(EnumValues.constructFromName(config, (Class) JsonValueFormat.class), false);
        when(provider.findPrimaryPropertySerializer(JsonValueFormat.class, prop)).thenReturn((JsonSerializer) enumSerializer);

        final Optional<JsonValueFormat> result = additionalConstraintResolver.getValueFormat(prop, provider);
        assumeTrue("Assume value format returned", result.isPresent());
        final JsonValueFormat valueFormat = result.get();
        assumeTrue("Assume value format to match annotation", annotation.format()[0].equals(valueFormat));

        enumSerializer.serialize(valueFormat, jsonGenerator, provider);

        verify(jsonGenerator).writeString(new SerializedString(annotation.format()[0].toString()));
    }

    @Test
    public void whenEnumConstantsSpecified_thenOrderedMapReturned() throws NoSuchFieldException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("pickyField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        Optional<Map<String, String>> result = additionalConstraintResolver.getEnumConstants(prop);
        assertTrue("Expect map returned", result.isPresent());
        Map<String, String> map = result.get();
        assertEquals("Expect " + annotation.enumValues().length + " enum constants", annotation.enumValues().length, map.size());
        assertThat("Expect map keys in order", map.keySet(), Matchers.contains(Arrays.stream(annotation.enumValues()).map(EnumConstant::value).toArray()));
        for (EnumConstant enumConstant : annotation.enumValues()) {
            assertEquals("Expect " + enumConstant.value() + " constant to be eponymous", enumConstant.value(), map.get(enumConstant.value()));
        }
    }

    @Test
    public void whenEnumConstantsSpecifiedWithTitle_thenOrderedMapReturned() throws NoSuchFieldException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("fruityField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        Optional<Map<String, String>> result = additionalConstraintResolver.getEnumConstants(prop);
        assertTrue("Expect map returned", result.isPresent());
        Map<String, String> map = result.get();
        assertEquals("Expect " + annotation.enumValues().length + " enum constants", annotation.enumValues().length, map.size());
        assertThat("Expect map keys in order", map.keySet(), Matchers.contains(Arrays.stream(annotation.enumValues()).map(EnumConstant::value).toArray()));
        for (EnumConstant enumConstant : annotation.enumValues()) {
            assertEquals("Expect " + enumConstant.value() + " constant to have title", enumConstant.title(), map.get(enumConstant.value()));
        }
    }
}
