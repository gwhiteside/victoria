package net.georgewhiteside.victoria;

import java.awt.EventQueue;

import java.io.IOException;

import javax.swing.UIManager;

// Videogame Investment Calculator and Tracker for Optimizing Returns on Internet Auctions

// 350 characters per search; 99 characters per word ("word" defined as consecutive characters

// BoneCP, c3p0, DBCP

// TimeUnit

// provide median and interquartile range

// Calendar and TimeUnit

public class Main {
	public static void main(String[] args) throws IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
				catch (Exception e) { e.printStackTrace(); }
				MainWindow window = new MainWindow();
			}
		});
	}
}
