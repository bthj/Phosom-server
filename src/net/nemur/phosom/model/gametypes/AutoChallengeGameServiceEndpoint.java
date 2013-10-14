package net.nemur.phosom.model.gametypes;

import java.io.IOException;

import javax.inject.Named;
import javax.jdo.PersistenceManager;

import net.nemur.phosom.model.Challenge;
import net.nemur.phosom.model.PMF;

import org.json.JSONException;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.datastore.Key;

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
	
	@ApiMethod(name = "computeScoreFromResponseToChallenge")
	public Challenge computeScoreFromResponseToChallenge(
			@Named("gameId")Long gameId, 
			@Named("playerId")Long playerId ) throws IOException {
	
		AutoChallengeGame game = getAutoChallengeGame(gameId);
		Challenge challengeWithScore = game.computeScoreFromResponseToChallenge(playerId);
		
		new AutoChallengeGameEndpoint().updateAutoChallengeGame(game);
		return challengeWithScore;
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
}
