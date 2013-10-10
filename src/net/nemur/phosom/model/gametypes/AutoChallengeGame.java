package net.nemur.phosom.model.gametypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.Random;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

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
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import net.nemur.phosom.model.Game;

//@PersistenceCapable(identityType = IdentityType.APPLICATION)
@PersistenceCapable
@Inheritance(customStrategy = "complete-table")
public class AutoChallengeGame extends Game {
	
	private static final String FLICKR_API_KEY = "0cc84fc9654aaeca27ce2ee40a0cf574"; // TODO: environment variable or something!
	private static final String BUCKET_NAME_AUTO_CHALLENGE = "auto-challenge-photos";
	
//	@PrimaryKey
//	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
//	private Key key;
	
	@Persistent String challengeUrl;
	@Persistent BlobKey challengePhotoBlobKey;
	

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
	
	public void populateAutoChallengeUrl() throws JSONException, IOException {
		// place ID found with http://www.flickr.com/services/api/explore/flickr.places.find
		String placeId = "554890";
		String flickrRestUrl = getFlickrRestUrlForPlaceId(placeId);
		setChallengeUrl( getRandomImageUrlFromFlicrRestResponse(flickrRestUrl) );		
	}
	
	/**
	 * This is where backoff parameters are configured. Here it is aggressively
	 * retrying with backoff, up to 10 times but taking no more that 15 seconds
	 * total to do so.
	 */
	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
			.initialRetryDelayMillis(10)
			.retryMaxAttempts(10)
			.totalRetryPeriodMillis(15000)
			.build());

	public void uploadChallengePhotoToCloudStorageAndSetBlobKey() throws IOException {
		String fileName = getFileNameFromUrlString( getChallengeUrl() );
		GcsFilename gcsFilename = new GcsFilename(BUCKET_NAME_AUTO_CHALLENGE, fileName);
		
		// set a key to the Cloud Storage containing the challenge photo
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		BlobKey blobKey = blobstoreService.createGsBlobKey( 
				"/gs/" + BUCKET_NAME_AUTO_CHALLENGE + "/" + fileName );
		setChallengePhotoBlobKey(blobKey);
		
		// get the URL to the challenge
		URL challengeUrl = new URL( getChallengeUrl() );
		HttpURLConnection httpConn = (HttpURLConnection) challengeUrl.openConnection();
		httpConn.setConnectTimeout(15 * 1000);
		httpConn.connect();
		
		// copy the file from URL to the Cloud Storage bucket:
		GcsOutputChannel outputChannel =
				gcsService.createOrReplace(gcsFilename, GcsFileOptions.getDefaultInstance());
		copy( httpConn.getInputStream(), Channels.newOutputStream(outputChannel) );
	}

	
	private String getFileNameFromUrlString( String urlString ) {
		return urlString.substring( urlString.lastIndexOf('/')+1, urlString.length() );
	}
	
	private String getFlickrRestUrlForPlaceId( String placeId ) {
		return "http://api.flickr.com/services/rest/?method=flickr.photos.search&place_id="+placeId+"&extras=original_format,tags,description,geo,date_upload,owner_name,place_url&format=json&nojsoncallback=1&api_key="+FLICKR_API_KEY;
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
	private String getStringFromUrl( String url ) throws MalformedURLException, IOException {
		// TODO: Use memcache
		StringBuilder stringBuilder = new StringBuilder();
		URL restUrl = new URL(url);
		HttpURLConnection httpConn = (HttpURLConnection) restUrl.openConnection();
		httpConn.setConnectTimeout(15 * 1000);
		httpConn.connect();
	
		BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
		String inputLine;
		while( (inputLine = in.readLine()) != null ) {
			stringBuilder.append(inputLine);
		}
		in.close();
		return stringBuilder.toString();
	}
	private String getRandomImageUrlFromFlicrRestResponse( String flickrRestUrl ) throws JSONException, IOException {
			
		JSONObject jsonObject = new JSONObject( getStringFromUrl(flickrRestUrl) );
		JSONObject photosJson = jsonObject.getJSONObject("photos");
		JSONArray photoArray = photosJson.getJSONArray("photo");
		
		int photoArrayIndex = randInt(0, photoArray.length());
		JSONObject photoObject = (JSONObject) photoArray.get(photoArrayIndex);
		
		return getImageUrlFromFlickrPhotoObject(photoObject);
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
	
	
	
	public String getChallengeUrl() {
		return challengeUrl;
	}
	public void setChallengeUrl(String challengeUrl) {
		this.challengeUrl = challengeUrl;
	}
	public BlobKey getChallengePhotoBlobKey() {
		return challengePhotoBlobKey;
	}

	public void setChallengePhotoBlobKey(BlobKey challengePhotoBlobKey) {
		this.challengePhotoBlobKey = challengePhotoBlobKey;
	}

}
