package de.impulsecraft.g19webview;

import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import com.teamdev.jxbrowser.chromium.Browser;
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
				/*Browser browser = new Browser();
				BrowserView view = new BrowserView(browser);
				view.setSize(320, 240);
				browser.loadURL("https://google.com");*/
				JTextPane tp = new JTextPane();
				tp.setSize(320, 240);
				tp.setContentType("text/html");
				Scanner sc = new Scanner(new URL("https://google.com").openStream(), "UTF-8");
				sc.useDelimiter("\\A");
				String html = sc.next();
				sc.close();
				tp.setText(html);
				//tp.setText("<!DOCTYPE html><html><head><title> hi </title></head><body><h1>This is a <b>HEADING</b></h1><p>And this is a paragraph<br>with a new line</p></body></html>");
				device.setForeground(true);
				LcdRGBABitmap bmp = device.createRGBABitmap();
				bmp.updateScreen(Priority.ALERT, SyncType.SYNC);
				while(!exit.get()){
					Graphics g = bmp.getGraphics();
					tp.paint(g);
					//view.paint(g);
					g.dispose();
					bmp.updateScreen(Priority.NORMAL, SyncType.SYNC);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				device.close();
			}
		}finally{
			con.close();
		}
		
	}
	
}