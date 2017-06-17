import models.Candidates;
import models.Vote;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class VoteTest {

  @Test
  public void checkVote(){
    String username = "voter";
    String candidate = Candidates.electable[0];
    Vote vote = new Vote(username, candidate);
    assertEquals(username + " " + candidate, vote.toString());
  }
}
