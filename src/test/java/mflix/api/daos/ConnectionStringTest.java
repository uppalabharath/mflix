package mflix.api.daos;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.SslSettings;

public class ConnectionStringTest {

	public static void main(String[] args) {

		MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString("mongodb+srv://mflixAppUser:mflixAppPwd@mflix-43fjf.mongodb.net/"))
				.build();
		MongoClient mongoClient = MongoClients.create(settings);

		SslSettings sslSettings = settings.getSslSettings();
		ReadPreference readPreference = settings.getReadPreference();
		ReadConcern readConcern = settings.getReadConcern();
		WriteConcern writeConcern = settings.getWriteConcern();
		
		System.out.println(sslSettings.isEnabled());
		System.out.println(readPreference.toString());
		System.out.println(readConcern.asDocument().toString());
		System.out.println(sslSettings.isInvalidHostNameAllowed());
		System.out.println(writeConcern.asDocument().toString());
	}

}
