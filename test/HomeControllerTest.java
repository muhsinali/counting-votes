import com.fasterxml.jackson.databind.JsonNode;
import controllers.HomeController;
import controllers.routes;
import models.Candidates;
import models.Voter;
import org.junit.Before;
import org.junit.Test;
import play.Configuration;
import play.Environment;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.FORBIDDEN;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.route;


public class HomeControllerTest extends WithApplication {
  private HomeController homeController;

  private JsonNode asJsonNode(String username, String candidate){
    return Json.parse("{\"username\": \"" + username + "\", \"candidate\": \"" + candidate + "\"}");
  }

  private int createCastVotePostRequest(String username, String candidate){
    Http.RequestBuilder request = new Http.RequestBuilder()
        .method("POST")
        .bodyJson(asJsonNode(username, candidate))
        .uri(routes.HomeController.castVote().url());
    Result result = route(request);
    return result.status();
  }



  @Before
  public void setUp(){
    homeController = new HomeController(
        app.injector().instanceOf(Configuration.class),
        app.injector().instanceOf(Environment.class)
    );
  }

  @Test
  public void castVoteShouldReturn201StatusCodeForNewVoter() throws IOException, InterruptedException {
    String username = UUID.randomUUID().toString();
    assertEquals(CREATED, createCastVotePostRequest(username, Candidates.electable[0]));
  }

  @Test
  public void castVoteShouldReturn403StatusCodeWhenVoterHasUsedUpAllVotes(){
    String username = UUID.randomUUID().toString();
    for(int i = 0; i < Voter.NUM_VOTES_ALLOWED; i++){
      assertEquals(CREATED, createCastVotePostRequest(username, Candidates.electable[i]));
    }
    assertEquals(FORBIDDEN, createCastVotePostRequest(username, Candidates.electable[0]));
  }

  @Test
  public void castVoteShouldReturn404StatusCodeForInvalidCandidate(){
    String username = UUID.randomUUID().toString();
    assertEquals(NOT_FOUND, createCastVotePostRequest(username, "invalidCandidate"));
  }

  @Test
  public void pollResultsShouldReturn200StatusCode() {
    Result result = homeController.pollResults();
    assertEquals(OK, result.status());
  }

}
