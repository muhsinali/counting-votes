import daos.VoterDAO;
import models.Candidates;
import models.Vote;
import models.Voter;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.Configuration;
import play.test.Helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class VoterDAOTest {
  private static VoterDAO voterDAO;

  @BeforeClass
  public static void beforeAllTests(){
    Application fakeApp = Helpers.fakeApplication();
    Configuration config = fakeApp.injector().instanceOf(Configuration.class);
    voterDAO = new VoterDAO(
        config.getString("mongodb.host"),
        config.getInt("mongodb.port"),
        config.getString("mongodb.test.database")
    );
  }

  @After
  public void afterTest(){
    voterDAO.drop();
  }


  @Test
  public void castVoteShouldReturnTrueIfVoterSuccessfullyCastsVoteForValidCandidate(){
    assertTrue(voterDAO.castVote(new Vote("test", "A")));
  }

  @Test
  public void castVoteShouldReturnFalseIfVoterAttemptsToVoteForInvalidCandidate(){
    assertFalse(voterDAO.castVote(new Vote("test", "invalidCandidate")));
  }

  @Test
  public void incrementVoteShouldReturnFalseForInvalidCandidate()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method method = VoterDAO.class.getDeclaredMethod("incrementVote", Voter.class, String.class);
    method.setAccessible(true);
    boolean result = (boolean) method.invoke(voterDAO, Voter.build(new Vote("test", "A")), "Z");
    assertFalse(result);
    method.setAccessible(false);
  }

  @Test
  public void incrementVoteShouldReturnFalseIfVoterHasUsedUpAllVotes()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    String username = UUID.randomUUID().toString();
    voterDAO.castVote(new Vote(username, "A"));

    Method method = VoterDAO.class.getDeclaredMethod("incrementVote", Voter.class, String.class);
    method.setAccessible(true);
    assertTrue((boolean) method.invoke(voterDAO, voterDAO.find(username), "A"));
    assertTrue((boolean) method.invoke(voterDAO, voterDAO.find(username), "A"));
    assertFalse((boolean) method.invoke(voterDAO, voterDAO.find(username), "A"));
    method.setAccessible(false);
  }

  @Test
  public void countAllVotesShouldReturnVoteCountsForAllCandidates(){
    String voterOne = "voterOne";
    String voterTwo = "voterTwo";

    assertTrue(voterDAO.castVote(new Vote(voterOne, "A")));
    assertTrue(voterDAO.castVote(new Vote(voterOne, "B")));
    assertTrue(voterDAO.castVote(new Vote(voterOne, "C")));

    assertTrue(voterDAO.castVote(new Vote(voterTwo, "A")));
    assertTrue(voterDAO.castVote(new Vote(voterTwo, "B")));
    assertTrue(voterDAO.castVote(new Vote(voterTwo, "B")));

    Map<String, Long> results = voterDAO.countAllVotes();
    assertEquals(2, results.get("A").longValue());
    assertEquals(3, results.get("B").longValue());
    assertEquals(1, results.get("C").longValue());
    assertEquals(0, results.get("D").longValue());
  }

  @Test
  public void dropShouldRemoveAllDocumentsInCollection(){
    for(String candidate : Candidates.electable){
      assertTrue(voterDAO.castVote(new Vote(UUID.randomUUID().toString(), candidate)));
    }
    assertEquals(Candidates.electable.length, voterDAO.numVoters());
    voterDAO.drop();
    assertEquals(0, voterDAO.numVoters());
  }

  @Test
  public void findShouldReturnVoterWithSpecifiedUsername(){
    String username = "voter";
    voterDAO.castVote(new Vote(username, "A"));
    assertTrue(voterDAO.find(username) != null);
  }

  @Test
  public void findShouldReturnNullForNonExistentVoter(){
    assertTrue(voterDAO.find("nonExistentVoter") == null);
  }
}
