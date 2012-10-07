package com.afp.pictiris.model;

import java.util.ArrayList;
import java.util.List;

public abstract class PictureFeedModel {

	private List<PictureDescriptor> descriptors = new ArrayList<PictureDescriptor>();

	public List<PictureDescriptor> getDescriptors() {
		return descriptors;
	}

	public void setDescriptors(List<PictureDescriptor> descriptors) {
		this.descriptors.clear();
		this.descriptors.addAll(descriptors);

		for (int i = 0; i < this.descriptors.size(); i++) {
			PictureDescriptor pd = this.descriptors.get(i);
			final String href = pd.getVisuHref();
		}
	}

}
