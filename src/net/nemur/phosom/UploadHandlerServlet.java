package net.nemur.phosom;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.nemur.phosom.model.Challenge;
import net.nemur.phosom.model.gametypes.AutoChallengeGame;
import net.nemur.phosom.model.gametypes.AutoChallengeGameEndpoint;
import net.nemur.phosom.util.BlobUtil;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

public class UploadHandlerServlet extends HttpServlet {

	@Override
	public void doPost( HttpServletRequest req, HttpServletResponse resp )
			throws ServletException, IOException {
		
		String gameIdString = req.getParameter( "gameid" );
		String playerIdString = req.getParameter( "playerid" );

		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		
		Map<String, List<BlobKey>> blobs = blobstoreService.getUploads( req );
		List<BlobKey> keys = blobs.get("photo");
		
		Map<String, List<BlobInfo>> blobInfos = blobstoreService.getBlobInfos( req );
		List<BlobInfo> infos = blobInfos.get( "photo" );
		
		
		boolean succeeded = false;
		if( null != keys && keys.size() > 0 ) {
			Long gameId = Long.parseLong( gameIdString );
			Long playerId = Long.parseLong( playerIdString );
			AutoChallengeGameEndpoint gameEndpoint = new AutoChallengeGameEndpoint();
			AutoChallengeGame game = gameEndpoint.getAutoChallengeGame( gameId );
			String fileName;
			if( null != infos && infos.size() > 0 ) {
				fileName = infos.get( 0 ).getFilename();
			} else {
				fileName = gameId + "_" + String.valueOf(System.currentTimeMillis());
			}
			BlobKey blobKey = keys.get( 0 );
			for( Challenge oneChallenge : game.getChallenges() ) {
				if( playerId.equals( oneChallenge.getPlayerId() ) ) {
					oneChallenge.setResponseBlobKey( blobKey );
					oneChallenge.setResponseBucketName( BlobUtil.BUCKET_NAME_CHALLENGE_RESPONSES );
					oneChallenge.setResponseFileName( fileName );
					oneChallenge.setResponseSourceUrl( null );
					oneChallenge.setResponseSourceTitle( null );
					break;
				}
			}
			gameEndpoint.updateAutoChallengeGame( game );
			succeeded = true;
		}
		if( succeeded ) {
			resp.setStatus( HttpServletResponse.SC_OK );
		} else {
			resp.sendError( 400, "Photo uplaod cannot be handled" );
		}
	}
}
