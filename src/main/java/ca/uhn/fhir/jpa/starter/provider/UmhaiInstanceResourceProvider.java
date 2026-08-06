package ca.uhn.fhir.jpa.starter.provider;

import ca.uhn.fhir.jpa.starter.custom.UmhaiInstance;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
public class UmhaiInstanceResourceProvider implements IResourceProvider {

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return UmhaiInstance.class;
    }

    @Read
    public UmhaiInstance read(@IdParam IdType theId) {
        // Metadatalink hook placeholder
        return null;
    }

    @Search
    public List<UmhaiInstance> searchAll() {
        return Collections.emptyList();
    }
}