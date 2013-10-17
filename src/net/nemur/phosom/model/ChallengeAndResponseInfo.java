package net.nemur.phosom.model;

public class ChallengeAndResponseInfo {
	
	private String challengePhotoUrl;
	private String responsePhotoUrl;
	private Integer score;

	public String getChallengePhotoUrl() {
		return challengePhotoUrl;
	}
	public void setChallengePhotoUrl(String challengePhotoUrl) {
		this.challengePhotoUrl = challengePhotoUrl;
	}
	public String getResponsePhotoUrl() {
		return responsePhotoUrl;
	}
	public void setResponsePhotoUrl(String responsePhotoUrl) {
		this.responsePhotoUrl = responsePhotoUrl;
	}
	public Integer getScore() {
		return score;
	}
	public void setScore(Integer score) {
		this.score = score;
	}
}
