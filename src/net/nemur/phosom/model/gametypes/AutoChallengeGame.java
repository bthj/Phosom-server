package net.nemur.phosom.model.gametypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;

import net.nemur.phosom.model.Game;

//@PersistenceCapable(identityType = IdentityType.APPLICATION)
@PersistenceCapable
@Inheritance(customStrategy = "complete-table")
public class AutoChallengeGame extends Game {
	
	private static final String FLICKR_API_KEY = "0cc84fc9654aaeca27ce2ee40a0cf574"; // TODO: environment variable or something!
	
	@Persistent
	String challengeUrl;
	
//	public AutoChallengeGame() throws JSONException, IOException {
//		// place ID found with http://www.flickr.com/services/api/explore/flickr.places.find
//		String placeId = "554890";
//		String flickrRestUrl = getFlickrRestUrlForPlaceId(placeId);
//		setChallengeUrl( getRandomImageUrlFromFlicrRestResponse(flickrRestUrl) );
//		
//		if( null == key ) {
//			DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
//			KeyRange keyRange = datastoreService.allocateIds("AutoChallengeGame", 1L);
//			key = keyRange.getStart();
//		}
//	}
	
	
//	public Key getKey() {
//		if( null == key ) {
//			DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
//			KeyRange keyRange = datastoreService.allocateIds("AutoChallengeGame", 1L);
//			key = keyRange.getStart();
//		}
//		return key;
//	}
	
	public void allocateKey() {
		DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
		KeyRange keyRange = datastoreService.allocateIds("AutoChallengeGame", 1L);
		key = keyRange.getStart();
	}
	
	public void populateAutoChallengeUrl() throws JSONException, IOException {
		// place ID found with http://www.flickr.com/services/api/explore/flickr.places.find
		String placeId = "554890";
		String flickrRestUrl = getFlickrRestUrlForPlaceId(placeId);
		setChallengeUrl( getRandomImageUrlFromFlicrRestResponse(flickrRestUrl) );		
	}
	

	public String getChallengeUrl() {
		return challengeUrl;
	}
	public void setChallengeUrl(String challengeUrl) {
		this.challengeUrl = challengeUrl;
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

}
