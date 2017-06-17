import models.Candidates;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class CandidatesTest {
  @Test
  public void isValidShouldReturnTrueIfProposedCandidateIsElectable(){
    assertTrue(Candidates.isValid("A"));
  }

  @Test
  public void isValidShouldReturnFalseIfProposedCandidateIsNotRunningForElection(){
    assertFalse(Candidates.isValid("invalidCandidate"));
  }
}
