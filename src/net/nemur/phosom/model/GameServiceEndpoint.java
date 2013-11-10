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
	
	
	// TODO: this is duplicated in AutoChallengeGameServiceEndpoint !
	private String getServingUrlFromBlobKey(BlobKey blobKey, int size) {
		String url;
		ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
		if( size > 1600 ) size = 1600;
		options.imageSize( size );
		options.crop(false);
		options.secureUrl(true);
		try {
			url = ImagesServiceFactory.getImagesService().getServingUrl(options);
		} catch( ImagesServiceFailureException e ) {
			url = "";
			// TODO: log...
		}
		return url;
	}

}
