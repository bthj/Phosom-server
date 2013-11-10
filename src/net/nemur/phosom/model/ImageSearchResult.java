package net.nemur.phosom.model;

public class ImageSearchResult {

	private String fullSizeImageUrl;
	private String thumbnailUrl;
	private String altText;
	private String sourceUrl;
	private String sourceTitle;
	
	public String getFullSizeImageUrl() {
		return fullSizeImageUrl;
	}
	public void setFullSizeImageUrl(String fullSizeImageUrl) {
		this.fullSizeImageUrl = fullSizeImageUrl;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public String getAltText() {
		return altText;
	}
	public void setAltText(String altText) {
		this.altText = altText;
	}
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl( String sourceUrl ) {
		this.sourceUrl = sourceUrl;
	}
	public String getSourceTitle() {
		return sourceTitle;
	}
	public void setSourceTitle( String sourceTitle ) {
		this.sourceTitle = sourceTitle;
	}
	
}
