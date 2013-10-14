package net.nemur.phosom.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable
public class Challenge {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key playerKey;
	
	@Persistent private Long playerId;
	
	@Persistent private BlobKey assignmentBlobKey;
	@Persistent private String assignmentBucketName; // silly that you can't get this from the BlobKey!
	@Persistent private String assignmentFileName;
	
	@Persistent private BlobKey responseBlobKey;
	@Persistent private String responseBucketName;
	@Persistent private String responseFileName;
	
	@Persistent private int points;
	
	public Key getPlayerKey() {
		return playerKey;
	}
	public void setPlayerKey(Key playerKey) {
		this.playerKey = playerKey;
	}
	
	public Long getPlayerId() {
		return playerId;
	}
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}
	public BlobKey getAssignmentBlobKey() {
		return assignmentBlobKey;
	}
	public void setAssignmentBlobKey(BlobKey assignmentBlobKey) {
		this.assignmentBlobKey = assignmentBlobKey;
	}
	public BlobKey getResponseBlobKey() {
		return responseBlobKey;
	}
	public void setResponseBlobKey(BlobKey responseBlobKey) {
		this.responseBlobKey = responseBlobKey;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	
	public String getAssignmentBucketName() {
		return assignmentBucketName;
	}
	public void setAssignmentBucketName(String assignmentBucketName) {
		this.assignmentBucketName = assignmentBucketName;
	}
	public String getAssignmentFileName() {
		return assignmentFileName;
	}
	public void setAssignmentFileName(String assignmentFileName) {
		this.assignmentFileName = assignmentFileName;
	}
	public String getResponseBucketName() {
		return responseBucketName;
	}
	public void setResponseBucketName(String responseBucketName) {
		this.responseBucketName = responseBucketName;
	}
	public String getResponseFileName() {
		return responseFileName;
	}
	public void setResponseFileName(String responseFileName) {
		this.responseFileName = responseFileName;
	}	
}
