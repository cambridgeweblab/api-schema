package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ucles.weblab.common.i18n.service.LocalisationService;
import ucles.weblab.common.xc.service.CrossContextConversionService;

import java.security.Principal;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @since 22/01/2016
 */
public class ControllerMethodSchemaCreatorTest {
    private final CrossContextConversionService crossContextConversionService = mock(CrossContextConversionService.class);
    private final EnumSchemaCreator enumSchemaCreator = mock(EnumSchemaCreator.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LocalisationService localisationService = mock(LocalisationService.class);
    private ControllerMethodSchemaCreator schemaCreator = new ControllerMethodSchemaCreator(objectMapper, crossContextConversionService, enumSchemaCreator, localisationService);

    @Before
    public void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    public void testMappingStringRequestParams() {
        com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = schemaCreator.createForRequestParams(methodOn(CarController.class).configureCar("red"));
        assertEquals("Expect object schema", JsonFormatTypes.OBJECT, schema.getType());
        ObjectSchema objectSchema = schema.asObjectSchema();
        assertThat("Expect 'color' property", objectSchema.getProperties().keySet(), hasItem("color"));
        assertEquals("Expect title", "Paint colour", objectSchema.getProperties().get("color").asSimpleTypeSchema().getTitle());
        assertEquals("Expect default value", "Silver", objectSchema.getProperties().get("color").asSimpleTypeSchema().getDefault());
        assertEquals("Expect required", true, objectSchema.getProperties().get("color").getRequired());
    }

    @Test
    public void testSkippingNonRequestParams() {
        com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = schemaCreator.createForRequestParams(methodOn(CarController.class).configureModelTForUser(null));
        assertEquals("Expect object schema", JsonFormatTypes.OBJECT, schema.getType());
        ObjectSchema objectSchema = schema.asObjectSchema();
        assertThat("Expect no properties", objectSchema.getProperties().keySet(), empty());
    }

    @Test
    public void testOptionalRequestParamsAndDefaultValues() {
        com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = schemaCreator.createForRequestParams(methodOn(CarController.class).configureKitCar(null, null));
        assertEquals("Expect object schema", JsonFormatTypes.OBJECT, schema.getType());
        ObjectSchema objectSchema = schema.asObjectSchema();
        assertThat("Expect properties", objectSchema.getProperties().keySet(), containsInAnyOrder("seats", "wheels"));
        assertNotEquals("Expect optional seats", true, objectSchema.getProperties().get("seats").getRequired());
        assertNotEquals("Expect optional wheels", true, objectSchema.getProperties().get("wheels").getRequired());
        assertEquals("Expect default value for wheels", "steel", objectSchema.getProperties().get("wheels").asSimpleTypeSchema().getDefault());
    }

    @Test
    public void testEnumParams() {
        com.fasterxml.jackson.module.jsonSchema.JsonSchema schema = schemaCreator.createForRequestParams(methodOn(CarController.class).configureCarRadio(CarController.RadioType.NONE));
        assertEquals("Expect object schema", JsonFormatTypes.OBJECT, schema.getType());
        ObjectSchema objectSchema = schema.asObjectSchema();
        assertThat("Expect 'type' property", objectSchema.getProperties().keySet(), hasItem("type"));
        assertEquals("Expect required", true, objectSchema.getProperties().get("type").getRequired());
    }

    @SuppressWarnings("WeakerAccess")
    @RequestMapping("/cars")
    public static class CarController {
        @RequestMapping("/saloon")
        CharSequence configureCar(@RequestParam("color") @JsonSchemaMetadata(title = "Paint colour", defaultValue = "Silver") String color) {
            return color;
        }

        @RequestMapping("/ford")
        CharSequence configureModelTForUser(Principal principal) {
            return "black";
        }

        @RequestMapping("/kit")
        CharSequence configureKitCar(@RequestParam(name = "seats", required = false) String seatMaterial, @RequestParam(name = "wheels", defaultValue = "steel") String wheelType) {
            return (StringUtils.isEmpty(seatMaterial)? "(no seats)" : seatMaterial) + "," + wheelType;
        }

        @RequestMapping("/radio")
        CharSequence configureCarRadio(@RequestParam(name = "type") RadioType radio) {
            return radio.toString();
        }

        enum RadioType {
            NONE,
            AM_FM,
            DAB
        }
    }

}
