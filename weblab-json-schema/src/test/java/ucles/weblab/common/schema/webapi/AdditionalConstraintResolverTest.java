package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since 15/10/15
 */
public class AdditionalConstraintResolverTest {
    private AdditionalConstraintResolver additionalConstraintResolver = new AdditionalConstraintResolver();

    static class DummyBean {
        @JsonSchema(format = "loopy")
        private String loopyField;

        @JsonSchema(format = "squiggly")
        private String squigglyField;

        @JsonSchema(format = JsonValueFormat.DATE_TIME_VALUE)
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

    @Test
    public void whenNewCustomFormatSpecifiedThenValueBehavesLikeEnum() throws NoSuchFieldException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("loopyField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);
        final Optional<JsonValueFormat> result = additionalConstraintResolver.getValueFormat(prop);
        assertTrue("Expect value format returned", result.isPresent());
        final JsonValueFormat valueFormat = result.get();
        assertEquals("Expect value format to match annotation", annotation.format(), valueFormat.toString());

        assertEquals("Expect valueOf() to work", valueFormat, JsonValueFormat.valueOf(annotation.format()));
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
