package net.nemur.phosom.util;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.blobstore.UploadOptions;

public class BlobUtil {

	public final static String BUCKET_NAME_AUTO_CHALLENGE = "auto-challenge-photos";
	public final static String BUCKET_NAME_CHALLENGE_RESPONSES = "challenge-response-photos";
	public final static String UPLOAD_HANDLER_URL = "/upload";
	
	public static BlobKey getBlobKeyFromBucketAndFileName( String bucketName, String fileName ) {
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		BlobKey blobKey = blobstoreService.createGsBlobKey( 
				"/gs/" + bucketName + "/" + fileName );
		return blobKey;
	}

}
