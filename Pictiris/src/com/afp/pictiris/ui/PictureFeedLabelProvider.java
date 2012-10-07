package com.afp.pictiris.ui;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import com.afp.pictiris.data.Callback;
import com.afp.pictiris.model.PictureDescriptor;

public class PictureFeedLabelProvider extends OwnerDrawLabelProvider {

	public int picHeight = 100;
	public int picWidth = 100;
	public int margin = 5;

	private final Image loadingImage;
	private final ResourceManager handles;

	public PictureFeedLabelProvider(Display display) {
		handles = new ResourceManager(display, 40);
		loadingImage = new Image(display, "loading.gif");
	}

	@Override
	protected void measure(Event event, Object element) {
		event.height = picHeight + 2 * margin;
		event.width = picWidth + 2 * margin;
		event.x = event.index * event.width;
		event.y = margin;
	}

	@Override
	protected void paint(final Event event, Object element) {
		if (element instanceof PictureDescriptor) {
			PictureDescriptor pd = (PictureDescriptor) element;
			/*Image image = handles.getImage(pd.getThumbnailHref(), new Callback<Image>() {
				
				@Override
				public void success(Image image) {
					Control c = (Control) event.widget;
					c.redraw(event.x, event.y, event.width, event.height, true);
				}
				
				@Override
				public void faillure(Throwable th) {
					
				}
			});
			if(image == null) {
				image = loadingImage;
			}*/
			Image image = loadingImage;
			Rectangle origin = image.getBounds();
			event.gc.drawImage(image, 0, 0, origin.width, origin.height, event.x,
					event.y, picHeight, picWidth);
		}
	}

}
