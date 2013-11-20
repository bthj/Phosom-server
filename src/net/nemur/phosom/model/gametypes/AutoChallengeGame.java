package net.nemur.phosom.model.gametypes;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Random;
import java.util.logging.Logger;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import net.nemur.phosom.ImageServlet;
import net.nemur.phosom.auxiliary.CachePhosom;
import net.nemur.phosom.model.Challenge;
import net.nemur.phosom.model.Game;
import net.nemur.phosom.util.BlobUtil;
import net.sf.jsr107cache.Cache;

//@PersistenceCapable(identityType = IdentityType.APPLICATION)
@PersistenceCapable
@Inheritance(customStrategy = "complete-table")
public class AutoChallengeGame extends Game {
	private static final Logger log = Logger.getLogger(AutoChallengeGame.class.getName());
	
	
	
	private static final String FLICKR_API_KEY = "0cc84fc9654aaeca27ce2ee40a0cf574"; // TODO: environment variable or something!
	private static final int IMAGE_SIZE_ONE_DIMENSION = 600;
	
//	@PrimaryKey
//	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
//	private Key key;
	
	@Persistent 
	@Embedded(members = {
			@Persistent(name="challengePhotoUrl", columns=@Column(name="challengePhotoUrl")),
			@Persistent(name="challengeProfileUrl", columns=@Column(name="challengeProfileUrl")),
			@Persistent(name="challengeOwnerName", columns=@Column(name="challengeOwnerName"))
	})
	ChallengeInfo challengeInfo;
	
	
	@Persistent BlobKey challengePhotoBlobKey;
	@Persistent String challengeFileName;

	

	public Key getKey() {
		if( null == key ) {
			DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
			KeyRange keyRange = datastoreService.allocateIds("AutoChallengeGame", 1L);
			key = keyRange.getStart();
		}
		return key;
	}
	
//	public void allocateKey() {
//		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
//		KeyRange keyRange = datastoreService.allocateIds("AutoChallengeGame", 1L);
//		key = keyRange.getStart();
//	}
	
	public void populateAutoChallengeUrl() throws JSONException, IOException, InterruptedException {
		// place ID found with http://www.flickr.com/services/api/explore/flickr.places.find
		String placeId = "554890";
		String flickrRestUrl = getFlickrRestUrlForPlaceId(placeId);
		setRandomChallengeInfoFromFlicrRestResponse( flickrRestUrl );		
	}
	

	public void addPlayerToGame( Long playerId ) {
		// let's check if the player has already played this game
		Challenge challenge = null;
		for( Challenge oneChallenge : getChallenges() ) {
			if( playerId.equals(oneChallenge.getPlayerId()) ) {
				challenge = oneChallenge;
				
				if( null != challenge.getResponseBlobKey() ) {
					// let's delete already stored response from storage
					BlobstoreServiceFactory.getBlobstoreService().delete(
							challenge.getResponseBlobKey() );
					challenge.setResponseBlobKey(null);
					challenge.setResponseBucketName(null);
					challenge.setResponseFileName(null);
					challenge.setPoints(0);
				}
				break;
			}
		}
		if( null == challenge ) {
			challenge = new Challenge();
			
			challenge.setPlayerId(playerId);
			
			challenge.setAssignmentBlobKey(getChallengePhotoBlobKey());
			challenge.setAssignmentBucketName(BlobUtil.BUCKET_NAME_AUTO_CHALLENGE);
			challenge.setAssignmentFileName(getChallengeFileName());
			
			getChallenges().add(challenge);
		}
	}
	

	public void uploadChallengePhotoToCloudStorageAndSetBlobKey() throws IOException {
		String fileName = getFileNameFromUrlString( getChallengeInfo().getChallengePhotoUrl() );
//		GcsFilename gcsFilename = new GcsFilename(BUCKET_NAME_AUTO_CHALLENGE, fileName);
		
		// set a key to the Cloud Storage containing the challenge photo
		BlobKey blobKey = BlobUtil.getBlobKeyFromBucketAndFileName( 
				BlobUtil.BUCKET_NAME_AUTO_CHALLENGE, fileName );
		setChallengePhotoBlobKey(blobKey);
		setChallengeFileName(fileName);
		
		// get the URL to the challenge
//		URL challengeUrl = new URL( getChallengeUrl() );
//		HttpURLConnection httpConn = (HttpURLConnection) challengeUrl.openConnection();
//		httpConn.setConnectTimeout(15 * 1000);
//		httpConn.connect();
//		
//		// copy the file from URL to the Cloud Storage bucket:
//		GcsOutputChannel outputChannel =
//				gcsService.createOrReplace(gcsFilename, GcsFileOptions.getDefaultInstance());
//		copy( httpConn.getInputStream(), Channels.newOutputStream(outputChannel) );
		
		uploadPhotoFromUrlToCloudStorage(
				getChallengeInfo().getChallengePhotoUrl(), 
				BlobUtil.BUCKET_NAME_AUTO_CHALLENGE, 
				fileName );
	}
	
	public void uploadResponsePhotoFromUrlToCloudStorageAndSetBlobKey( 
			String url, String sourceUrl, String sourceTitle, Long playerId ) throws IOException {
		String fileName = getFileNameFromUrlString( url );
		
		BlobKey blobKey = BlobUtil.getBlobKeyFromBucketAndFileName(
				BlobUtil.BUCKET_NAME_CHALLENGE_RESPONSES, fileName);
		
		for( Challenge oneChallenge : getChallenges() ) {
			if( playerId.equals(oneChallenge.getPlayerId()) ) {
				oneChallenge.setResponseBlobKey(blobKey);
				oneChallenge.setResponseBucketName(BlobUtil.BUCKET_NAME_CHALLENGE_RESPONSES);
				oneChallenge.setResponseFileName(fileName);
				oneChallenge.setResponseSourceUrl( sourceUrl );
				oneChallenge.setResponseSourceTitle( sourceTitle );
				break;
			}
		}
		
		uploadPhotoFromUrlToCloudStorage(url, BlobUtil.BUCKET_NAME_CHALLENGE_RESPONSES, fileName);
	}


	
	private void uploadPhotoFromUrlToCloudStorage( String urlString, String bucketName, String fileName ) throws IOException {
		GcsFilename gcsFilename = new GcsFilename(bucketName, fileName);
		
		// get the URL
		URL url = new URL( urlString );
		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setConnectTimeout(30 * 1000);
		httpConn.connect();
		
		byte[] photoBytes = IOUtils.toByteArray(httpConn.getInputStream());
//		byte[] photoBytes = IOUtils.toByteArray(url);
		// resize photo
		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		Image originalImage = ImagesServiceFactory.makeImage(photoBytes);
		Transform resize = ImagesServiceFactory.makeResize(
				IMAGE_SIZE_ONE_DIMENSION, IMAGE_SIZE_ONE_DIMENSION);
		Image resizedImage = imagesService.applyTransform(resize, originalImage);
		
		// copy the file from URL to the Cloud Storage bucket:
		GcsOutputChannel outputChannel =
				gcsService.createOrReplace(gcsFilename, GcsFileOptions.getDefaultInstance());
//		copy( httpConn.getInputStream(), Channels.newOutputStream(outputChannel) );
		
		ByteArrayInputStream photoInputStream = new ByteArrayInputStream(
				resizedImage.getImageData());
		OutputStream photoOutputStream = Channels.newOutputStream(outputChannel);
		IOUtils.copy(photoInputStream, photoOutputStream);
		IOUtils.closeQuietly(photoInputStream);
		IOUtils.closeQuietly(photoOutputStream);
	}
	
	
	
	private String getFileNameFromUrlString( String urlString ) {
		return urlString.substring( urlString.lastIndexOf('/')+1, urlString.length() );
	}
	
	private String getFlickrRestUrlForPlaceId( String placeId ) {
		return "https://api.flickr.com/services/rest/?method=flickr.photos.search&place_id="+placeId+"&extras=original_format,description,geo,owner_name,place_url&format=json&nojsoncallback=1&api_key="+FLICKR_API_KEY;
	}
	
	private String getImageUrlFromFlickrPhotoObject( JSONObject flickrPhotoObject ) throws JSONException {
		
		int farm = flickrPhotoObject.getInt("farm");
		String server = flickrPhotoObject.getString("server");
		String photoId = flickrPhotoObject.getString("id");
		String secret = flickrPhotoObject.getString("secret");
		String format = "b";
		String fileType = "jpg";
		
		return "http://farm"+farm+".static.flickr.com/"+server+"/"+photoId+"_"+secret+"_"+format+"."+fileType;
	}
	private String getProfileImageUrlFromFlickrPhotoObject( JSONObject flickrPhotoObject ) {
		String ownerId = flickrPhotoObject.getString("owner");
		String photoId = flickrPhotoObject.getString("id");
		return "http://www.flickr.com/photos/" + ownerId + "/" + photoId;
	}
	private String getOwnerNameFromFlickrPhotoObject( JSONObject flickrPhotoObject ) {
		return flickrPhotoObject.getString( "ownername" );
	}
	
	private String getJsonFromUrl( String url ) throws MalformedURLException, IOException {
		log.info( "Fetching JSON from : " + url );
		
		StringBuilder stringBuilder = new StringBuilder();
		URL restUrl = new URL(url);
		HttpURLConnection httpConn = (HttpURLConnection) restUrl.openConnection();
		httpConn.setRequestMethod("GET");
		httpConn.setRequestProperty("Accept", "application/json");
		httpConn.setConnectTimeout(30 * 1000);
		httpConn.connect();
	
		BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
		String inputLine;
		while( (inputLine = in.readLine()) != null ) {
			stringBuilder.append(inputLine);
		}
		in.close();
		httpConn.disconnect();
		return stringBuilder.toString();
	}
	
	private void setRandomChallengeInfoFromFlicrRestResponse( String flickrRestUrl ) throws JSONException, IOException, InterruptedException {
		String challengeImageUrl = null;
		String challengeImageProfileUrl = null;
		String challengeImageOwnerName = null;
		
		Cache cache = CachePhosom.getInstance().getCache();
		String flickrPhotosJSON;
		if( cache.containsKey(flickrRestUrl) ) {
			flickrPhotosJSON = (String) cache.get(flickrRestUrl);
		} else {
			flickrPhotosJSON = getJsonFromUrl(flickrRestUrl);	
		}
		
		JSONObject jsonObject = null;
		for( int i=0; i < 5; i++ ) { // retry hack for when flickr api fails
			try {
				jsonObject = new JSONObject( flickrPhotosJSON );
				// this JSON seems to parse fine, put it into the cahce
				cache.put(flickrRestUrl, flickrPhotosJSON);
				break;
			} catch ( JSONException e ) {
				Thread.sleep(1000);
				log.info( "Caught error while parsing Flickr JSON, try again..." );
				flickrPhotosJSON = getJsonFromUrl(flickrRestUrl);
				continue;
			}
		}
		if( null != jsonObject ) {
			JSONObject photosJson = jsonObject.getJSONObject("photos");
			JSONArray photoArray = photosJson.getJSONArray("photo");
			
			int photoArrayIndex = randInt(0, photoArray.length());
			JSONObject photoObject = (JSONObject) photoArray.get(photoArrayIndex);
			
			challengeImageUrl = getImageUrlFromFlickrPhotoObject(photoObject);	
			challengeImageProfileUrl = getProfileImageUrlFromFlickrPhotoObject( photoObject );
			challengeImageOwnerName = getOwnerNameFromFlickrPhotoObject( photoObject );
		}
		ChallengeInfo challengeInfo = new ChallengeInfo();
		challengeInfo.setChallengePhotoUrl( challengeImageUrl );
		challengeInfo.setChallengeProfileUrl( challengeImageProfileUrl );
		challengeInfo.setChallengeOwnerName( challengeImageOwnerName );
		setChallengeInfo( challengeInfo );
	}


	
	/**  from http://stackoverflow.com/a/363692/169858
	 * Returns a psuedo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimim value
	 * @param max Maximim value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	private static int randInt(int min, int max) {

	    // Usually this can be a field rather than a method variable
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    //  so add 1 to make it inclusive
	    // ...no, not in this case // int randomNum = rand.nextInt((max - min) + 1) + min;
	    int randomNum = rand.nextInt((max - min)) + min;

	    return randomNum;
	}
	
	/**
	 * Used below to determine the size of chucks to read in. Should be > 1kb
	 * and < 10MB
	 */
	private static final int BUFFER_SIZE = 2 * 1024 * 1024;

	/**
	 * from
	 * https://code.google.com/p/appengine-gcs-client/source/browse/trunk/java/example/src/com/google/appengine/demos/GcsExampleServlet.java 
	 * Transfer the data from the inputStream to the outputStream. Then close both streams.
	 */
	private void copy(InputStream input, OutputStream output) throws IOException {
		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {
				output.write(buffer, 0, bytesRead);
				bytesRead = input.read(buffer);
			}
		} finally {
			input.close();
			output.close();
		}
	}
	
	
	@PersistenceCapable(identityType = IdentityType.DATASTORE, detachable = "true")
	@EmbeddedOnly
	public static class ChallengeInfo {

		@Persistent private String challengePhotoUrl;
		@Persistent private String challengeProfileUrl;
		@Persistent private String challengeOwnerName;
		
		public String getChallengePhotoUrl() {
			return challengePhotoUrl;
		}
		public void setChallengePhotoUrl( String challengePhotoUrl ) {
			this.challengePhotoUrl = challengePhotoUrl;
		}
		public String getChallengeProfileUrl() {
			return challengeProfileUrl;
		}
		public void setChallengeProfileUrl( String challengeProfileUrl ) {
			this.challengeProfileUrl = challengeProfileUrl;
		}
		public String getChallengeOwnerName() {
			return challengeOwnerName;
		}
		public void setChallengeOwnerName( String challengeOwnerName ) {
			this.challengeOwnerName = challengeOwnerName;
		}
	}
	
	
	public ChallengeInfo getChallengeInfo() {
		return challengeInfo;
	}

	public void setChallengeInfo( ChallengeInfo challengeInfo ) {
		this.challengeInfo = challengeInfo;
	}

	public BlobKey getChallengePhotoBlobKey() {
		return challengePhotoBlobKey;
	}

	public void setChallengePhotoBlobKey(BlobKey challengePhotoBlobKey) {
		this.challengePhotoBlobKey = challengePhotoBlobKey;
	}
	
	public String getChallengeFileName() {
		return challengeFileName;
	}

	public void setChallengeFileName(String challengeFileName) {
		this.challengeFileName = challengeFileName;
	}

}
