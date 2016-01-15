package net.georgewhiteside.victoria;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Videogame Investment Calculator and Tracker for Optimizing Returns on Internet Auctions

// HikariCP? just go with sqlite?

// filter by system

// price display options -- price to buy individually / estimated price to sell individually (subtract ebay fees, shipping cost)

// track and respect daily api request volume

// auto-update; every 24 hours check search table for update timestamps and update anything older than (say) 4 weeks; possibly configurable

// exclude sales in freakin' Australia... Canada too?

// indicate rising and falling prices with color or symbol (help spot/avoid temporary fad price inflations)

public class Main
{
	static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) {
		Thread.currentThread().setName("bootstrap");
		logger.debug("Application entry point");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Thread.currentThread().setName("dispatch-0");
				logger.debug("Handoff to dispatch thread");
				try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
				catch (Exception e) { e.printStackTrace(); }
				new MainWindow();
			}
		});
		logger.debug("Init thread concluded");
	}
}
