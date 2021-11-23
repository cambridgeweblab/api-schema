package ucles.weblab.common.schema.webapi;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.types.LinkDescriptionObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


public class SchemaCreatingController<C extends SchemaCreatingController<C>> {
    ResourceSchemaCreator schemaCreator;

    @Autowired
    void configureSchemaCreator(ResourceSchemaCreator schemaCreator) {
        this.schemaCreator = schemaCreator;
    }

    public ResourceSchemaCreator getSchemaCreator() {
        return schemaCreator;
    }

    public C self() {
        //noinspection unchecked
        return (C) methodOn((Class) getClass());
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
