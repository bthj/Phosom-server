package net.nemur.phosom.model;

import javax.annotation.Nullable;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;

//@Entity
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Player {

//	@Id
//	private String playerId;
	
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key key;

	public Key getKey() {
		if( null == key ) {
			DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
			KeyRange keyRange = datastoreService.allocateIds("Player", 1L);
			key = keyRange.getStart();
		}
		return key;
	}
	
	@Persistent
	private String playerScreenName;
	
	@Nullable
	@Persistent
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPlayerScreenName() {
		return playerScreenName;
	}

	public void setPlayerScreenName(String playerScreenName) {
		this.playerScreenName = playerScreenName;
	}
	
	
//	@ElementCollection
//	private List<Player> friends;
//	@ElementCollection
//	private List<Game> games;
	
	
//	public String getPlayerId() {
//		return playerId;
//	}
//	public void setPlayerId(String playerId) {
//		this.playerId = playerId;
//	}
//	public String getPlayerScreenName() {
//		return playerScreenName;
//	}
//	public void setPlayerScreenName(String playerScreenName) {
//		this.playerScreenName = playerScreenName;
//	}
//	public List<Player> getFriends() {
//		return friends;
//	}
//	public void setFriends(List<Player> friends) {
//		this.friends = friends;
//	}
//	public List<Game> getGames() {
//		return games;
//	}
//	public void setGames(List<Game> games) {
//		this.games = games;
//	}
	
	
	
}
