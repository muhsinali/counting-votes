package models;

/* Represents a vote that a given voter has casted. Intended to be immutable, as a vote
should not change once received.
 */
public class Vote {
  public final String username;
  public final String candidate;

  public Vote(final String username, final String candidate){
    this.username = username;
    this.candidate = candidate;
  }


  @Override
  public String toString(){return username + " " + candidate;}
}
