package models;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.Property;

/**
 * Voter contains all the necessary information for a given voter, including their ID and the votes they've casted.
 */
@Entity("voters")
@Indexes({
    // Want this to be the unique id. Every voter should have a unique username
    @Index(fields = @Field("username"), options = @IndexOptions(unique = true)),

    // Create indexes for each candidate to the aggregation queries used to count votes for each candidate
    @Index(fields = @Field("A")),
    @Index(fields = @Field("B")),
    @Index(fields = @Field("C")),
    @Index(fields = @Field("D"))
})
public class Voter {
  public static final int NUM_VOTES_ALLOWED = 3;

  @Id
  private ObjectId id;


  @Property
  private String username;
  @Property
  private int A;
  @Property
  private int B;
  @Property
  private int C;
  @Property
  private int D;


  // Needed by Morphia for creating the class before populating its fields (Morphia only looks for the default constructor)
  public Voter(){

  }

  private Voter(String username, int A, int B, int C, int D){
    this.username = username;
    this.A = A;
    this.B = B;
    this.C = C;
    this.D = D;
  }

  // Voter needs to have casted at least 1 vote for them to be registered in the database
  public static Voter build(Vote v){
    switch(v.candidate){
      case "A": return new Voter(v.username, 1, 0, 0, 0);
      case "B": return new Voter(v.username, 0, 1, 0, 0);
      case "C": return new Voter(v.username, 0, 0, 1, 0);
      case "D": return new Voter(v.username, 0, 0, 0, 1);
    }
    return null;
  }

  public int numVotesCasted(){return A + B + C + D;}

  public boolean anyVotesRemaining() {
    return numVotesCasted() < NUM_VOTES_ALLOWED;
  }

}
