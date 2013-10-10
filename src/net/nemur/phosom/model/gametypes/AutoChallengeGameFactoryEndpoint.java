package net.nemur.phosom.model.gametypes;

import java.io.IOException;

import org.json.JSONException;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "autoChallengeGameFactory", version = "v1")
public class AutoChallengeGameFactoryEndpoint {

	@ApiMethod(name = "createGame", httpMethod = "POST")
	public AutoChallengeGame createGame() throws JSONException, IOException {
		
		AutoChallengeGame autoChallengeGame = new AutoChallengeGame();
		autoChallengeGame.populateAutoChallengeUrl();
		autoChallengeGame.uploadChallengePhotoToCloudStorageAndSetBlobKey();
		
		AutoChallengeGameEndpoint gameEndpoint = new AutoChallengeGameEndpoint();
		return gameEndpoint.insertAutoChallengeGame(autoChallengeGame);
	}
}
