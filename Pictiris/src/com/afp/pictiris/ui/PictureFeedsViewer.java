package com.afp.pictiris.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import com.afp.pictiris.data.Callback;
import com.afp.pictiris.data.FeedProvider;
import com.afp.pictiris.model.PictureDescriptor;

public class PictureFeedsViewer extends Composite {

	private final ResourceManager resourceManager;
	private Composite previewControl;
	private Label previewLabel;
	private GridData previewData;
	private PictureFeedViewer currentFeed = null;
	private final List<PictureFeedViewer> feeds = new ArrayList<PictureFeedViewer>();

	public PictureFeedsViewer(Composite parent, ResourceManager manager) {
		super(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		setLayout(layout);
		this.resourceManager = manager;
		setBackground(resourceManager.black);

		previewControl = new Composite(this, SWT.BORDER);
		GridLayout gl = new GridLayout(1, false);
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		previewControl.setLayout(gl);
		previewData = new GridData(SWT.FILL, SWT.FILL, true, true);
		previewData.exclude = true;
		previewControl.setVisible(false);
		previewControl.setLayoutData(previewData);

		Link close = new Link(previewControl, SWT.NONE);
		close.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		close.setText("<a>X</a>");
		close.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentFeed = null;
				for (int i = 0; i < feeds.size(); i++) {
					PictureFeedViewer f = feeds.get(i);
					f.setVisible(true);
				}
				previewControl.setVisible(false);
				previewData.exclude = true;
				relayout();
			}
		});

		previewLabel = new Label(previewControl, SWT.NONE);
		previewLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true,
				true));
		previewLabel.setImage(resourceManager.loadingImage);
	}

	public void addFeed() {
		final PictureFeedViewer feed = new PictureFeedViewer(this,
				resourceManager) {
			@Override
			public void feedSelected() {
				currentFeed = this;
				for (int i = 0; i < feeds.size(); i++) {
					PictureFeedViewer f = feeds.get(i);
					f.setVisible(f == currentFeed);
				}
				previewControl.setVisible(true);
				previewData.exclude = false;
				relayout();
			}

			@Override
			public void pictureSelected(PictureDescriptor pd) {
				resourceManager.getPreview(pd, new Callback<Image>() {

					@Override
					public void success(Image image) {
						previewLabel.setImage(image);
						relayout();
					}

					@Override
					public void faillure(Throwable th) {
						th.printStackTrace();
					}
				});
			}

		};
		feeds.add(feed);
		feed.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		feed.setModel(FeedProvider.getFeed());
		feed.setVisible(currentFeed == null);
		layout(true, true);
	}

	public void relayout() {
		layout(true, true);
	}
}
