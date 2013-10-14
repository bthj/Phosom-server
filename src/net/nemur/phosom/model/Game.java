package net.nemur.phosom.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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

import org.apache.lucene.document.Document;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.imageanalysis.FCTH;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

//@Entity
@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
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
	protected List<Challenge> challenges;
	
	
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
	
	
	
	public Challenge computeScoreFromResponseToChallenge( Long playerId ) throws IOException {
		Challenge challengeWithScore = null;
		for( Challenge oneChallenge : getChallenges() ) {
			if( playerId.equals(oneChallenge.getPlayerId()) ) {
				if( null != oneChallenge.getAssignmentBlobKey() 
						&& null != oneChallenge.getResponseBlobKey() ) {
					
					compareResponseToChallengeAndSetScore( oneChallenge );
					challengeWithScore = oneChallenge;
				}
			}
			break;
		}
		return challengeWithScore;
	}
	
	
	
	/**
	 * This is where backoff parameters are configured. Here it is aggressively
	 * retrying with backoff, up to 10 times but taking no more that 15 seconds
	 * total to do so.
	 */
	protected final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
			.initialRetryDelayMillis(10)
			.retryMaxAttempts(10)
			.totalRetryPeriodMillis(15000)
			.build());
	
	private void compareResponseToChallengeAndSetScore( Challenge challenge ) throws IOException {
		
		double[] challengeImageFeatureVector = getFCTHFeatureVectorFromFile(
				challenge.getAssignmentBucketName(), challenge.getAssignmentFileName() );
		double[] responseImageFeatureVector = getFCTHFeatureVectorFromFile(
				challenge.getResponseBucketName(), challenge.getResponseFileName() );
		double distanceBetweenChallengeAndResponse = 
				calculateEuclideanDistance(challengeImageFeatureVector, responseImageFeatureVector);
		
		challenge.setPoints( 
				calculateScoreFromDistanceBetweenImages(distanceBetweenChallengeAndResponse) );
	}
	private int calculateScoreFromDistanceBetweenImages( double distance ) {
		// let's assume the maximum distance is 10, though the distance can indeed be higher
		if( distance > 10.0 ) {
			distance = 10.0;
		}
		// the maximum score is 10 and let's subtract the distance from it
		//  to give the overall score:
		return (int) Math.round( 10 - distance );
	}
	
	private double[] getFCTHFeatureVectorFromFile( String bucketName, String fileName ) throws IOException {
		GcsFilename gcsFilename = new GcsFilename(bucketName, fileName);
		// Reading from Cloud Storage as in http://stackoverflow.com/a/18340201/169858
		// see other ways to read at https://code.google.com/p/appengine-gcs-client/source/browse/trunk/java/example/src/com/google/appengine/demos/LocalExample.java
		ReadableByteChannel rbc = gcsService.openReadChannel(gcsFilename, 0);
		InputStream inputStream = Channels.newInputStream(rbc);
		
		DocumentBuilder builder = DocumentBuilderFactory.getFCTHDocumentBuilder();
		Document doc = builder.createDocument(inputStream, fileName);
		inputStream.close();
		
		FCTH fcthDescriptor = new FCTH();
		fcthDescriptor.setByteArrayRepresentation(doc.getFields().get(0).binaryValue().bytes);
		
		return fcthDescriptor.getDoubleHistogram();
	}
    private static double calculateEuclideanDistance(double[] vector1, double[] vector2) {

        double innerSum = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            innerSum += Math.pow(vector1[i] - vector2[i], 2.0);
        }

        return Math.sqrt(innerSum);
    }
}
