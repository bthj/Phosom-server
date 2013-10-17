package net.nemur.phosom.model.gametypes;

import java.io.IOException;
import java.net.URL;

import javax.inject.Named;
import javax.jdo.PersistenceManager;

import net.nemur.phosom.model.Challenge;
import net.nemur.phosom.model.ChallengeAndResponseInfo;
import net.nemur.phosom.model.Game;
import net.nemur.phosom.model.GameEndpoint;
import net.nemur.phosom.model.PMF;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.utils.SystemProperty;

// TODO: add client ID verification http://cloud-endpoints-slides.appspot.com/gdl_2012_08_08.html#8
@Api(name = "autoChallengeGameService", version = "v1")
public class AutoChallengeGameServiceEndpoint {

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
	public ChallengeAndResponseInfo getChallengeAndResponseInfo(
			@Named("gameId") Long gameId, 
			@Named("playerId") Long playerId,
			@Named("size") int size ) throws JSONException, IOException {
		
		ChallengeAndResponseInfo photoInfo = new ChallengeAndResponseInfo();
		
		AutoChallengeGame game = getAutoChallengeGame(gameId);
		Challenge challenge = null;
		for( Challenge oneChallenge : game.getChallenges() ) {
			if( playerId.equals(oneChallenge.getPlayerId()) ) {
				challenge = oneChallenge;
				break;
			}
		}
		if( null != challenge ) {
			String challengePhotoUrl = getServingUrlFromBlobKey(
					challenge.getAssignmentBlobKey(), size);
			String responsePhotoUrl = getServingUrlFromBlobKey(
					challenge.getResponseBlobKey(), size);
			int score = getScoreFromImageUrls(challengePhotoUrl, responsePhotoUrl);
			
			photoInfo.setChallengePhotoUrl( challengePhotoUrl );
			photoInfo.setResponsePhotoUrl( responsePhotoUrl );
			photoInfo.setScore( score );
			
			challenge.setPoints(score);
		}
		
		AutoChallengeGameEndpoint gameEndpoint = new AutoChallengeGameEndpoint();
		gameEndpoint.updateAutoChallengeGame(game);
		
		return photoInfo;
	}
	
	
//	@ApiMethod(name = "computeScoreFromResponseToChallenge")
//	public Challenge computeScoreFromResponseToChallenge(
//			@Named("gameId")Long gameId, 
//			@Named("playerId")Long playerId ) throws IOException {
//	
//		AutoChallengeGame game = getAutoChallengeGame(gameId);
//		Challenge challengeWithScore = game.computeScoreFromResponseToChallenge(playerId);
//		
//		new AutoChallengeGameEndpoint().updateAutoChallengeGame(game);
//		return challengeWithScore;
//	}
	
	
	
	/**
	 * Cloned from AutoChallengeGameEndpoint with the addition of eagerly
	 * fetching Challenge entities
	 * 
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getAutoChallengeGame")
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
		URL analysisUrl = new URL( getImageAnalysisUrlString(url1, url2) );
		JSONObject similarityResult = new JSONObject( 
				IOUtils.toString(analysisUrl, "ISO-8859-1") );
		double distance = 10 * similarityResult.getDouble("distance");
		if( distance > 10.0 ) {
			distance = 10.0;
		}
		// the maximum score is 10 and let's subtract the distance from it
		//  to give the overall score:
		return (int) Math.round( 10 - distance );
	}
}
