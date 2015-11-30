package net.georgewhiteside.victoria;

import info.puneetsingh.fsm.TextBrew;

import java.awt.EventQueue;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;

import javax.swing.JTextField;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Position;
import javax.swing.text.DocumentFilter.FilterBypass;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.NGramDistance;
import org.apache.lucene.search.spell.StringDistance;

import com.ebay.services.client.ClientConfig;
import com.ebay.services.client.FindingServiceClientFactory;
import com.ebay.services.finding.FindCompletedItemsRequest;
import com.ebay.services.finding.FindCompletedItemsResponse;
import com.ebay.services.finding.FindItemsAdvancedRequest;
import com.ebay.services.finding.FindItemsAdvancedResponse;
import com.ebay.services.finding.FindItemsByKeywordsRequest;
import com.ebay.services.finding.FindItemsByKeywordsResponse;
import com.ebay.services.finding.FindingServicePortType;
import com.ebay.services.finding.PaginationInput;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

// https://github.com/tdebatty/java-string-similarity
// https://github.com/apache/lucene-solr/tree/lucene_solr_5_3_1/lucene/suggest/src/java/org/apache/lucene/search/spell
// http://www.csse.monash.edu.au/~lloyd/tildeAlgDS/Dynamic/Edit/
// https://mail.python.org/pipermail/python-list/2001-March/085007.html
// http://cpansearch.perl.org/src/KCIVEY/Text-Brew-0.02/lib/Text/Brew.pm
// http://www.ling.ohio-state.edu/~cbrew/795M/string-distance.html
// https://en.wikipedia.org/wiki/String_metric
// https://en.wikipedia.org/wiki/Approximate_string_matching
// https://en.wikipedia.org/wiki/Edit_distance
// https://en.wikipedia.org/wiki/Bitap_algorithm
// http://www.ling.ohio-state.edu/~cbrew/795M/string-distance.html
// https://github.com/relaynetwork/fuzzy-string-matching/blob/master/src/main/java/com/relaynetwork/TextBrew.java
// ...found via http://www.slideshare.net/kyleburton/fuzzy-string-matching
// http://stackoverflow.com/questions/7842071/improved-levenshtein-algorithm
// http://stackoverflow.com/questions/5859561/getting-the-closest-string-match

// The longest common subsequence (LCS) of two sequences, s1 and s2, is a subsequence
// of both s1 and of s2 of maximum possible length. The more alike that s1 and s2 are, the longer is their LCS.

public class MainWindow {
	private String EBAY_CAT_VIDEO_GAMES = "139973";
	
	private String title = "VICTORIA";

	private JFrame frame;
	private JTextField textSearch;
	private JTable tableSearch;
	private JTable tableSelected;
	
	private List<VideoGame> videoGames;

	/**
	 * Create the application.
	 */
	public MainWindow() {
		Config config = Config.getInstance();
		
		String dbUrl = config.getProperty(Config.DB_URL);
		String dbUser = config.getProperty(Config.DB_USER);
		String dbPass = config.getProperty(Config.DB_PASS);
		
		VideoGameDatabase vgDatabase = new VideoGameDatabase(dbUrl, dbUser, dbPass);
		
		initialize();
		videoGames = vgDatabase.getProducts();
		frame.setVisible(true);
		
		//ebayTest();
	}
	
	private void ebayTest() {
		ClientConfig clientConfig = new ClientConfig();
		String ebayAppID = Config.getInstance().getProperty(Config.EBAY_APP_ID);
		clientConfig.setApplicationId(ebayAppID);
		
		FindingServicePortType serviceClient = FindingServiceClientFactory.getServiceClient(clientConfig);
		
		FindCompletedItemsRequest request = new FindCompletedItemsRequest();
		
		request.setKeywords("`street fighter 2010`".replace('`', '"'));
		request.getCategoryId().clear();
		request.getCategoryId().add(EBAY_CAT_VIDEO_GAMES);
		
		PaginationInput pi = new PaginationInput();
		pi.setEntriesPerPage(100);
		pi.setPageNumber(1);
		
		request.setPaginationInput(pi);
		
		FindCompletedItemsResponse response = serviceClient.findCompletedItems(request);
		
		System.out.println("Ack = " + response.getAck());
		System.out.println("Found " + response.getSearchResult().getCount() + " items");
	}
	


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame(title);
		frame.setBounds(100, 100, 562, 694);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				System.out.println("Shutting down...");
				cleanup();
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(8, 8, 8, 8));
		frame.getContentPane().add(panel);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {100, 100, 0};
		gridBagLayout.rowHeights = new int[] {20, 20, 20, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gridBagLayout);
		
		
		
		textSearch = new JTextField();
		((AbstractDocument)textSearch.getDocument()).setDocumentFilter(changeDocumentFilter);
		

		KeyStroke keyStrokeDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		textSearch.getInputMap().put(keyStrokeDown, "down");
		textSearch.getActionMap().put("down", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) { 
				autocompleteNavigate(1);
			}
		});
		
		KeyStroke keyStrokeUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
		textSearch.getInputMap().put(keyStrokeUp, "up");
		textSearch.getActionMap().put("up", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				autocompleteNavigate(-1);
			}
		});
		
		KeyStroke keyStrokeEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		textSearch.getInputMap().put(keyStrokeEnter, "enter");
		textSearch.getActionMap().put("enter", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				autocompleteSelect();
			}
		});
		
		GridBagConstraints gbc_textSearch = new GridBagConstraints();
		gbc_textSearch.fill = GridBagConstraints.BOTH;
		gbc_textSearch.insets = new Insets(0, 0, 5, 5);
		gbc_textSearch.gridx = 0;
		gbc_textSearch.gridy = 0;
		panel.add(textSearch, gbc_textSearch);
		
		
		
		tableSearch = new JTable();
		tableSearch.setFillsViewportHeight(true);
		setTableProperties(tableSearch);
		tableSearch.setModel(new VideoGameTableModel());
		tableSearch.setTableHeader(null);
		JScrollPane scrollSearch = new JScrollPane(tableSearch);
		
		GridBagConstraints gbc_scrollSearch = new GridBagConstraints();
		gbc_scrollSearch.weightx = 0.5;
		gbc_scrollSearch.gridheight = 2;
		gbc_scrollSearch.insets = new Insets(0, 0, 0, 5);
		gbc_scrollSearch.fill = GridBagConstraints.BOTH;
		gbc_scrollSearch.gridx = 0;
		gbc_scrollSearch.gridy = 1;
		panel.add(scrollSearch, gbc_scrollSearch);
		
		
		
		tableSelected = new JTable();
		tableSelected.setFillsViewportHeight(true);
		setTableProperties(tableSelected);
		tableSelected.setModel(new VideoGameTableModel());
		tableSelected.setTableHeader(null);
		JScrollPane scrollSelected = new JScrollPane(tableSelected);
		
		GridBagConstraints gbc_scrollSelected = new GridBagConstraints();
		gbc_scrollSelected.weightx = 0.5;
		gbc_scrollSelected.fill = GridBagConstraints.BOTH;
		gbc_scrollSelected.insets = new Insets(0, 0, 5, 0);
		gbc_scrollSelected.gridx = 1;
		gbc_scrollSelected.gridy = 1;
		panel.add(scrollSelected, gbc_scrollSelected);
		
		
		
		JLabel labelTotal = new JLabel("$4.76");
		labelTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		GridBagConstraints gbc_labelTotal = new GridBagConstraints();
		gbc_labelTotal.fill = GridBagConstraints.HORIZONTAL;
		gbc_labelTotal.ipadx = 8;
		gbc_labelTotal.gridx = 1;
		gbc_labelTotal.gridy = 2;
		panel.add(labelTotal, gbc_labelTotal);
		
		
		
		
		
		
		
		
		
		
	}
	
	private void setTableProperties(JTable table) {
		table.setRowHeight(20);
		table.setIntercellSpacing(new Dimension(0, 2));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowGrid(false);
		table.setDefaultRenderer(Object.class, new FocusedCellRenderer()); // remove any annoying cell border giving a clean row selection
	}
	
	
	
	private void autocompleteSelect() {
		int selectedRow = tableSearch.getSelectedRow();
		
		if(selectedRow == -1) {
			return;
		}
		
		/*
		@SuppressWarnings("unchecked")
		AdvancedTableModel<VideoGameMatch> atm = (AdvancedTableModel<VideoGameMatch>) tableSearch.getModel();
		VideoGameMatch vgm = atm.getElementAt(selectedRow);
		VideoGame vg = vgm.getVideoGame();
		*/
		
		VideoGameTableModel searchModel = (VideoGameTableModel) tableSearch.getModel();
		//System.out.println(vgtm.getVideoGameAt(selectedRow));
		VideoGame vg = searchModel.getVideoGameAt(selectedRow);
		
		VideoGameTableModel selectedModel = (VideoGameTableModel) tableSelected.getModel();
		selectedModel.addRow(vg);
		
		textSearch.setText(""); // automatically clears the tableSearch model
	}
	
	
	
	private void autocompleteNavigate(int moveAmount) {
		int numRows = tableSearch.getRowCount();
		int selectedRow = tableSearch.getSelectedRow();
		
		if(numRows == 0)
			return;
		
		int newRow = selectedRow + moveAmount;
		
		if(newRow < 0) {
			newRow = numRows - 1;
		} else if(newRow == numRows) {
			newRow = 0;
		}
		
		tableSearch.setRowSelectionInterval(newRow, newRow);
		tableSearch.scrollRectToVisible(tableSearch.getCellRect(newRow, 0, true));
	}
	
	
	
	private void updateSearchResults(String query) {
		VideoGameTableModel model = (VideoGameTableModel) tableSearch.getModel();
		model.clear();
		
		if(query.length() == 0) {
			return;
		}
		
		long startTime = System.nanoTime();
		List<VideoGameMatch> autocomplete = searchEnhanced(query, videoGames);
		long endTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println("" + endTime + " ms");
		
		int limit = Integer.MAX_VALUE;
		for(VideoGameMatch vgm : autocomplete) {
			if(limit-- <= 0) {
				break;
			}
			model.addRow(vgm.getVideoGame());
		}
		
		if(tableSearch.getRowCount() > 0) {
			tableSearch.setRowSelectionInterval(0, 0); // select the first item by default when typing a search string
		}
	}
	
	DocumentFilter changeDocumentFilter = new DocumentFilter() {
		@Override
		public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
			super.remove(fb, offset, length);
			update(fb);
		}

		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
			super.insertString(fb, offset, string, attr);
			update(fb);
		}
		
		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
			super.replace(fb, offset, length, text, attrs);
			update(fb);
		}
		
		private void update(FilterBypass fb) throws BadLocationException {
			Document d = fb.getDocument();
			String text = d.getText(0, d.getLength());
			updateSearchResults(text);
		}
	};
	
	
	
	private List<VideoGameMatch> searchEnhanced(String text, List<VideoGame> videoGameList) {	
		SortedSet<VideoGameMatch> firstPass = searchTest(text, videoGameList);
		
		List<VideoGameMatch> secondPass = new ArrayList<VideoGameMatch>();
		
		float matchCutoff = Math.min((text.length() + 1) * 0.05f, 0.5f);
		
		int limit = Integer.MAX_VALUE; //100;
		for(Iterator<VideoGameMatch> it = firstPass.iterator(); it.hasNext() && limit > 0; limit--) {
			VideoGameMatch vgm = it.next();
			if(vgm.getScore() < matchCutoff) {
				continue;
			}
			SmithWaterman sw = new SmithWaterman(text, vgm.getVideoGame().getTitle());
			vgm.setScore(sw.computeSmithWaterman());
			secondPass.add(vgm);
		}
		
		//Collections.sort(secondPass, Collections.reverseOrder());
		
		return secondPass;
	}
	
	private SortedSet<VideoGameMatch> searchTest(String text, List<VideoGame> videoGameList) {
		// TODO optimize this by pruning the initial result set, try adding to an array and sorting at the end, etc.
		
		StringDistance editDistance = new LevenshteinDistance();
		//StringDistance editDistance = new NGramDistance(3);
		
		SortedSet<VideoGameMatch> videoGameAutocomplete = new TreeSet<VideoGameMatch>(); // Collections.reverseOrder()
		for(VideoGame vg : videoGameList) {
			
			String queryString = text.toLowerCase();
			String matchString = vg.getTitle().toLowerCase();
			
			float score = editDistance.getDistance(queryString, matchString);
			
			//score += StringUtils.getFuzzyDistance(matchString, queryString, Locale.US);
			videoGameAutocomplete.add(new VideoGameMatch(vg, score));
		}
		
		return videoGameAutocomplete;
	}
	
	private void cleanup() {
		
	}
}
