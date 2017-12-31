package de.impulsecraft.g19webview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserContext;
import com.teamdev.jxbrowser.chromium.BrowserContextParams;
import com.teamdev.jxbrowser.chromium.BrowserType;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.StorageType;
import com.teamdev.jxbrowser.chromium.events.FailLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FrameLoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadListener;
import com.teamdev.jxbrowser.chromium.events.ProvisionalLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.StartLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.StatusEvent;
import com.teamdev.jxbrowser.chromium.events.StatusListener;
import com.teamdev.jxbrowser.chromium.swing.internal.LightWeightWidget;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import net.djpowell.lcdjni.AppletCapability;
import net.djpowell.lcdjni.ConfigCallback;
import net.djpowell.lcdjni.DeviceType;
import net.djpowell.lcdjni.KeyCallback;
import net.djpowell.lcdjni.LcdConnection;
import net.djpowell.lcdjni.LcdDevice;
import net.djpowell.lcdjni.LcdRGBABitmap;
import net.djpowell.lcdjni.Priority;
import net.djpowell.lcdjni.SyncType;

public class Main {

	public static void main(String[] args) {
		ConfigCallback configCallback = new ConfigCallback() {

			@Override
			public void onConfig() {
				// TODO Auto-generated method stub

			}
		};

		LcdConnection con = new LcdConnection("WebView", false,
				AppletCapability.getCaps(AppletCapability.QVGA),
				configCallback, null);
		try {
			final AtomicBoolean exit = new AtomicBoolean(false);
			// #1 Create Browser instance
			BrowserContextParams params = new BrowserContextParams("user-data-dir");
	        params.setStorageType(StorageType.MEMORY);

	        BrowserContext browserContext = new BrowserContext(params);
			final Browser browser = new Browser(BrowserType.LIGHTWEIGHT, browserContext);
			BrowserView view = new BrowserView(browser);

			// #2 Set the required view size
			browser.setSize(320, 240);

			// Wait until Chromium resizes view
			Thread.sleep(500);

			// #3 Load web page and wait until web page is loaded completely
			Browser.invokeAndWaitFinishLoadingMainFrame(browser,
					new Callback<Browser>() {
						@Override
						public void invoke(Browser browser) {
							browser.loadURL(JOptionPane.showInputDialog("What page do you want to load ?"));
						}
					});

			// Wait until Chromium renders web page content and scroll to the top left corner
			Thread.sleep(500);
			browser.executeJavaScript("window.scrollTo(0, " + "0);");
			JSValue documentHeight = browser
					.executeJavaScriptAndReturnValue("Math.max(document.body.scrollHeight, "
							+ "document.documentElement.scrollHeight);");
			JSValue documentWidth = browser
					.executeJavaScriptAndReturnValue("Math.max(document.body.scrollWidth, "
							+ "document.documentElement.scrollWidth);");
			final int maxX = (int) documentWidth.getNumberValue(), maxY = (int) documentHeight
					.getNumberValue();

			// #4 Get java.awt.Image of the loaded web page.
			LightWeightWidget lightWeightWidget = (LightWeightWidget) view
					.getComponent(0);
			Image image = lightWeightWidget.getImage();

			KeyCallback keyCallback = new KeyCallback() {
				boolean okDown = false;
				boolean menuDown = false;
				@Override
				public void onKeyUp(int button) {
					if (button == CANCEL && !menuDown)
						exit.set(true);
					if (button == OK)
						okDown = false;
					if (button == MENU)
						menuDown = false;
				}

				@Override
				public void onKeyDown(int button) {
					if (button == OK)
						okDown = true;
					if (button == MENU)
						menuDown = true;
				}

				int scrollx = 0, scrolly = 0;

				@Override
				public void onKey(int button) {
					if (!menuDown) {
						if (button == UP) {
							if (scrolly - 120 > 0) {
								scrolly -= 120;
							} else {
								scrolly = 0;
							}
						}
						if (button == DOWN) {
							if (scrolly + 120 < maxY) {
								scrolly += 120;
							} else {
								scrolly = maxY - 240;
							}
						}

						if (button == LEFT) {
							scrollx -= (scrollx - 160 > 0) ? 160 : scrollx;
						}
						if (button == RIGHT) {
							scrollx += (scrollx + 160 < maxX) ? 160 : (maxX
									- scrollx - 240);
						}
						if (button == UP || button == DOWN || button == LEFT
								|| button == RIGHT)
							browser.executeJavaScript("window.scrollTo("
									+ scrollx + ", " + scrolly + ");");
					} else {
						if(button == UP){
							browser.zoomIn();
						}
						if(button == DOWN){
							browser.zoomOut();
						}
						if(button == OK){
							browser.zoomReset();
						}
					}
				}
			};
			LcdDevice device = con.openDevice(DeviceType.QVGA, keyCallback);
			try {

				
				device.setForeground(true);
				LcdRGBABitmap bmp = device.createRGBABitmap();
				bmp.updateScreen(Priority.ALERT, SyncType.SYNC);
				while (!exit.get()) {
					Graphics g = bmp.getGraphics();
					g.drawImage(lightWeightWidget.getImage(), 0, 0, 320, 240,
							null);
					g.dispose();
					bmp.updateScreen(Priority.NORMAL, SyncType.SYNC);
				}
			} finally {
				device.close();
				browser.dispose();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			con.close();
			System.exit(0);
		}

	}
}
