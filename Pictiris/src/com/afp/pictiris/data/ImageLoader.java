package com.afp.pictiris.data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

public class ImageLoader {
	private final ExecutorService service = Executors.newFixedThreadPool(5);

	public ImageLoader() {

	}

	public void loadThumbnail(final String href, final Callback<ImageData> callback) {
		service.execute(new Runnable() {

			@Override
			public void run() {
				try {
					ImageData data = new ImageData(href);
					Point dim = scaleTo(new Point(data.width, data.height),
							160, 100);
					callback.success(data.scaledTo(dim.x, dim.y));
				} catch (Exception e) {
					callback.faillure(e);
				}
			}
		});
	}

	
	public void loadPreview(final String href, final Callback<ImageData> callback) {
		service.execute(new Runnable() {

			@Override
			public void run() {
				try {
					ImageData data = new ImageData(href);
					Point dim = scaleTo(new Point(data.width, data.height),
							1024, 640);
					callback.success(data.scaledTo(dim.x, dim.y));
				} catch (Exception e) {
					callback.faillure(e);
				}
			}
		});
	}
	
	public static Point scaleTo(Point data, int maxWidth, int maxHeight) {
		if (data.x < maxWidth && data.y < maxHeight) {
			return data;
		}
		float ratio = Math.min((((float) maxWidth) / data.x),
				(((float) maxHeight) / data.y));
		int w = (int) (data.x * ratio);
		int h = (int) (data.y * ratio);
		return new Point(w, h);
	}

	public void stop() {
		service.shutdown();
	}
}
