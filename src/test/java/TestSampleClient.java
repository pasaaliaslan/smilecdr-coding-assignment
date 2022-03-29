import static org.junit.Assert.assertTrue;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * SampleClient Unit Tests.
 */
public class TestSampleClient {

  private final String path1 = "src/main/resources/surnames.txt";
  private final List<String> familynames20 = FamilynameReader.extractFamilynamesFromFile(path1);

  private final String path3 = "src/main/resources/testSurnames1.txt";
  private final List<String> familynames1 = FamilynameReader.extractFamilynamesFromFile(path3);

  private final String path4 = "src/main/resources/testSurnames2.txt";
  private final List<String> familynames0 = FamilynameReader.extractFamilynamesFromFile(path4);

  private FhirContext fhirContext = FhirContext.forR4();
  private IGenericClient client0 = fhirContext
      .newRestfulGenericClient("http://hapi.fhir.org/baseR4");
  private IGenericClient client1 = fhirContext
      .newRestfulGenericClient("http://hapi.fhir.org/baseR4");
  private IGenericClient client20 = fhirContext
      .newRestfulGenericClient("http://hapi.fhir.org/baseR4");

  /**
   * Initialize the interceptors of client, before tests.
   */
  @Before
  public void registerInterceptors() {
    client0.registerInterceptor(new LoggingInterceptor(false));
    client0.registerInterceptor(new StopWatchInterceptor(0));

    client1.registerInterceptor(new LoggingInterceptor(false));
    client1.registerInterceptor(new StopWatchInterceptor(1));

    client20.registerInterceptor(new LoggingInterceptor(false));
    client20.registerInterceptor(new StopWatchInterceptor(20));
  }

  /**
   * Base of every unit test.
   */
  private List<Long> getAverageFinishTimes(int familynamesSize, int numberOfRepeats,
      List<Integer> iterationsWithNoCache) {

    IGenericClient client = this.client0;
    if (familynamesSize == 20) {
      client = client20;
    } else if (familynamesSize == 1) {
      client = client1;
    }

    List<String> familynames = this.familynames0;
    if (familynamesSize == 20) {
      familynames = familynames20;
    } else if (familynamesSize == 1) {
      familynames = familynames1;
    }

    // Hold stdout for after test.
    PrintStream stdout = new PrintStream(System.out);

    // Create new output stream for holding print data, and set System.out to it.
    ByteArrayOutputStream newConsole = new ByteArrayOutputStream();
    System.setOut(new PrintStream(newConsole));

    // Run the method.
    SampleClient.loopQueryGroupsWithCacheControl(client, familynames, numberOfRepeats,
        iterationsWithNoCache);

    // Reset System.out to stdout and client to have no interceptors.
    System.setOut(stdout);

    // Get print log.
    final String[] log = newConsole.toString().split("\n");

    List<Long> averageFinishTimes = new ArrayList<Long>();
    averageFinishTimes.add((long) 0);

    for (String line : log) {
      if (line.startsWith("Average Response Time:")) {
        averageFinishTimes
            .add(Long.parseLong(line.substring(line.indexOf("Average Response Time:") + 23)));
      }
    }

    return averageFinishTimes;
  }

  @Test
  public void testMinimumRequirement() {

    int familynamesSize = 20;
    int numberOfRepeats = 3;
    List<Integer> iterationsWithNoCache = new ArrayList<Integer>();
    iterationsWithNoCache.add(1);
    iterationsWithNoCache.add(3);

    List<Long> averageFinishTimes = getAverageFinishTimes(familynamesSize, numberOfRepeats,
        iterationsWithNoCache);

    assertTrue(compareAverageTimes(averageFinishTimes, iterationsWithNoCache));
  }

  @Test
  public void testMinimumRequirement2() {

    int familynamesSize = 20;
    int numberOfRepeats = 3;
    List<Integer> iterationsWithNoCache = new ArrayList<Integer>();
    iterationsWithNoCache.add(1);
    iterationsWithNoCache.add(3);

    List<Long> averageFinishTimes = getAverageFinishTimes(familynamesSize, numberOfRepeats,
        iterationsWithNoCache);

    assertTrue(compareAverageTimes(averageFinishTimes, iterationsWithNoCache));
  }

  @Test
  public void testNoFamilynames() {

    int familynamesSize = 0;
    int numberOfRepeats = 3;
    List<Integer> iterationsWithNoCache = new ArrayList<Integer>();
    iterationsWithNoCache.add(3);

    List<Long> averageFinishTimes = getAverageFinishTimes(familynamesSize, numberOfRepeats,
        iterationsWithNoCache);

    assertTrue(averageFinishTimes.size() == 1);
  }

  @Test
  public void testOneFamilyname() {

    int familynamesSize = 1;
    int numberOfRepeats = 3;
    List<Integer> iterationsWithNoCache = new ArrayList<Integer>();
    iterationsWithNoCache.add(1);
    iterationsWithNoCache.add(3);

    List<Long> averageFinishTimes = getAverageFinishTimes(familynamesSize, numberOfRepeats,
        iterationsWithNoCache);

    assertTrue(averageFinishTimes.size() == 4
        && compareAverageTimes(averageFinishTimes, iterationsWithNoCache));
  }

  @Test
  public void testNoRepeat() {

    int familynamesSize = 20;
    int numberOfRepeats = 0;
    List<Integer> iterationsWithNoCache = new ArrayList<Integer>();
    iterationsWithNoCache.add(3);

    List<Long> averageFinishTimes = getAverageFinishTimes(familynamesSize, numberOfRepeats,
        iterationsWithNoCache);

    assertTrue(averageFinishTimes.size() == 1);
  }

  @Test
  public void testOneRepeat() {

    int familynamesSize = 20;
    int numberOfRepeats = 1;
    List<Integer> iterationsWithNoCache = new ArrayList<Integer>();
    iterationsWithNoCache.add(3);

    List<Long> averageFinishTimes = getAverageFinishTimes(familynamesSize, numberOfRepeats,
        iterationsWithNoCache);

    assertTrue(averageFinishTimes.size() == 2);
  }

  @Test
  public void testCacheInMiddle() {

    int familynamesSize = 20;
    int numberOfRepeats = 10;
    List<Integer> iterationsWithNoCache = new ArrayList<Integer>();
    iterationsWithNoCache.add(3);
    iterationsWithNoCache.add(7);

    List<Long> averageFinishTimes = getAverageFinishTimes(familynamesSize, numberOfRepeats,
        iterationsWithNoCache);

    assertTrue(compareAverageTimes(averageFinishTimes, iterationsWithNoCache));
  }

  private boolean compareAverageTimes(List<Long> averageFinishTimes,
      List<Integer> iterationsWithNoCache) {

    boolean result = true;

    for (Integer iteration : iterationsWithNoCache) {
      for (int i = 0; i < averageFinishTimes.size(); i++) {
        if (!iterationsWithNoCache.contains(i)) {
          if (averageFinishTimes.get(iteration) < averageFinishTimes.get(i)) {
            return false;
          }
        }
      }
    }

    return result;
  }
}