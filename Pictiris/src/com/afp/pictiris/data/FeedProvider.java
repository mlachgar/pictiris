package com.afp.pictiris.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.afp.pictiris.model.PictureDescriptor;

public class FeedProvider {

	public static List<PictureDescriptor> getFeed() {
		List<String> list = new ArrayList<String>();
		File dir = new File("I:\\data-from-D\\famille\\Loire-et-Cher\\zoo");
		for (File f : dir.listFiles()) {
			if (f.getName().toLowerCase().endsWith(".jpg")) {
				list.add(f.getAbsolutePath());
			}
		}
		List<PictureDescriptor> list2 = new ArrayList<PictureDescriptor>();
		int index = 0;
		for (int i = 0; i < 1; i++) {
			for (String path : list) {
				PictureDescriptor pd = new PictureDescriptor();
				pd.setVisuHref(path);
				pd.setThumbnailHref(path);
				pd.setId(UUID.randomUUID().toString());
				pd.setIndex(index++);
				list2.add(pd);
			}
		}
		return list2;
	}
}
