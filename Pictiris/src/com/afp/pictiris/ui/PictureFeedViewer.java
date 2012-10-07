package com.afp.pictiris.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.progress.UIJob;

import com.afp.pictiris.data.Callback;
import com.afp.pictiris.model.PictureDescriptor;

public abstract class PictureFeedViewer extends Composite {

	public static final int REFRESH_PERIOD = 300;
	public int picHeight = 100;
	public int picWidth = 160;
	public int margin = 5;

	private final ResourceManager handles;
	private List<PictureDescriptor> model = Collections.emptyList();
	private Canvas canvas;
	private Link label;
	private Point size = new Point(0, 0);
	private Job loadJob;
	private GridData gridData;
	private Slider slider;

	private int selectedIndex = -1;
	private VisibleArea visibleArea = new VisibleArea();

	public abstract void feedSelected();

	public abstract void pictureSelected(PictureDescriptor pd);

	public PictureFeedViewer(Composite parent, final ResourceManager handles) {
		super(parent, SWT.BORDER);
		setBackgroundMode(SWT.INHERIT_DEFAULT);
		setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		this.handles = handles;
		setBackground(handles.darkGray);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		setLayout(layout);

		createInfoComposite(this);

		createCanvas(this);

		setUpLoadJob();
		setUpControlListeners();
	}

	private void createCanvas(Composite parent) {
		Composite content = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		content.setLayout(layout);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		canvas = new Canvas(content, SWT.BORDER);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(final PaintEvent e) {
				Image imgGC = new Image(e.display, canvas.getBounds());
				GC gc = new GC(imgGC);
				try {
					computeVisibleItems();
					int x = visibleArea.offset;
					for (int i = visibleArea.start; i < visibleArea.end; i++) {
						PictureDescriptor pd = model.get(i);
						Image img = handles.getThumbnail(pd);
						if (img != null) {
							Rectangle bounds = img.getBounds();
							gc.drawImage(img, 0, 0, bounds.width,
									bounds.height, x, margin, picWidth,
									picHeight);
							gc.drawText(String.valueOf(i + 1),
									x + picWidth / 2, picHeight - 10, true);
						}
						if (i == selectedIndex) {
							Color bg = e.gc.getForeground();
							try {
								gc.setLineWidth(margin);
								gc.setForeground(e.display
										.getSystemColor(SWT.COLOR_WHITE));
								gc.drawRectangle(x, margin, picWidth, picHeight);
							} finally {
								gc.setForeground(bg);
							}
						}
						x += picWidth + margin;
					}
					e.gc.drawImage(imgGC, 0, 0);
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					imgGC.dispose();
					gc.dispose();
				}
			}
		});

		slider = new Slider(content, SWT.HORIZONTAL);
		slider.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
		slider.setMinimum(0);
		slider.setMaximum(100);
		slider.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				visibleArea.x = slider.getSelection();
				canvas.redraw();
				slider.setToolTipText(String.valueOf(slider.getSelection()));
			}
		});
		slider.setToolTipText(String.valueOf(slider.getSelection()));
	}

	private void setUpLoadJob() {
		loadJob = new UIJob(getDisplay(), "Load images job") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					if (!isDisposed() && model.size() > 0) {
						computeVisibleItems();
						for (int i = visibleArea.start; i <= visibleArea.end; i++) {
							PictureDescriptor pd = model.get(i);
							final int index = i;
							handles.getThumbnail(pd, new Callback<Image>() {

								@Override
								public void success(Image image) {
									if (!isDisposed()) {
										redrawItemAt(index);
									}
								}

								@Override
								public void faillure(Throwable th) {
									th.printStackTrace();
								}
							});
						}
						schedule(REFRESH_PERIOD);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		loadJob.schedule(REFRESH_PERIOD);
	}

	private void setUpControlListeners() {
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				selectItem(e.x, e.y);
			}
		});

		canvas.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				canvas.setToolTipText("(" + e.x + "," + e.y + ")\n("
						+ canvas.getBounds() + ")");
			}
		});

		canvas.addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (selectedIndex != -1) {
					int i = selectedIndex;
					if (e.keyCode == SWT.ARROW_LEFT && selectedIndex > 0) {
						clearSelection();
						selectItemAt(i - 1);
					} else if (e.keyCode == SWT.ARROW_RIGHT
							&& selectedIndex < model.size() - 1) {
						clearSelection();
						selectItemAt(i + 1);
					}
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {

			}
		});

		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				refresh();
			}
		});
	}

	public void clearSelection() {
		if (selectedIndex != -1) {
			int i = selectedIndex;
			selectedIndex = -1;
			redrawItemAt(i);
		}
	}

	private void ensureVisible(int i) {
		// int x = i * (picWidth + margin);
		// int start = scrollBar.getSelection();
		// int width = scrollBar.getThumb();
		// int end = start + width - picWidth - margin;
		// if (x < start) {
		// scroll.setOrigin(x, 0);
		// } else if (x > end) {
		// scroll.setOrigin(x - width + picWidth + margin, 0);
		// }
	}

	protected void redrawItemAt(int i) {
		canvas.redraw();
		// canvas.redraw(i * (picWidth + margin) - margin, 0, picWidth + 2
		// * margin, picHeight + 2 * margin, false);
	}

	protected void selectItem(int x, int y) {
		clearSelection();
		int i = x / (picWidth + margin);
		if (i >= 0 && i < model.size()) {
			selectItemAt(i);
		}
	}

	protected void selectItemAt(int i) {
		selectedIndex = i;
		PictureDescriptor selected = model.get(i);
		pictureSelected(selected);
		ensureVisible(i);
		redrawItemAt(i);
	}

	public ResourceManager getHandles() {
		return handles;
	}

	private void createInfoComposite(Composite parent) {
		Composite infoComposite = new Composite(parent, SWT.BORDER);
		infoComposite.setLayout(new GridLayout(1, false));
		infoComposite.setLayoutData(new GridData(SWT.LEAD, SWT.FILL, false,
				true));

		label = new Link(infoComposite, SWT.FLAT);
		label.setForeground(getDisplay().getSystemColor(SWT.COLOR_GRAY));
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		label.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				feedSelected();
			}
		});
	}

	private void computeVisibleItems() {
		int width = canvas.getSize().x;
		visibleArea.start = visibleArea.x / (picWidth + margin);
		visibleArea.end = ((visibleArea.x + width) / (picWidth + margin));
		visibleArea.offset = ((visibleArea.x + width) % (picWidth + margin));
		if (visibleArea.offset != 0) {
			visibleArea.end++;
		}
	}

	public void setModel(List<PictureDescriptor> model) {
		this.model = model;
		if (model == null) {
			size.x = 0;
			size.y = 0;
			label.setText("<a>no items</a>");
		} else {
			label.setText("<a>" + model.size() + " items</a>");
			size.x = (picWidth + margin) * model.size();
			size.y = picHeight + 2 * margin;
		}
		refresh();
	}

	public void setLayoutData(GridData layoutData) {
		gridData = layoutData;
		super.setLayoutData(layoutData);
	}

	@Override
	public void setVisible(boolean visible) {
		gridData.exclude = !visible;
		super.setVisible(visible);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point p = slider.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point s = super.computeSize(SWT.DEFAULT,
				picHeight + (2 * margin) + p.y, true);
		return s;
	}

	private void refresh() {
		size = canvas.computeSize(SWT.DEFAULT, picHeight + 2 * margin);
		canvas.setSize(size);
		slider.setMaximum((picWidth + margin) * model.size());
		slider.setThumb(size.x);
		slider.setPageIncrement(size.x);
		slider.setIncrement(picWidth + margin);
		layout(false);
	}

	private class VisibleArea {
		int x;
		int start;
		int end;
		int offset;

		@Override
		public String toString() {
			return "x:" + x + "\nstart:" + start + "\nend:" + end + "\noffset:"
					+ offset;
		}
	}
}
