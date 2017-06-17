[![Build Status](https://travis-ci.org/muhsinali/counting-votes.svg?branch=master)](https://travis-ci.org/muhsinali/counting-votes)
Counting Votes
=================================
This is a RESTful web service that allows users to cast a maximum of 3 votes from a selection of candidates. There are 4 candidates that are running for election. The user can view the live voting results at any time (served in JSON format).

See [below](https://github.com/muhsinali/counting-votes/blob/master/README.md#how-to-run-the-web-app) on how to set up and use this web service. 


### Key features:
#### General
- Implemented in a [TDD](https://github.com/muhsinali/counting-votes/blob/7c637c730f06494a4bc6ca533568f3ce504be5ef/test/VoterDAOTest.java) & [BDD](https://github.com/muhsinali/counting-votes/blob/master/test/resources/features/HomeController.feature) fashion using JUnit and Cucumber; code coverage is ~95%
- Used [runtime dependency injection](https://github.com/muhsinali/counting-votes/blob/2426397301b306840ec6809cc506a034aafd31ef/app/controllers/HomeController.java#L21-L29)
- [Configuration settings](https://github.com/muhsinali/counting-votes/blob/f11e7354275dfea6bc80a88220ee4f6b7ee2496d/conf/application.conf#L13-L17) are stored in `conf/application.conf`

#### Performance
- Utilised [indexes](https://github.com/muhsinali/counting-votes/blob/7c637c730f06494a4bc6ca533568f3ce504be5ef/app/models/Voter.java#L17-L25) and covered queries in mongo database
- Used [Redis to serve vote counts](https://github.com/muhsinali/counting-votes/blob/7c637c730f06494a4bc6ca533568f3ce504be5ef/app/daos/VoterDAO.java#L71-L78) - accessing in-memory DB is much faster than looping over the all votes stored in a Mongo database as they're stored on disk
- Ensured that updates made to any existing voter's voting preferences are made directly in the database. Saves having to pull the document, convert to a Java object, update, convert back to a Mongo document, and insert it back into the database (and removing the old document).
- Added a [Gzip compression filter](https://github.com/muhsinali/counting-votes/blob/7c637c730f06494a4bc6ca533568f3ce504be5ef/app/Filters.java) to Gzip server responses


### Tech stack
- Java 8
- Play framework 2.5.14
- Redis (Jedis 2.9.0)
- Mongo (Morphia 1.3.2)
- JUnit 4.12 for unit tests
- Cucumber 1.2.5 for feature tests


### How to run the web app
To run the web service locally, start the mongo daemon process using `mongod`, and start the default redis server using `redis-server`.
Then go to the root directory of this project and run `activator run`. Once ready, go to [http://localhost:9000](http://localhost:9000) (if running for the first time, will need to wait 30s - 60s for the source code to compile).


### Using the web app (example commands):
#### To view results:
```bash
curl http://localhost:9000/
```

#### To cast a vote:
```bash
curl --header "Content-type: application/json" \
 --request POST \
  --data '{"username": "muhsinali", "candidate": "A"}' \
   http://localhost:9000/
```




