import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
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

    // Extract familynames from file.
    final String familynamesFilePath = "src/main/resources/surnames.txt";
    final List<String> familynames = FamilynameReader
        .extractFamilynamesFromFile(familynamesFilePath);
    final long numberOfFamilynames = familynames.size();

    // Set number of iterations.
    final int numberOfRepeats = 3;

    // Set iterations with No Cache.
    final List<Integer> iterationsWithNoCache = new ArrayList<Integer>();
    iterationsWithNoCache.add(3);

    // Create a FHIR client
    FhirContext fhirContext = FhirContext.forR4();
    IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
    client.registerInterceptor(new LoggingInterceptor(false));
    client.registerInterceptor(new StopWatchInterceptor(numberOfFamilynames));

    loopQueryGroupsWithCacheControl(client, familynames, numberOfRepeats, iterationsWithNoCache);
  }

  /**
   * Loop query groups with cache control.
   */
  public static void loopQueryGroupsWithCacheControl(IGenericClient client,
      List<String> familynames, int numberOfRepeats, List<Integer> iterationsWithNoCache) {

    if (familynames == null || familynames.size() == 0) {
      System.out.println("No list of familynames is provided, terminating...");
      return;
    }

    // Interceptor for adding header (Cache-Control in particular)
    // to client requests.
    AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();

    for (int i = 1; i <= numberOfRepeats; i++) {
      if (iterationsWithNoCache != null && iterationsWithNoCache.contains(i)) {
        interceptor.addHeaderValue("Cache-Control", "no-cache");
        client.registerInterceptor(interceptor);
      }
      loopQueriesWithFamilynames(client, familynames, i);
      client.unregisterInterceptor(interceptor);
    }
  }

  /**
   * Extract patient records of all given familynames.
   */
  private static void loopQueriesWithFamilynames(IGenericClient client, List<String> familynames,
      int repeatCount) {

    System.out.println("======================================== Query Group " + repeatCount
        + " ========================================");

    if (familynames == null || familynames.size() == 0) {
      System.out.println("No familyname is given, proceeding with the next repeat");
    }

    for (String familyname : familynames) {
      extractPatientRecordsWithFamilyname(client, familyname);
    }
  }

  /**
   * Return a list of patient records, in which each patient is with familyname.
   */
  private static List<String> extractPatientRecordsWithFamilyname(IGenericClient client,
      String familyname) {

    // Search for Patient resources
    Bundle response = client.search().forResource("Patient")
        .where(Patient.FAMILY.matches().value(familyname)).returnBundle(Bundle.class).execute();

    ArrayList<String> patientRecords = new ArrayList<String>();

    for (BundleEntryComponent entry : response.getEntry()) {
      Patient patient = (Patient) entry.getResource();
      patientRecords.add(extractPatientRecord(patient));
    }

    // Sort by first name before return.
    patientRecords.sort(null);

    return patientRecords;
  }

  private static String extractPatientRecord(Patient patient) {
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

    return patientRecord;
  }

}
