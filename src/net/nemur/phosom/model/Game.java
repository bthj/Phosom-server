package net.nemur.phosom.model;

import java.util.List;

import javax.annotation.Nullable;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;

//@Entity
@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public class Game {

//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Key id;
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Nullable
	@Persistent
	@ElementCollection
	private List<Long> players;
	
	@Nullable
	@Persistent
	@Embedded
	private List<Challenge> challenges;
	
	
	public Long getId() {
		if( null == id ) {
			DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
			KeyRange keyRange = datastoreService.allocateIds("Game", 1L);
			id = keyRange.getStart().getId();
		}
		return id;
	}
	
	public List<Long> getPlayers() {
		return players;
	}
	public void setPlayers(List<Long> players) {
		this.players = players;
	}
	public List<Challenge> getChallenges() {
		return challenges;
	}
	public void setChallenges(List<Challenge> challenges) {
		this.challenges = challenges;
	}
	
	
}
