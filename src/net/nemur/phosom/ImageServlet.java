package net.nemur.phosom;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFilename;

public class ImageServlet extends HttpServlet {

	public void doGet( HttpServletRequest req, HttpServletResponse resp) throws IOException {
		GcsFilename fileName = getFileName(req);
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		BlobKey blobKey = blobstoreService.createGsBlobKey(
				"/gs/" + fileName.getBucketName() + "/" + fileName.getObjectName() );
		blobstoreService.serve(blobKey, resp);
	}
	
	
	private GcsFilename getFileName(HttpServletRequest req) {
		String[] splits = req.getRequestURI().split("/", 4);
		if (!splits[0].equals("") || !splits[1].equals("image-service")) {
			throw new IllegalArgumentException(
					"The URL is not formed as expected. "
							+ "Expecting /image-service/<bucket>/<object>");
		}
		return new GcsFilename(splits[2], splits[3]);
	}
}
