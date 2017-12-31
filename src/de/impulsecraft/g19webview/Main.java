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
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserType;
import com.teamdev.jxbrowser.chromium.Callback;
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

	public static void main(String[] args){
		ConfigCallback configCallback = new ConfigCallback() {
			
			@Override
			public void onConfig() {
				// TODO Auto-generated method stub
				
			}
		};
		
		LcdConnection con = new LcdConnection("WebView", false, AppletCapability.getCaps(AppletCapability.QVGA), configCallback, null);
		try{
			final AtomicBoolean exit = new AtomicBoolean(false);
			// #1 Create Browser instance
	        final Browser browser = new Browser(BrowserType.LIGHTWEIGHT);
	        BrowserView view = new BrowserView(browser);

	        // #2 Set the required view size
	        browser.setSize(1920, 1440);

	        // Wait until Chromium resizes view
	        Thread.sleep(500);

	        // #3 Load web page and wait until web page is loaded completely
	        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
	            @Override
	            public void invoke(Browser browser) {
	                browser.loadURL("https://google.com");
	            }
	        });

	        // Wait until Chromium renders web page content
	        Thread.sleep(500);

	        // #4 Get java.awt.Image of the loaded web page.
	        LightWeightWidget lightWeightWidget = (LightWeightWidget) view.getComponent(0);
	        Image image = lightWeightWidget.getImage();

			KeyCallback keyCallback = new KeyCallback() {
				
				@Override
				public void onKeyUp(int button) {
					// TODO Auto-generated method stub
					if (button == CANCEL) exit.set(true);
				}
				
				@Override
				public void onKeyDown(int arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onKey(int arg0) {
					// TODO Auto-generated method stub
					
				}
			};
			LcdDevice device = con.openDevice(DeviceType.QVGA, keyCallback);
			try{
				
//				JTextPane tp = new JTextPane();
//				tp.setSize(320, 240);
//				tp.setContentType("text/html");
//				Scanner sc = new Scanner(new URL("https://google.com").openStream(), "UTF-8");
//				sc.useDelimiter("\\A");
//				String html = sc.next();
//				sc.close();
//				tp.setText(html);
				//tp.setText("<!DOCTYPE html><html><head><title> hi </title></head><body><h1>This is a <b>HEADING</b></h1><p>And this is a paragraph<br>with a new line</p></body></html>");
				
				device.setForeground(true);
				LcdRGBABitmap bmp = device.createRGBABitmap();
				bmp.updateScreen(Priority.ALERT, SyncType.SYNC);
				while(!exit.get()){
					Graphics g = bmp.getGraphics();
//					tp.paint(g);
					g.drawImage(image, 0, 0, 320, 240, null);
					g.dispose();
					bmp.updateScreen(Priority.NORMAL, SyncType.SYNC);
				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
			}finally{
				device.close();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			con.close();
		}
		
	}
	
}
