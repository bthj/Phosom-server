package net.nemur.phosom.model.gametypes;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import net.nemur.phosom.model.Challenge;
import net.nemur.phosom.model.ChallengeAndResponseInfo;
import net.nemur.phosom.model.PMF;
import net.nemur.phosom.model.Player;
import net.nemur.phosom.model.PlayerEndpoint;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.utils.SystemProperty;

// TODO: add client ID verification http://cloud-endpoints-slides.appspot.com/gdl_2012_08_08.html#8
@Api(name = "autoChallengeGameService", version = "v1")
public class AutoChallengeGameServiceEndpoint {
	
	private static final int LISTVIEW_IMAGE_SQUARE_SIZE = 80;

	@ApiMethod(name = "createGame", httpMethod = "POST")
	public AutoChallengeGame createGame() throws JSONException, IOException {
		
		AutoChallengeGame autoChallengeGame = new AutoChallengeGame();
		autoChallengeGame.populateAutoChallengeUrl();
		autoChallengeGame.uploadChallengePhotoToCloudStorageAndSetBlobKey();
		
		AutoChallengeGameEndpoint gameEndpoint = new AutoChallengeGameEndpoint();
		return gameEndpoint.insertAutoChallengeGame(autoChallengeGame);
	}
	
	@ApiMethod(name = "addPlayerToGame" )
	public AutoChallengeGame addPlayerToGame(
			@Named("gameId")Long gameId, @Named("playerId")Long playerId ) {
		
		AutoChallengeGame game = getAutoChallengeGame(gameId);
		
		game.addPlayerToGame(playerId);
		
		AutoChallengeGameEndpoint gameEndpoint = new AutoChallengeGameEndpoint();
		return gameEndpoint.updateAutoChallengeGame(game);
	}
	
	@ApiMethod(name = "respondToChallengeWithUrl")
	public AutoChallengeGame respondToChallengeWithUrl( 
			@Named("gameId")Long gameId, 
			@Named("playerId")Long playerId, 
			@Named("url")String url ) throws IOException {
		
		AutoChallengeGame game = getAutoChallengeGame(gameId);
		game.uploadResponsePhotoFromUrlToCloudStorageAndSetBlobKey(url, playerId);

		AutoChallengeGameEndpoint gameEndpoint = new AutoChallengeGameEndpoint();
		return gameEndpoint.updateAutoChallengeGame(game);
	}
	
	@ApiMethod(name = "getChallengeAndResponseInfo", path="get_challenge_and_response_info", httpMethod = HttpMethod.GET)
	public List<ChallengeAndResponseInfo> getChallengeAndResponseInfo(
			@Named("gameId") Long gameId, 
			@Named("playerId") Long playerId,
			@Named("size") int size ) throws JSONException, IOException {
		
		List<ChallengeAndResponseInfo> challengesInfo = new ArrayList<ChallengeAndResponseInfo>();
		
		AutoChallengeGame game = getAutoChallengeGame(gameId);
		
		for( Challenge oneChallenge : game.getChallenges() ) {
			
			ChallengeAndResponseInfo playerPhotoInfo = 
					getInfoFromChallenge(oneChallenge, size);
			
			if( playerId.equals(oneChallenge.getPlayerId()) ) {
				if( 0 == oneChallenge.getPoints() ) {
					// if zero points / score, let's assume it hasn't been calculated, but
					//  it can indeed have been calculated as zero but then we'll just calculate again
					int score = getScoreFromImageUrls(
							playerPhotoInfo.getChallengePhotoUrl(), 
							playerPhotoInfo.getResponsePhotoUrl());
					playerPhotoInfo.setScore( score ); // to be returned here
					oneChallenge.setPoints(score); // to be saved along with the game
				}
			} else {
				// let's get the player's name as it's not the current logged in player
				PlayerEndpoint playerEndpoint = new PlayerEndpoint();
				Player player = playerEndpoint.getPlayer(oneChallenge.getPlayerId());
				playerPhotoInfo.setPlayerName(player.getPlayerScreenName());
			}
			challengesInfo.add(playerPhotoInfo);
		}
		
		AutoChallengeGameEndpoint gameEndpoint = new AutoChallengeGameEndpoint();
		gameEndpoint.updateAutoChallengeGame(game);
		
		return challengesInfo;
	}
	
	@SuppressWarnings("unchecked")
	@ApiMethod(name = "listChallengesPlayedByPlayer", path="list_challenges_played_by_player", httpMethod = HttpMethod.GET)
	public List<ChallengeAndResponseInfo> listGamesPlayedByPlayer( @Named("playerId") Long playerId ) {
		List<ChallengeAndResponseInfo> challengesInfo = new ArrayList<ChallengeAndResponseInfo>();
		List<Challenge> queryResults = null;
		
		PersistenceManager pm = getPersistenceManager();
		Query q = pm.newQuery(Challenge.class);
		q.setFilter("playerId == playerIdParam");
		q.declareParameters("Long playerIdParam");
		try {
			queryResults = (List<Challenge>) q.execute(playerId);
			for( Challenge oneChallenge : queryResults ) {
				ChallengeAndResponseInfo oneChallengeInfo = new ChallengeAndResponseInfo();
				oneChallengeInfo.setParentGameId(oneChallenge.getKey().getParent().getId());
				oneChallengeInfo.setGameInfo( 
						"Game #" + oneChallenge.getKey().getParent().getId() + 
						" - score: " + oneChallenge.getPoints());
				oneChallengeInfo.setScore( oneChallenge.getPoints() );
				setUrlsFromChallengeBlobsToChallengeInfo(
						oneChallenge, oneChallengeInfo, LISTVIEW_IMAGE_SQUARE_SIZE);
				challengesInfo.add(oneChallengeInfo);
			}
		} finally {
			q.closeAll();
		}
		return challengesInfo;
	}
	
	/**
	 * Cloned from AutoChallengeGameEndpoint with the addition of eagerly
	 * fetching Challenge entities
	 * 
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getAutoChallengeGame", path="get_game", httpMethod = HttpMethod.GET)
	public AutoChallengeGame getAutoChallengeGame(@Named("id") Long id) {
		PersistenceManager mgr = getPersistenceManager();
		AutoChallengeGame autochallengegame = null;
		try {
			autochallengegame = mgr.getObjectById(AutoChallengeGame.class, id);
			// let's eagerly fetch all challenges before closing the connection
			for( Challenge oneChallenge : autochallengegame.getChallenges() )
				;
		} finally {
			mgr.close();
		}
		return autochallengegame;
	}
	
	

	private void setUrlsFromChallengeBlobsToChallengeInfo(
			Challenge oneChallenge, ChallengeAndResponseInfo oneChallengeInfo, int size) {
		if( null != oneChallenge.getAssignmentBlobKey() ) {
			oneChallengeInfo.setChallengePhotoUrl(
					getServingUrlFromBlobKey(
							oneChallenge.getAssignmentBlobKey(), size) );	
		}
		if( null != oneChallenge.getResponseBlobKey() ) {
			oneChallengeInfo.setResponsePhotoUrl(
					getServingUrlFromBlobKey(
							oneChallenge.getResponseBlobKey(), size) );	
		}
	}
	
	private ChallengeAndResponseInfo getInfoFromChallenge( Challenge challenge, int size ) {
		
		ChallengeAndResponseInfo info = new ChallengeAndResponseInfo();
		
		setUrlsFromChallengeBlobsToChallengeInfo(challenge, info, size);
		info.setScore( challenge.getPoints() );
		
		return info;
	}
	
	private static PersistenceManager getPersistenceManager() {
		return PMF.get().getPersistenceManager();
	}
	
	
	
	private String getServingUrlFromBlobKey(BlobKey blobKey, int size) {
		String url;
		ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
		options.imageSize( size );
		options.crop(false);
		try {
			url = ImagesServiceFactory.getImagesService().getServingUrl(options);
		} catch( ImagesServiceFailureException e ) {
			url = "";
			// TODO: log...
		}
		return url;
	}
	
	private String getImageAnalysisUrlString( String url1, String url2 ) {
		String host; // TODO: from config...
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
			host = "http://image.phosom.nemur.net";
		} else {
			host = "http://localhost:8080";
		}
		return host + "/pia/analyze/similarity?url1=" + url1 + "&url2=" + url2;
	}
	private int getScoreFromImageUrls( String url1, String url2 ) throws JSONException, IOException {
		int score = 0;
		if( null != url1 && null != url2 ) {
			URL analysisUrl = new URL( getImageAnalysisUrlString(url1, url2) );
			JSONObject similarityResult = new JSONObject( 
					IOUtils.toString(analysisUrl, "ISO-8859-1") );
			double distance = 1000 * similarityResult.getDouble("distance");
			if( distance > 1000.0 ) {
				distance = 1000.0;
			}
			// the maximum score is 10 and let's subtract the distance from it
			//  to give the overall score:
			score = (int) Math.round( 1000 - distance );	
		}
		return score;
	}
}
