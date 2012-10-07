package com.afp.pictiris.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.afp.pictiris.data.Callback;
import com.afp.pictiris.data.ImageLoader;
import com.afp.pictiris.model.PictureDescriptor;

public class ResourceManager {

	private final Map<String, Image> handles = new HashMap<String, Image>();
	private final Set<String> loading = new HashSet<String>();
	private final Map<String, Long> tempo = new HashMap<String, Long>();
	private Display display;
	private int capacity;
	public final Image loadingImage;
	private Image preview;
	private String previewId;
	private final ImageLoader loader = new ImageLoader();

	public final Color black;
	public final Color darkGray;

	public ResourceManager(Display display, int capacity) {
		this.display = display;
		this.capacity = capacity;
		loadingImage = new Image(display, "loading.gif");
		black = new Color(display, 40, 40, 40);
		darkGray = new Color(display, 70, 70, 70);
		display.addListener(SWT.Dispose, new Listener() {

			@Override
			public void handleEvent(Event event) {
				loader.stop();
			}
		});
	}

	public Image getThumbnail(final PictureDescriptor pd) {
		final String id = pd.getId();
		if (loading.contains(id)) {
			return loadingImage;
		}
		tempo.put(id, Long.valueOf(System.nanoTime()));
		Image image = handles.get(id);
		return image != null ? image : loadingImage;
	}

	public Image getThumbnail(final PictureDescriptor pd,
			final Callback<Image> callback) {
		final String id = pd.getId();
		if (loading.contains(id)) {
			return loadingImage;
		}
		tempo.put(id, Long.valueOf(System.nanoTime()));
		Image image = handles.get(id);
		if (image == null) {
			loading.add(id);
			image = loadingImage;
			if (handles.size() >= capacity) {
				releaseOldest();
			}
			System.err.println("Loading : "+pd.getIndex());
			loader.loadThumbnail(pd.getThumbnailHref(), new Callback<ImageData>() {
				@Override
				public void success(ImageData data) {
					if (!display.isDisposed()) {
						final Image img = new Image(display, data);
						handles.put(id, img);
						loading.remove(id);
						display.asyncExec(new Runnable() {
							@Override
							public void run() {
								try {
									callback.success(img);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
					}
				}

				@Override
				public void faillure(Throwable th) {
					callback.faillure(th);
				}
			});
		}
		return image;
	}
	
	public Image getPreview(final PictureDescriptor pd,
			final Callback<Image> callback) {
		final String id = pd.getId();
		if (preview != null && id.equals(previewId)) {
			return preview;
		}
		if(preview != null && !preview.isDisposed()) {
			preview.dispose();
		}
		loader.loadPreview(pd.getThumbnailHref(), new Callback<ImageData>() {
			@Override
			public void success(ImageData data) {
				if (!display.isDisposed()) {
					final Image img = new Image(display, data);
					previewId = id;
					preview = img;
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								callback.success(img);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

			@Override
			public void faillure(Throwable th) {
				callback.faillure(th);
			}
		});
		return loadingImage;
		
	}

	private synchronized Image releaseOldest() {
		Map.Entry<String, Long> oldest = null;
		for (Map.Entry<String, Long> entry : tempo.entrySet()) {
			if (oldest == null || oldest.getValue() > entry.getValue()) {
				oldest = entry;
			}
		}
		if (oldest != null) {
			System.err.println("Disposed : " + oldest.getKey());
			handles.remove(oldest.getKey());
			tempo.remove(oldest.getKey());
		}
		return null;
	}

}
