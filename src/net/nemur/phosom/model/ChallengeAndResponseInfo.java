package net.nemur.phosom.model;

public class ChallengeAndResponseInfo {
	
	private Long parentGameId;
	private String challengePhotoUrl;
	private String responsePhotoUrl;
	private Integer score;
	private String gameInfo;
	private String playerName;

	
	public Long getParentGameId() {
		return parentGameId;
	}
	public void setParentGameId(Long parentGameId) {
		this.parentGameId = parentGameId;
	}
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
	public String getGameInfo() {
		return gameInfo;
	}
	public void setGameInfo(String gameInfo) {
		this.gameInfo = gameInfo;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

}
