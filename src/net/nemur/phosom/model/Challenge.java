package net.nemur.phosom.model;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.blobstore.BlobKey;

@PersistenceCapable
public class Challenge {

	@Persistent private Long playerId;
	@Persistent BlobKey assignmentBlobKey;
	@Persistent BlobKey responseBlobKey;
	@Persistent private int points;
	
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
}
