package stepDefinitions;

import akka.stream.Materializer;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.routes;
import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import daos.VoterDAO;
import models.Voter;
import net.sf.ehcache.CacheManager;
import play.Application;
import play.Configuration;
import play.core.j.JavaResultExtractor;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.route;

public class HomeControllerSteps implements En {
  private Application fakeApp;
  private VoterDAO voterDAO;
  //private int statusCode;
  private Materializer materializer;

  // Used in the 4th scenario
  private Map<String, Integer> voteCounts;
  private Result result;
  private long responseTimeMillis;



  // Helper methods
  private JsonNode asJsonNode(String username, String candidate){
    return Json.parse("{\"username\": \"" + username + "\", \"candidate\": \"" + candidate + "\"}");
  }

  private void createCastVotePostRequest(String username, String candidate){
    Http.RequestBuilder request = new Http.RequestBuilder()
        .method("POST")
        .bodyJson(asJsonNode(username, candidate))
        .uri(routes.HomeController.castVote().url());
    result = route(request);
  }


  // Ensures that scenarios are kept independent
  @Before
  public void beforeScenario(){
    fakeApp = Helpers.fakeApplication();
    Helpers.start(fakeApp);
  }
  @After
  public void afterScenario(){
    voterDAO.drop();
    CacheManager.getInstance().shutdown();    // forces cache shutdown after every scenario
    Helpers.stop(fakeApp);
  }


  public HomeControllerSteps(){
    Given("^I am counting votes$", () -> {
      Configuration config = fakeApp.injector().instanceOf(Configuration.class);
      materializer = fakeApp.injector().instanceOf(Materializer.class);
      voterDAO = new VoterDAO(
          config.getString("mongodb.host"),
          config.getInt("mongodb.port"),
          config.getString("mongodb.test.database")
      );
    });

    When("^I receive a vote for candidate \"([^\"]*)\" from voter \"([^\"]*)\"$", (String candidate, String voter) ->
      createCastVotePostRequest(voter, candidate)
    );

    When("^voter \"([^\"]*)\" has not voted before$", (String voter) -> {
      Voter v = voterDAO.find(voter);
      assertEquals(1, v.numVotesCasted());
    });

    Then("^I register that vote and return a (\\d+) response$", (Integer statusCode) ->
        assertEquals(statusCode, new Integer(result.status()))
    );

    Given("^I have received (\\d+) votes for candidate \"([^\"]*)\" from voter \"([^\"]*)\"$",
        (Integer numVotes, String candidate, String voter) ->
          IntStream.range(0, numVotes).forEach(i -> {
            createCastVotePostRequest(voter, candidate);
            assertEquals(CREATED, result.status());
          })
    );

    Then("^I return a (\\d+) response$", (Integer statusCode) -> assertEquals(statusCode, new Integer(result.status())));

    Then("^I do not register that vote from voter \"([^\"]*)\"$", (String voter) -> {
      Voter v = voterDAO.find(voter);
      assertEquals(Voter.NUM_VOTES_ALLOWED, v.numVotesCasted());
    });


    Given("^I have received (\\d+) votes for (\\d+) candidates$", (Long numVotesCasted, Integer numCandidates) -> {
      // No implementation needed here
    });


    Given("^the votes are split:$", (DataTable dataTable) -> {
      voteCounts = dataTable.asMap(String.class, Integer.class);
      voteCounts.forEach((key, value) -> voterDAO.jedis.set(key, Long.toString(value)));
    });

    When("^I receive a request for the overall result$", () -> {
      long startTime = System.currentTimeMillis();
      Http.RequestBuilder request = new Http.RequestBuilder().uri(routes.HomeController.pollResults().url());
      result = route(request);
      long endTime = System.currentTimeMillis();
      responseTimeMillis = endTime - startTime;
    });

    Then("^I return the correct result$", () -> {
      assertEquals("pollResults() should return a 200 HTTP status code", OK, result.status());
      // Deliberately using .get() here because want test to fail if value is not present
      assertEquals("Content should be in JSON", "application/json", result.contentType().get());
      assertEquals("Content should be encoded in UTF-8", "UTF-8", result.charset().get());

      // Check if content is correct
      ByteString bs = JavaResultExtractor.getBody(result, TimeUnit.SECONDS.toMillis(5L), materializer);
      JsonNode expected = Json.toJson(voteCounts);
      JsonNode actual = Json.parse(bs.toArray());
      assertEquals(expected, actual);   // uses the .equals() method internally
    });

    Then("^the response time is under (\\d+) second$", (Integer numSeconds) ->
        assertTrue(responseTimeMillis < TimeUnit.SECONDS.toMillis(numSeconds))
    );
  }
}
