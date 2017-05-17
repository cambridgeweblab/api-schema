package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import java.net.URI;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since 15/10/15
 */
public class AdditionalConstraintResolverTest {
    private AdditionalConstraintResolver additionalConstraintResolver;
    @SuppressWarnings("unused")
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

        @JsonSchema(readOnlyExpression = "#{100 < 500}")
        private String numberField;

        @JsonSchema(readOnlyExpression = "#{#currentUsername == 'peterpan'}")
        private String userField;

        @JsonSchema(enumRef = "urn:xc:i18n:countries:$isoCodes")
        private String enumRefField;

        @JsonSchema(mediaType = "text/html")
        private String someFileString;

        public String getEnumRefField() {
            return enumRefField;
        }

        public String getUserField() {
            return userField;
        }

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

        public String getNumberField() {
            return numberField;
        }

        public String getSomeFileString() {
            return someFileString;
        }
    }

    @Before
    public void setup() {
        //put something on the context to test the readonlyexpression annotation value
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("currentUsername", "peterpan");
        additionalConstraintResolver = new AdditionalConstraintResolver(context);
    }

    @Test
    public void whenNewCustomFormatSpecifiedThenValueBehavesLikeEnum() throws NoSuchFieldException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
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

    @Test
    public void testReadOnlyExpressionAttribute() throws NoSuchFieldException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("numberField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        Optional<Boolean> readOnlyExpression = additionalConstraintResolver.getReadOnlyExpression(prop);
        assertNotNull(readOnlyExpression.get());
        assertTrue("Readonly expression is not true", readOnlyExpression.get());

    }

    @Test
    public void testReadOnlyExpressionAttributeWithContext() throws NoSuchFieldException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("userField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        Optional<Boolean> readOnlyExpression = additionalConstraintResolver.getReadOnlyExpression(prop);
        assertNotNull(readOnlyExpression.get());
        assertTrue("Readonly expression is not true", readOnlyExpression.get());
    }

    @Test
    public void testEnumRef() throws NoSuchFieldException {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("enumRefField").getAnnotation(JsonSchema.class);

        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        Optional<URI> uri = additionalConstraintResolver.getEnumRef(prop);
        assertNotNull(uri);
    }

    @Test
    public void testGetMediaType() throws Exception {
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonSchema annotationMediaType = DummyBean.class.getDeclaredField("someFileString").getAnnotation(JsonSchema.class);
        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotationMediaType);

        Optional<String> mediaType = additionalConstraintResolver.getMediaType(prop);
        assertNotNull(mediaType);
        assertEquals("text/html", mediaType.get());

        //check any other one
        JsonSchema annotationEnumRef = DummyBean.class.getDeclaredField("enumRefField").getAnnotation(JsonSchema.class);
        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotationEnumRef);
        mediaType = additionalConstraintResolver.getMediaType(prop);
        assertTrue(!mediaType.isPresent());
    }
}
