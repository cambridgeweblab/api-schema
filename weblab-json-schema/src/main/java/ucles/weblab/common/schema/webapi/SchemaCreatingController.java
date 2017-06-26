package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

public class SchemaCreatingController<Self extends SchemaCreatingController<Self>> {
    ResourceSchemaCreator schemaCreator;

    @Autowired
    void configureSchemaCreator(ResourceSchemaCreator schemaCreator) {
        this.schemaCreator = schemaCreator;
    }

    public ResourceSchemaCreator getSchemaCreator() {
        return schemaCreator;
    }

    public Self self() {
        //noinspection unchecked
        return (Self) methodOn((Class) getClass());
    }

    /**
     * Convenience function to update the links array in the schema with a copy of that array
     * with link appended.
     */
    protected void addALink(JsonSchema schema, LinkDescriptionObject link) {
        LinkDescriptionObject[] links = schema.asSimpleTypeSchema().getLinks();
        LinkDescriptionObject[] copy = Arrays.copyOf(links, links.length + 1);
        copy[links.length] = link;
        schema.asSimpleTypeSchema().setLinks(copy);
    }
}
