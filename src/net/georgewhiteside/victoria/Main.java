package net.georgewhiteside.victoria;

import java.awt.EventQueue;
import java.io.IOException;

import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Videogame Investment Calculator and Tracker for Optimizing Returns on Internet Auctions

// 350 characters per search; 99 characters per word ("word" defined as consecutive characters

// HikariCP, BoneCP, c3p0, DBCP

// provide median and interquartile range

// Calendar and TimeUnit

// NumberFormat.getCurrencyInstance()

public class Main
{
	static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args) throws IOException {
		logger.info("Application started.");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
				catch (Exception e) { e.printStackTrace(); }
				MainWindow window = new MainWindow();
			}
		});
	}
}
