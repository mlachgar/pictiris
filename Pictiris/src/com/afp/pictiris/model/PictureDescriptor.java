package com.afp.pictiris.model;

import org.eclipse.swt.graphics.Image;

public class PictureDescriptor {
	private String id;
	private int index;
	private String title;
	private String thumbnailHref;
	private String visuHref;
	public Image image;
	
	 public PictureDescriptor() {
		
	}

	 public void setId(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getThumbnailHref() {
		return thumbnailHref;
	}

	public void setThumbnailHref(String thumbnailHref) {
		this.thumbnailHref = thumbnailHref;
	}

	public String getVisuHref() {
		return visuHref;
	}

	public void setVisuHref(String visuHref) {
		this.visuHref = visuHref;
	}

	public String getId() {
		return id;
	}
	 
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	 
}
