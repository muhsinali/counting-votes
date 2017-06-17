package models;

import org.bson.types.ObjectId;

/**
 * This class is needed to store the counted votes for a given candidate when performing an aggregation query using
 * Morphia.
 */
public class VotingResult {
  private ObjectId id;  // required by Morphia for grouping operation in aggregation query
  private long total;

  // No setter, therefore 'total' field is read only
  public long getTotal(){return total;}
}
