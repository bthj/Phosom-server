package net.nemur.phosom.model;

import javax.inject.Named;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;

/**
 * To be replaced by Google User authentication
 * @author bthj
 *
 */

@Api(name = "playerfactory", version = "v1")
public class PlayerFactoryEndpoint {
	
	@ApiMethod(name="createPlayerWithName", httpMethod = "POST")
	public Player createPlayerWithName( @Named("type") String name ) {
		
		Player player = new Player();
		player.setPlayerScreenName(name);
		
		PlayerEndpoint playerEndpoint = new PlayerEndpoint();
		return playerEndpoint.insertPlayer(player);
	}
}
