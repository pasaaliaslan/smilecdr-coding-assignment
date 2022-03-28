import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Patient;

/**
 * SampleClient Class.
 */
public class SampleClient {

  /**
   * main method.
   */
  public static void main(String[] theArgs) {

    // Create a FHIR client
    FhirContext fhirContext = FhirContext.forR4();
    IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
    client.registerInterceptor(new LoggingInterceptor(false));

    // Search for Patient resources
    Bundle response = client.search().forResource("Patient")
        .where(Patient.FAMILY.matches().value("SMITH")).returnBundle(Bundle.class).execute();

    List<String> patientRecords = extractPatientRecords(response);

    System.out.println(patientRecords);
  }

  private static List<String> extractPatientRecords(Bundle response) {
    ArrayList<String> patientRecords = new ArrayList<String>();

    for (BundleEntryComponent entry : response.getEntry()) {
      Patient patient = (Patient) entry.getResource();

      String patientRecord = "";

      // Parse Name
      try {
        patientRecord += patient.getName().get(0).getGivenAsSingleString();
      } catch (Exception e) {
        patientRecord += "[No Name]";
      }

      patientRecord += " ";

      // Parse Family Name
      try {
        patientRecord += patient.getName().get(0).getFamily();
      } catch (Exception e) {
        patientRecord += "[No Family Name]";
      }

      patientRecord += " / ";

      // Parse BirthDate
      try {
        patientRecord += patient.getBirthDate().toString();
      } catch (Exception e) {
        patientRecord += "[No Birth Date]";
      }

      patientRecords.add(patientRecord);
    }

    // Sort by first name before return.
    patientRecords.sort(null);

    return patientRecords;
  }

}
