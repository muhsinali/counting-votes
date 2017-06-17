import models.Candidates;
import models.Vote;
import models.Voter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class VoterTest {

  @Test
  public void buildShouldOnlyReturnANewVoterIfCandidateProvidedIsValid(){
    for(String candidate : Candidates.electable){
      assertTrue(Voter.build(new Vote("validCandidate", candidate)) != null);
    }
  }

  @Test
  public void buildShouldReturnNullIfCandidateIsInvalid(){
    assertTrue(Voter.build(new Vote("voter", "invalidCandidate")) == null);
  }
}
