package net.nemur.phosom.model;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.google.appengine.api.blobstore.BlobKey;

@PersistenceCapable
public class Assignment {

	@Persistent private Player player;
	@Persistent BlobKey assignmentBlobKey;
	@Persistent BlobKey responseBlobKey;
	@Persistent private int points;
	
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
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
