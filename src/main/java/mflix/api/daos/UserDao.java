package mflix.api.daos;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoWriteException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import mflix.api.models.Session;
import mflix.api.models.User;

@Configuration
public class UserDao extends AbstractMFlixDao {

	private final MongoCollection<User> usersCollection;
	private final MongoCollection<Session> sessionsCollection;

	private final Logger log;

	@Autowired
	public UserDao(MongoClient mongoClient, @Value("${spring.mongodb.database}") String databaseName) {
		super(mongoClient, databaseName);
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				fromProviders(PojoCodecProvider.builder().automatic(true).build()));

		usersCollection = db.getCollection("users", User.class).withCodecRegistry(pojoCodecRegistry);
		log = LoggerFactory.getLogger(this.getClass());
		sessionsCollection = db.getCollection("sessions", Session.class).withCodecRegistry(pojoCodecRegistry);
	}

	/**
	 * Inserts the `user` object in the `users` collection.
	 *
	 * @param user - User object to be added
	 * @return True if successful, throw IncorrectDaoOperation otherwise
	 */
	public boolean addUser(User user) {
		/*
		 * User oldUser = usersCollection.find(Filters.eq("email",
		 * user.getEmail())).first(); if (oldUser != null) { throw new
		 * IncorrectDaoOperation("User already esists"); }
		 */
		try {			
			usersCollection.withWriteConcern(WriteConcern.MAJORITY).insertOne(user);
			return true;
		} catch(MongoWriteException wrEx) {
			throw new IncorrectDaoOperation("Some error occurred while updating comment"+wrEx.getError().getCategory());
		}

	}

	/**
	 * Creates session using userId and jwt token.
	 *
	 * @param userId - user string identifier
	 * @param jwt    - jwt string token
	 * @return true if successful
	 */
	public boolean createUserSession(String userId, String jwt) {
		
		Bson updateFilter = new Document("user_id", userId);
	    Bson setUpdate = Updates.set("jwt", jwt);
	    UpdateOptions options = new UpdateOptions().upsert(true);
	    try {
	    	sessionsCollection.updateOne(updateFilter, setUpdate, options);
		    return true;
	    } catch (MongoWriteException wrEx) {
	    	throw new IncorrectDaoOperation("Some error occurred while updating comment"+wrEx.getError().getCategory());
	    }
	    
	}

	/**
	 * Returns the User object matching the an email string value.
	 *
	 * @param email - email string to be matched.
	 * @return User object or null.
	 */
	public User getUser(String email) {
		return usersCollection.find(new Document("email", email)).first();
	}

	/**
	 * Given the userId, returns a Session object.
	 *
	 * @param userId - user string identifier.
	 * @return Session object or null.
	 */
	public Session getUserSession(String userId) {
		return sessionsCollection.find(Filters.eq("user_id", userId)).first();
	}

	public boolean deleteUserSessions(String userId) {
		Document sessionDeleteFilter = new Document("user_id", userId);
	    DeleteResult res = sessionsCollection.deleteOne(sessionDeleteFilter);
	    return res.wasAcknowledged();
	}

	/**
	 * Removes the user document that match the provided email.
	 *
	 * @param email - of the user to be deleted.
	 * @return true if user successfully removed
	 */
	public boolean deleteUser(String email) {
		if (deleteUserSessions(email)) {
			Document userDeleteFilter = new Document("email", email);
			try {
				DeleteResult res = usersCollection.deleteOne(userDeleteFilter);
				return res.wasAcknowledged();
			} catch (MongoWriteException wrEx) {
				throw new IncorrectDaoOperation("Some error occurred while updating comment"+wrEx.getError().getCategory());
			}
			
		}
		return false;
	}

	/**
	 * Updates the preferences of an user identified by `email` parameter.
	 *
	 * @param email           - user to be updated email
	 * @param userPreferences - set of preferences that should be stored and replace
	 *                        the existing ones. Cannot be set to null value
	 * @return User object that just been updated.
	 */
	public boolean updateUserPreferences(String email, Map<String, ?> userPreferences) {
		User user = usersCollection.find(Filters.eq("email", email)).iterator().tryNext();
		if (userPreferences == null) {
			throw new IncorrectDaoOperation("Prefs should not be null");
		}
		final Map<String, String> oldPrefs = user.getPreferences() != null ? user.getPreferences() :  new HashMap<String, String>();
		userPreferences.keySet().forEach((String key) -> {
			oldPrefs.put(key, (String)userPreferences.get(key));
		});
		try {
			UpdateResult result = usersCollection.updateOne(new Document("email", email), Updates.set("preferences", oldPrefs));
			return result.wasAcknowledged();
		} catch (MongoWriteException wrEx) {
			throw new IncorrectDaoOperation("Some error occurred while updating comment"+wrEx.getError().getCategory());
		}
		
	}
}
