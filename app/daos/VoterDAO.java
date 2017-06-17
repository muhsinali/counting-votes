package daos;

import com.mongodb.MongoClient;
import models.Candidates;
import models.Vote;
import models.Voter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateOpsImpl;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * This is the DAO that's used by the web app to interact with the database.
 */
public class VoterDAO {
  public Datastore datastore;   // TODO: temporarily exposed for it to be accessed in step definition
  public Jedis jedis;   // TODO: temporarily exposed for it to be accessed in step definition

  // MongoDB configuration details are injected by caller
  public VoterDAO(String host, int port, String databaseName){
    jedis = new Jedis();  // uses default settings

    MongoClient client = new MongoClient(host, port);
    datastore = new Morphia().map(Voter.class).createDatastore(client, databaseName);
    // Guarantees that the indexes specified in the Voter class are created during class mapping
    datastore.ensureIndexes();
  }

  public boolean castVote(Vote v){
    if(!Candidates.isValid(v.candidate)) return false;

    Voter voter = datastore.createQuery(Voter.class).field("username").equal(v.username).get();
    // Insert voter's candidate count if the voter hasn't previously voted
    if(voter == null){
      jedis.incr(v.candidate);
      datastore.save(Voter.build(v));
      return true;
    }
    // Update existing voter's candidate count
    return incrementVote(voter, v.candidate);
  }

//  public Map<String, Long> countAllVotesFromMongo(){
//    Map<String, Long> results = new HashMap<>();
//
//    for(String candidate : Candidates.electable) {
//      Iterator<VotingResult> it = datastore.createAggregation(Voter.class)
//          .project(Projection.projection(candidate))
//          .group("voteCount", Group.grouping("total", Group.sum(candidate)))
//          .aggregate(VotingResult.class);
//
//      // For a sum aggregation operation, only 1 result should be returned by the query.
//      // Therefore, only expect iterator to have 1 element.
//      if(it.hasNext()){
//        results.put(candidate, it.next().getTotal());
//      } else {
//        // For the case where no votes have been casted
//        results.put(candidate, 0L);
//      }
//    }
//    return results;
//  }

  public Map<String, Long> countAllVotes(){
    Map<String, Long> results = new HashMap<>();
    for(String candidate : Candidates.electable){
      long voteCount = jedis.exists(candidate) ? Long.parseLong(jedis.get(candidate)) : 0L;
      results.put(candidate, voteCount);
    }
    return results;
  }

  public long numVoters(){return datastore.getCount(Voter.class);}

  public void drop(){
    // Clear Redis
    Set<String> keys = jedis.keys("*");
    if(!keys.isEmpty()){
      jedis.del(keys.toArray(new String[keys.size()]));
    }

    // Drop collection in mongo database
    datastore.getDB().dropDatabase();
  }

  public Voter find(String username){
    return datastore.find(Voter.class).field("username").equal(username).get();
  }

  private boolean incrementVote(Voter voter, String candidate){
    // Checks to ensure that the candidate that the voter is voting for is valid and they have some votes remaining
    if(!Candidates.isValid(candidate) || !voter.anyVotesRemaining()){
      return false;
    }

    // Increment vote counts in Redis server
    jedis.incr(candidate);

    // Increments the vote count for the specified candidate by 1 within the database
    UpdateOperations<Voter> ops = new UpdateOpsImpl<>(Voter.class, new Mapper());
    ops.inc(candidate);
    return datastore.update(voter, ops).getWriteResult().isUpdateOfExisting();
  }
}
