package com.afp.pictiris.ui;

import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class TestApp {

	private ResourceManager resourcesManager;
	private PictureFeedsViewer viewer;

	public TestApp() {

	}

	public void start() {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setSize(1000, 600);
		shell.setLayout(new GridLayout(1, false));
		shell.setText("Picture Gallery");

		resourcesManager = new ResourceManager(display, 200);
		// Color bg = resourcesManager.darkGray;
		Color bg = display.getSystemColor(SWT.COLOR_GRAY);

		shell.setBackground(bg);

		Control bar = createCoolbar(shell);
		bar.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));

		bar.setBackground(bg);
		viewer = new PictureFeedsViewer(shell, resourcesManager);
		viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.addFeed();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	public Control createCoolbar(Composite parent) {
		ToolBarManager manager = new ToolBarManager();
		manager.add(new Action("New feed", ImageDescriptor
				.createFromURL(TestApp.class.getResource("/1.png"))) {
			@Override
			public void run() {
				viewer.addFeed();
			}
		});
		manager.add(getAction("2.png"));
		manager.add(getAction("3.png"));
		manager.add(getAction("4.png"));
		manager.add(getAction("5.png"));
		manager.add(getAction("6.png"));
		return manager.createControl(parent);
	}

	private static Action getAction(final String id) {
		Action action = new Action(id) {

		};
		URL url = TestApp.class.getResource("/" + id);
		action.setImageDescriptor(ImageDescriptor.createFromURL(url));
		return action;
	}

	public static void main(String[] args) {
		new TestApp().start();
	}
}
