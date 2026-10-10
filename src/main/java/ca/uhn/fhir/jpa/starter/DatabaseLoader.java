package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirSystemDao;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class DatabaseLoader implements CommandLineRunner {

	@Autowired
	private DaoRegistry myDaoRegistry;

	@Autowired
	private FhirContext myFhirContext;

	@Override
	public void run(String... args) throws Exception {
		try {
			// Find the system bundle executor
			IFhirSystemDao<Bundle, ?> systemDao = myDaoRegistry.getSystemDao();

			// Read the JSON file from resources
			ClassPathResource resource = new ClassPathResource("preload-data.json");
			if (resource.exists()) {
				try (InputStream is = resource.getInputStream()) {
					Bundle bundle = myFhirContext.newJsonParser().parseResource(Bundle.class, is);

					// Post the transaction bundle to the database internally
					systemDao.transaction(null, bundle);
					System.out.println("====== AUTO-LOAD: Successfully loaded startup FHIR data ======");
				}
			}
		} catch (Exception e) {
			System.err.println("====== AUTO-LOAD FAILED: " + e.getMessage());
		}
	}
}
