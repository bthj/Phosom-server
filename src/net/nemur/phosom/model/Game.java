package net.nemur.phosom.model;

import java.util.ArrayList;
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
//@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public class Game {

//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Key id;
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	protected Key key;
	
	
	@Nullable
	@Persistent(embeddedElement = "true", defaultFetchGroup = "true") // as in http://stackoverflow.com/a/7095821/169858
//	@Embedded //DataNucleus complains about this
	private List<Challenge> challenges;
	
	
	// TODO: DATE
	
	public Key getKey() {
		if( null == key ) {
			DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
			KeyRange keyRange = datastoreService.allocateIds("Game", 1L);
			key = keyRange.getStart();
		}
		return key;
	}
	

	public List<Challenge> getChallenges() {
		if( null == challenges ) {
			challenges = new ArrayList<Challenge>();
		}
		return challenges;
	}
	public void setChallenges(List<Challenge> challenges) {
		this.challenges = challenges;
	}
	
	
}
