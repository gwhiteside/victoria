package net.georgewhiteside.victoria;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.BoxLayout;
import javax.swing.JTextPane;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Box;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

public class QueryEditor extends JDialog {
	
	boolean firstShown = true;
	String queryString = null;
	
	JTextPane textPane;
	JLabel lblCount;
	
	public QueryEditor() {
		this(null);
	}
	
	public QueryEditor(Frame owner) {
		super(owner);
		
		setModal(true);
		
		setMinimumSize(new Dimension(480, 160));
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(8, 8, 8, 8));
		getContentPane().add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] {0, 0};
		gbl_panel.rowHeights = new int[] {0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblCharactersPer = new JLabel("350 characters per search; 99 characters per \"word\" (consecutive characters)");
		GridBagConstraints gbc_lblCharactersPer = new GridBagConstraints();
		gbc_lblCharactersPer.fill = GridBagConstraints.BOTH;
		gbc_lblCharactersPer.insets = new Insets(0, 0, 5, 0);
		gbc_lblCharactersPer.gridx = 0;
		gbc_lblCharactersPer.gridy = 0;
		panel.add(lblCharactersPer, gbc_lblCharactersPer);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 8, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(scrollPane, gbc_scrollPane);
		
		textPane = new JTextPane();
		textPane.getDocument().addDocumentListener(new QueryDocumentListener());
		textPane.addMouseListener(new ContextMenuMouseListener());
		scrollPane.setViewportView(textPane);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] {0, 100, 100};
		gbl_panel_1.rowHeights = new int[] {0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		lblCount = new JLabel("0");
		lblCount.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblCount = new GridBagConstraints();
		gbc_lblCount.insets = new Insets(0, 0, 0, 5);
		gbc_lblCount.weightx = 1.0;
		gbc_lblCount.anchor = GridBagConstraints.WEST;
		gbc_lblCount.gridx = 0;
		gbc_lblCount.gridy = 0;
		panel_1.add(lblCount, gbc_lblCount);
		
		JButton btnSave = new JButton("Save");
		btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(0, 8, 0, 5);
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 0;
		panel_1.add(btnSave, gbc_btnSave);
		
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				queryString = getText();
				setVisible(false);
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 8, 0, 0);
		gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		panel_1.add(btnCancel, gbc_btnCancel);
		
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}
	
	@Override
	public void setVisible(boolean b) {
		if(firstShown) {
			firstShown = false;
			setLocationRelativeTo(getOwner());
		}
		super.setVisible(b);
	}
	
	public void setupAndShow(String videoGameTitle, String query) {
		setTitle("Query String for " + videoGameTitle);
		query = query == null ? "" : query;
		//query = collapseWhitespace(query);
		//textPane.setText(expandCsv(query));
		textPane.setText(query);
		queryString = null;
		setVisible(true);
	}

	public String getQuery() {
		return queryString;
	}
	
	private class QueryDocumentListener implements DocumentListener {
		Pattern pattern = Pattern.compile("-\\(\\S+\\)?");
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			updateCount(e.getDocument());
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateCount(e.getDocument());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateCount(e.getDocument());
		}
		
		private void updateCount(Document doc) {
			String text = getText(doc);
			Matcher matcher = pattern.matcher(text);
			int maxLen = 0;
			while(matcher.find()) {
				String group = matcher.group();
				if(group.length() > maxLen) {
					maxLen = group.length();
				}
			}
			lblCount.setText(String.valueOf(text.length()) + " - " + String.valueOf(maxLen));
		}
	}
	
	private String getText(Document doc) {
		String text = null;
		try {
			text = doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return text;
	}
	
	private String getText() {
		return getText(textPane.getDocument());
	}
	
	private String collapse(String string) {
		return collapseCsv(collapseWhitespace(string));
	}
	
	private String collapseWhitespace(String string) {
		return string.replaceAll("\\s+", " ");
	}
	
	private String collapseCsv(String string) {
		return string.replace(", ", ",");
	}
	
	private String expandCsv(String string) {
		return string.replace(",", ", ");
	}
	
	private String scramble() {
		return null;
	}
}
