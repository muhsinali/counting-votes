package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import daos.VoterDAO;
import models.Candidates;
import models.Vote;
import play.Configuration;
import play.Environment;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * HomeController contains the entry points of this web application and handles all HTTP requests.
 */
public class HomeController extends Controller {
  private VoterDAO voterDAO;

  @Inject
  public HomeController(Configuration config, Environment env){
    voterDAO = new VoterDAO(
        config.getString("mongodb.host"),
        config.getInt("mongodb.port"),
        // separates test data by placing it in a different Mongo collection
        config.getString(env.isTest() ? "mongodb.test.database" : "mongodb.database")
    );
  }


  // Requires the voter's unique username and the candidate they want to vote for.
  public Result castVote(){
    JsonNode json = request().body().asJson();
    if(json == null){
      return badRequest("Invalid data provided. To vote, please provide your username and the candidate you wish to " +
          "vote for.");
    }
    // TODO need to somehow verify that JSON in body is valid
    Vote vote = new Vote(json.findPath("username").textValue(), json.findPath("candidate").textValue());

    // Check for valid candidate. If not found, then provide a list of possible candidates.
    if(!Candidates.isValid(vote.candidate)){
      return notFound("Candidate" + vote.candidate + " is not listed as one of the electable candidates.\n" +
          "Please select from one of the following: " + Candidates.asString());
    }

    boolean wasVoteCasted = voterDAO.castVote(vote);
    if(wasVoteCasted) {
      return created(vote.username + " has voted for candidate " + vote.candidate);
    } else {
      return forbidden(vote.username + " has reached the maximum number of possible votes");
    }
  }


  /*
  Returns the voting results for all candidates. Retrieves vote counts from server-side cache (i.e. Redis).

  This saves having to go into the mongo database and serve the live result every time a request is received. This
  improves the performance of the web app esp. when hit with a 1000's of requests (e.g. when the election is over and
  everybody wants to find out who won)
   */
  public Result pollResults(){
    return ok(Json.toJson(voterDAO.countAllVotes()));
  }

}
