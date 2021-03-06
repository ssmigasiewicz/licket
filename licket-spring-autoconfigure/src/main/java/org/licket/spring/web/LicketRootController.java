package org.licket.spring.web;

import org.licket.core.LicketApplication;
import org.licket.core.resource.ByteArrayResource;
import org.licket.core.resource.Resource;
import org.licket.core.view.ComponentContainerView;
import org.licket.core.view.container.LicketComponentContainer;
import org.licket.spring.resource.ResourcesStorage;
import org.licket.surface.SurfaceContext;
import org.licket.surface.tag.ElementFactories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

/**
 * @author activey
 */
@Controller
@RequestMapping("/")
public class LicketRootController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicketRootController.class);

    @Autowired
    private LicketApplication licketApplication;

    @Autowired
    private ResourcesStorage resourcesStorage;

    @Autowired
    private ElementFactories surfaceElementFactories;

    @PostConstruct
    private void initialize() {
        // TODO refactor whole method
        LOGGER.debug("Initializing licket application: {}.", licketApplication.getName());

        LicketComponentContainer<?> rootContainer = licketApplication.getRootComponentContainer();
        ComponentContainerView containerView = rootContainer.getComponentContainerView();
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        new SurfaceContext(surfaceElementFactories).processTemplateContent(containerView.readViewContent(), byteArrayStream);
        resourcesStorage
                .putResource(new ByteArrayResource("index.html", TEXT_HTML_VALUE, byteArrayStream.toByteArray()));
    }


    @GetMapping(value = "/index", produces = TEXT_HTML_VALUE)
    public @ResponseBody
    ResponseEntity<InputStreamResource> generateRootHtml() {
        Optional<Resource> resourceOptional = resourcesStorage.getResource("index.html");
        if (!resourceOptional.isPresent()) {
            return status(NOT_FOUND).contentLength(0).body(null);
        }
        Resource resource = resourceOptional.get();
        return ok()
                .contentType(parseMediaType(resource.getMimeType()))
                .body(new InputStreamResource(resource.getStream()));
    }
}
