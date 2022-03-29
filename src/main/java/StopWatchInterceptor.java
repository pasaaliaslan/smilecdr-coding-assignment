import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import java.io.IOException;

/**
 * Stopwatch Interceptor.
 */
@Interceptor
public class StopWatchInterceptor implements IClientInterceptor {

  private long totalTime;
  private long numberOfRequests;
  private long numberOfRequestsInLoop;

  /**
   * Stopwatch Interceptor Constructor.
   */
  public StopWatchInterceptor(long numberOfRequestsInLoop) {
    super();
    this.totalTime = 0;
    this.numberOfRequests = -1;
    this.numberOfRequestsInLoop = numberOfRequestsInLoop;
  }

  @Override
  @Hook(value = Pointcut.CLIENT_REQUEST)
  public void interceptRequest(IHttpRequest theRequest) {
  }

  @Override
  @Hook(value = Pointcut.CLIENT_RESPONSE)
  public void interceptResponse(IHttpResponse theResponse) throws IOException {
    long timeTakenForOneRequest = theResponse.getRequestStopWatch().getMillis();
    numberOfRequests++;

    if (numberOfRequests != 0) {
      totalTime += timeTakenForOneRequest;
    }

    if (this.numberOfRequestsInLoop == this.numberOfRequests) {
      this.getAverageTime();
      this.resetStopwatch();
    }
  }

  private void getAverageTime() {
    System.out.println();
    System.out.println("Total Time: " + totalTime);
    System.out.println("Total Number of Requests: " + numberOfRequests);
    System.out.println("Average Response Time: " + (totalTime / numberOfRequests));
    System.out.println();
  }

  private void resetStopwatch() {
    this.totalTime = 0;
    this.numberOfRequests = 0;
  }

}
