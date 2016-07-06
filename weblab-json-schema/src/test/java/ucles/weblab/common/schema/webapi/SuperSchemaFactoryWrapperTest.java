package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ReferenceSchema;
import com.fasterxml.jackson.module.jsonSchema.types.StringSchema;
import com.fasterxml.jackson.module.jsonSchema.types.UnionTypeSchema;
import com.fasterxml.jackson.module.jsonSchema.types.ValueTypeSchema;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @since 15/12/15
 */
@RunWith(MockitoJUnitRunner.class)
public class SuperSchemaFactoryWrapperTest {
    final JsonSchemaFactory schemaFactory = new JsonSchemaFactory();
    final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    CrossContextConversionService crossContextConversionService;
    @Mock
    EnumSchemaCreator enumSchemaCreator;
    SuperSchemaFactoryWrapper superSchemaFactoryWrapper;

    @Before
    public void init() {
        superSchemaFactoryWrapper = new SuperSchemaFactoryWrapper(crossContextConversionService, enumSchemaCreator, objectMapper);
    }

    static class DummyBean {
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

        @JsonSchema(enumValues = {
                @EnumConstant(value = "brass", title = "Muck")
        })
        private String dirtyField;

        public String getPickyField() {
            return pickyField;
        }

        public String getFruityField() {
            return fruityField;
        }

        public String getDirtyField() {
            return dirtyField;
        }
    }

    @Test
    public void testInliningSimpleEnumSchema() throws NoSuchFieldException {
        BeanProperty prop = mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("pickyField").getAnnotation(JsonSchema.class);
        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        StringSchema enumSchema = schemaFactory.stringSchema();
        enumSchema.setEnums(Arrays.stream(annotation.enumValues()).map(EnumConstant::value).collect(Collectors.toCollection(HashSet<String>::new)));

        when(enumSchemaCreator.createEnum(anyMap(), any())).thenReturn(enumSchema);


        ValueTypeSchema baseSchema = schemaFactory.stringSchema();
        superSchemaFactoryWrapper.addEnumConstraints(baseSchema, prop);
        assertNotNull("Expect schema extension", baseSchema.getExtends());
        assertEquals("Expect one super-schema", 1, baseSchema.getExtends().length);

        com.fasterxml.jackson.module.jsonSchema.JsonSchema superSchema = baseSchema.getExtends()[0];
        assertTrue("Expect super-schema to be a string schema", superSchema.isStringSchema());
        assertEquals("Expect super-schema to have our enums", enumSchema.getEnums(), superSchema.asValueTypeSchema().getEnums());
    }

    @Test
    public void testInliningSingleValueEnumWithTitle() throws NoSuchFieldException {
        BeanProperty prop = mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("dirtyField").getAnnotation(JsonSchema.class);
        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        StringSchema enumSchema = schemaFactory.stringSchema();
        enumSchema.setEnums(Arrays.stream(annotation.enumValues()).map(EnumConstant::value).collect(Collectors.toCollection(HashSet<String>::new)));
        enumSchema.setTitle(Arrays.stream(annotation.enumValues()).findFirst().map(EnumConstant::title).get());

        when(enumSchemaCreator.createEnum(anyMap(), any())).thenReturn(enumSchema);

        ValueTypeSchema baseSchema = schemaFactory.stringSchema();
        superSchemaFactoryWrapper.addEnumConstraints(baseSchema, prop);
        assertNotNull("Expect schema extension", baseSchema.getExtends());
        assertEquals("Expect one super-schema", 1, baseSchema.getExtends().length);

        com.fasterxml.jackson.module.jsonSchema.JsonSchema superSchema = baseSchema.getExtends()[0];
        assertTrue("Expect super-schema to be a string schema", superSchema.isStringSchema());
        assertEquals("Expect super-schema to have our enums", enumSchema.getEnums(), superSchema.asValueTypeSchema().getEnums());
        assertEquals("Expect super-schema to have our title", enumSchema.getTitle(), superSchema.asValueTypeSchema().getTitle());
    }

    @Test
    public void testWrappingUnionTypeEnumSchemaAsDataUri() throws NoSuchFieldException, IOException {
        BeanProperty prop = mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("fruityField").getAnnotation(JsonSchema.class);
        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        UnionTypeSchema unionSchema = new EnumSchemaCreator.NonBrokenUnionTypeSchema();
        unionSchema.setElements(Arrays.stream(annotation.enumValues()).map(ev -> {
            ValueTypeSchema enumValue = schemaFactory.stringSchema();
            enumValue.setEnums(Collections.singleton(ev.value()));
            enumValue.setTitle(ev.title());
            return enumValue;
        }).toArray(ValueTypeSchema[]::new));

        when(enumSchemaCreator.createEnum(anyMap(), any())).thenReturn(unionSchema);

        ValueTypeSchema baseSchema = schemaFactory.stringSchema();
        superSchemaFactoryWrapper.addEnumConstraints(baseSchema, prop);
        assertNotNull("Expect schema extension", baseSchema.getExtends());
        assertEquals("Expect one super-schema", 1, baseSchema.getExtends().length);

        com.fasterxml.jackson.module.jsonSchema.JsonSchema superSchema = baseSchema.getExtends()[0];
        assertTrue("Expect super-schema to be a reference schema", superSchema instanceof ReferenceSchema);

        URI refUri = URI.create(superSchema.get$ref());
        assertEquals("Expect a data: URI", "data", refUri.getScheme());

        // Fetch the data URI and check it returns the expected JSON schema.
        String expectedJson = objectMapper.writeValueAsString(unionSchema);
        String content = new BufferedReader(new InputStreamReader(parseDataURI(refUri.toString()), StandardCharsets.UTF_8)).readLine();
        assertEquals("Expect data URI contents to match the serialized schema", expectedJson, content);
    }

    @Test
    public void testEnumValuesWithSpacesIn() throws NoSuchFieldException, IOException {
        BeanProperty prop = mock(BeanProperty.class);
        JsonSchema annotation = DummyBean.class.getDeclaredField("fruityField").getAnnotation(JsonSchema.class);
        when(prop.getAnnotation(JsonSchema.class)).thenReturn(annotation);

        StringSchema firstEnumValue = schemaFactory.stringSchema();
        firstEnumValue.setEnums(Collections.singleton("apple"));
        firstEnumValue.setTitle("Apple Juice Drink");
        StringSchema secondEnumValue = schemaFactory.stringSchema();
        secondEnumValue.setEnums(Collections.singleton("ribena"));
        secondEnumValue.setTitle("Ribena");
        UnionTypeSchema unionSchema = new EnumSchemaCreator.NonBrokenUnionTypeSchema();
        unionSchema.setElements(new ValueTypeSchema[]{firstEnumValue, secondEnumValue});

        when(enumSchemaCreator.createEnum(anyMap(), any())).thenReturn(unionSchema);
        ValueTypeSchema baseSchema = schemaFactory.stringSchema();
        superSchemaFactoryWrapper.addEnumConstraints(baseSchema, prop);
        Assume.assumeNotNull("Expect schema extension", baseSchema.getExtends());
        Assume.assumeTrue("Expect one super-schema", 1 == baseSchema.getExtends().length);
        com.fasterxml.jackson.module.jsonSchema.JsonSchema superSchema = baseSchema.getExtends()[0];
        URI refUri = URI.create(superSchema.get$ref());
        // + signs are not valid Base64, or URL encoding except in application/x-www-form-urlencoded MIME format
        assertTrue("Expect no + symbols in data", refUri.getSchemeSpecificPart().indexOf('+', refUri.getSchemeSpecificPart().indexOf(',')) < 0);

        JsonNode jsonNode = objectMapper.readTree(new InputStreamReader(parseDataURI(refUri.toString()), StandardCharsets.UTF_8));
    //    assertEquals("Expect enum title to match", firstEnumValue.getTitle(), jsonNode.get("type").get(0).get("title").asText());
        //TODO - get this assert working
    }

    /**
         * Parses inline data URIs as generated by MS Word's XML export and FO
         * stylesheet.
         *
         * @see <a href="http://www.ietf.org/rfc/rfc2397">RFC 2397</a>
         */
        private InputStream parseDataURI(String href) {
            int commaPos = href.indexOf(',');
            // header is of the form data:[<mediatype>][;base64]
            String header = href.substring(0, commaPos);
            String data = href.substring(commaPos + 1);
            if (header.endsWith(";base64")) {
                byte[] bytes = new byte[0];
                try {
                    bytes = data.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ByteArrayInputStream encodedStream = new ByteArrayInputStream(bytes);
                return Base64.getDecoder().wrap(encodedStream);
            } else {
                String encoding = "UTF-8";
                final int charsetpos = header.indexOf(";charset=");
                if (charsetpos > 0) {
                    encoding = header.substring(charsetpos + 9);
                }
                try {
                    final String unescapedString = URLDecoder.decode(data, encoding);
                    return new ByteArrayInputStream(unescapedString.getBytes(StandardCharsets.UTF_8));
                } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                    System.out.println(e.getMessage());
                }
            }
            return null;
        }

}
