
package ucles.weblab.common.forms.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.HttpMethod;
import ucles.weblab.common.forms.domain.FormEntity;
import ucles.weblab.common.webapi.TitledLink;

import java.io.IOException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


/**
 *
 * @author Sukhraj
 */
public class FormResourceAssembler extends RepresentationModelAssemblerSupport<FormEntity, FormResource> {

    private static final Logger log = LoggerFactory.getLogger(FormResourceAssembler.class);

    private final ObjectMapper objectMapper;

    public FormResourceAssembler(ObjectMapper objectMapper) {
        super(FormController.class, FormResource.class);
        this.objectMapper = objectMapper;
    }


    @Override
    public FormResource toModel(FormEntity entity) {
        FormResource resource = instantiateModel(entity);
        resource.add(linkTo(methodOn(FormController.class).view(entity.getId())).withSelfRel());
        resource.add(new TitledLink(linkTo(methodOn(FormController.class).delete(entity.getId())),
                "delete", null, HttpMethod.DELETE.name()));

        return resource;
    }

    @Override
    protected FormResource instantiateModel(FormEntity entity) {

        JsonNode schemaAsNode = null;
        try {
            schemaAsNode = objectMapper.readTree(entity.getSchema());
        } catch (IOException ex) {
            log.error("Exception converting to jsonode", ex);
        }

        return new FormResource(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getApplicationName(),
                entity.getBusinessStreams(),
                schemaAsNode,
                entity.getValidFrom(),
                entity.getValidTo());
    }


}
