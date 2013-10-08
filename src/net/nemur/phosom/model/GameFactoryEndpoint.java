package net.nemur.phosom.model;

import javax.annotation.Nullable;
import javax.inject.Named;

import net.nemur.phosom.model.gametypes.AutoChallengeMultiPlayerGame;
import net.nemur.phosom.model.gametypes.AutoChallengeSinglePlayerGame;
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
		case GameTypes.GAME_TYPE_AUTO_CHALLENGE_SINGLE_PLAYER:
			gameToCreate = new AutoChallengeSinglePlayerGame();
			// TODO: fetch challenge photo
			break;
			
		case GameTypes.GAME_TYPE_AUTO_CHALLENGE_MULTI_PLAYER:
			gameToCreate = new AutoChallengeMultiPlayerGame();
			// TODO: fetch challenge photo
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

//		Challenge challenge = new Challenge();
//		for( Player onePlayer : players ) {
//			gameToCreate.getPlayers().add( onePlayer.getKey().getId() );
//			
//			Assignment assignment = new Assignment();
//			assignment.setPlayer( onePlayer );
//			challenge.getAssignments().add(assignment);
//		}
//		gameToCreate.getChallenges().add(challenge);
		
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
