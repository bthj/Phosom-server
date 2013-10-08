package net.nemur.phosom.model.gametypes;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import net.nemur.phosom.model.Game;

@PersistenceCapable
public class AutoChallengeGame extends Game {
	
	private static final String FLICKR_API_KEY = "0cc84fc9654aaeca27ce2ee40a0cf574"; // TODO: environment variable or something!
	
	@Persistent
	String challengeUrl;
	
	public AutoChallengeGame() {
		// TODO Auto-generated constructor stub
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
	private String getRandomImageUrlFromFlicrRestResponse( String flickrRestUrl ) {
		String imageUrl = null;
		
		return imageUrl;
	}

}
