	package net.nemur.phosom.model;

import java.io.IOException;
import java.net.URL;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.persistence.EntityExistsException;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import net.nemur.phosom.model.gametypes.AutoChallengeGame;
import net.nemur.phosom.model.gametypes.GameTypes;
import net.nemur.phosom.model.gametypes.ManualChallengeDuelGame;
import net.nemur.phosom.model.gametypes.ManualChallengeGroupGame;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ImagesServiceFailureException;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.utils.SystemProperty;

@Api(name = "gameService", version = "v1")
public class GameServiceEndpoint {

//	@ApiMethod(name = "createGame", httpMethod = "POST")
//	public Game createGame( @Named("type") String type ) throws JSONException, IOException {
//		
//		Game gameToCreate = null;
//		
//		switch ( type ) {
//		case GameTypes.GAME_TYPE_AUTO_CHALLENGE:
//			gameToCreate = new AutoChallengeGame();
////			((AutoChallengeGame)gameToCreate).allocateKey();
//			((AutoChallengeGame)gameToCreate).populateAutoChallengeUrl();
//			((AutoChallengeGame)gameToCreate).uploadChallengePhotoToCloudStorageAndSetBlobKey();
//			break;
//			
//		case GameTypes.GAME_TYPE_MANUAL_CHALLENGE_DUEL_GAME:
//			gameToCreate = new ManualChallengeDuelGame();
//			break;
//			
//		case GameTypes.GAME_TYPE_MANUAL_CHALLENGE_GROUP_GAME:
//			gameToCreate = new ManualChallengeGroupGame();
//			break;
//
//		default:
//			break;
//		}
//		
////		GameEndpoint gameEndpoint = new GameEndpoint();
////		return gameEndpoint.insertGame(gameToCreate);
//		// TODO:  temporarily saving here inline to get the right kind of
//		//        incremental IDs:
//		PersistenceManager mgr = getPersistenceManager();
//		try {
//			mgr.makePersistent(gameToCreate);
//		} finally {
//			mgr.close();
//		}
//		return gameToCreate;
//	}
	
	@ApiMethod(name = "getChallengePhotoUrl", path="get_challenge_photo_url", httpMethod = HttpMethod.GET)
	public ChallengeAndResponseInfo getChallengePhotoUrl(
			@Named("bucket")String bucket, 
			@Named("filename")String filename,
			@Named("size")int size ) {
		
		String url = "";
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		BlobKey blobKey = blobstoreService.createGsBlobKey(
				"/gs/" + bucket + "/" + filename );
		
		url = getServingUrlFromBlobKey(blobKey, size);
		
		ChallengeAndResponseInfo challengeUrl = new ChallengeAndResponseInfo();
		challengeUrl.setChallengePhotoUrl(url);
		return challengeUrl;
	}
	

	
//	@ApiMethod(name = "addPlayerToGame" )
//	public Game addPlayerToGame( 
//			@Named("gameId")Long gameId, @Named("playerId")Long playerId ) {
//		
//		GameEndpoint gameEndpoint = new GameEndpoint();
//		
//		Game game = gameEndpoint.getGame(gameId);
//		Challenge challenge = new Challenge();
//		challenge.setPlayerId(playerId);
//		game.getChallenges().add(challenge);
//		
//		return gameEndpoint.updateGame(game);
//	}

	
	
	
	// TODO: this is duplicated in AutoChallengeGameServiceEndpoint !
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

}
