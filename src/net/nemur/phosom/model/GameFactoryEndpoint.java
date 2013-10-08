package net.nemur.phosom.model;

import javax.inject.Named;

import net.nemur.phosom.model.gametypes.AutoChallengeGame;
import net.nemur.phosom.model.gametypes.GameTypes;
import net.nemur.phosom.model.gametypes.ManualChallengeDuelGame;
import net.nemur.phosom.model.gametypes.ManualChallengeGroupGame;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

@Api(name = "gamefactory", version = "v1")
public class GameFactoryEndpoint {

	@ApiMethod(name = "createGame", httpMethod = "POST")
	public Game createGame( @Named("type") String type ) {
		
		Game gameToCreate = null;
		
		switch ( type ) {
		case GameTypes.GAME_TYPE_AUTO_CHALLENGE:
			gameToCreate = new AutoChallengeGame();
			break;
			
		case GameTypes.GAME_TYPE_MANUAL_CHALLENGE_DUEL_GAME:
			gameToCreate = new ManualChallengeDuelGame();
			break;
			
		case GameTypes.GAME_TYPE_MANUAL_CHALLENGE_GROUP_GAME:
			gameToCreate = new ManualChallengeGroupGame();
			break;

		default:
			break;
		}
		
		GameEndpoint gameEndpoint = new GameEndpoint();
		return gameEndpoint.insertGame(gameToCreate);
	}
	
	public Game addPlayerToGame( 
			@Named("gameId")Long gameId, @Named("playerId")Long playerId ) {
		
		GameEndpoint gameEndpoint = new GameEndpoint();
		
		Game game = gameEndpoint.getGame(gameId);
		Challenge challenge = new Challenge();
		challenge.setPlayerId(playerId);
		game.getChallenges().add(challenge);
		
		return gameEndpoint.updateGame(game);
	}
}
