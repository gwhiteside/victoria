package net.georgewhiteside.victoria;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import net.georgewhiteside.victoria.tables.PriceTableModel;
import net.georgewhiteside.victoria.tables.PriceTableModel.PriceRow;
import net.georgewhiteside.victoria.tables.SearchRow;
import net.georgewhiteside.victoria.tables.SearchTableModel;

import org.simmetrics.StringMetric;
import org.simmetrics.metrics.JaroWinkler;
import org.simmetrics.simplifiers.Simplifiers;
import org.simmetrics.builders.StringMetricBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.JComboBox;

/*
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
 * 
 */

// http://stackoverflow.com/questions/19012691/set-column-width-of-jtable-by-percentage

@SuppressWarnings("serial")
public class MainWindow {
	private String title = "VICTORIA";

	private JFrame frame;
	private JTextField textSearch;
	private JTableCustom tableSearch;
	private JTableCustom tablePrice;
	
	private QueryEditor queryEditor;
	
	private Database database;
	private EbayMiner ebay;
	private StringMetric stringMetric;
	
	Logger log = LoggerFactory.getLogger(this.getClass());

	BackgroundUpdater updater;
	
	/**
	 * Create the application.
	 */
	public MainWindow() {
		log.info("Creating main window...");
		
		Config config = Config.getInstance();
		
		final String dbUrl = config.getProperty(Config.DB_URL);
		final String dbUser = config.getProperty(Config.DB_USER);
		final String dbPass = config.getProperty(Config.DB_PASS);
		
		database = new Database(dbUrl, dbUser, dbPass);
		
		ebay = new EbayMiner(config.getProperty(Config.EBAY_APP_ID), config.getProperty(Config.POSTAL_CODE));
		ebay.setIgnoreCountries("CA", "AU"); // shipping charges suck for Canada and Australia
		
		stringMetric = StringMetricBuilder
			.with(new JaroWinkler())
			.simplify(Simplifiers.toLowerCase())
			.build();
		
		initialize();
		//textSearch.setEnabled(false);
		frame.setVisible(true);
		
		// background updater (runs immediately then retriggers daily) to check database entries; anything older than, say, a month, gets updated
		updater = new BackgroundUpdater(database, ebay);
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
				shutdown();
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(8, 8, 8, 8));
		frame.getContentPane().add(panel);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {100, 100, 0};
		gridBagLayout.rowHeights = new int[] {20, 0, 20, 20, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
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
			public void actionPerformed(ActionEvent e) { autocompleteSelect(); }
		});
		
		GridBagConstraints gbc_textSearch = new GridBagConstraints();
		gbc_textSearch.fill = GridBagConstraints.BOTH;
		gbc_textSearch.insets = new Insets(0, 0, 5, 5);
		gbc_textSearch.gridx = 0;
		gbc_textSearch.gridy = 0;
		panel.add(textSearch, gbc_textSearch);
		
		JComboBox comboBox = new JComboBox();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		panel.add(comboBox, gbc_comboBox);
		
		
		
		final JLabel labelTotal = new JLabel("");
		labelTotal.setHorizontalAlignment(SwingConstants.RIGHT);
		labelTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		GridBagConstraints gbc_labelTotal = new GridBagConstraints();
		gbc_labelTotal.insets = new Insets(0, 0, 5, 0);
		gbc_labelTotal.fill = GridBagConstraints.BOTH;
		gbc_labelTotal.ipadx = 8;
		gbc_labelTotal.gridx = 1;
		gbc_labelTotal.gridy = 1;
		panel.add(labelTotal, gbc_labelTotal);
		
		
		
		tableSearch = new JTableCustom();
		tableSearch.setModel(new SearchTableModel());
		JScrollPane scrollSearch = new JScrollPane(tableSearch);
		
		GridBagConstraints gbc_scrollSearch = new GridBagConstraints();
		gbc_scrollSearch.weightx = 0.5;
		gbc_scrollSearch.insets = new Insets(0, 0, 5, 5);
		gbc_scrollSearch.fill = GridBagConstraints.BOTH;
		gbc_scrollSearch.gridx = 0;
		gbc_scrollSearch.gridy = 2;
		panel.add(scrollSearch, gbc_scrollSearch);
		
		
		
		tablePrice = new JTableCustom();
		final PriceTableModel priceTableModel = new PriceTableModel(database, ebay);
		priceTableModel.addTableModelListener(new TableModelListener() {
			NumberFormat format = NumberFormat.getCurrencyInstance();
			@Override
			public void tableChanged(TableModelEvent e) {
				labelTotal.setText(format.format(priceTableModel.getPriceTotal()));
			}
		});
		tablePrice.setModel(priceTableModel);
		
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem editQueryItem = new JMenuItem("Edit query...");
		editQueryItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// query editor is a modal dialog, so this is all done on the dispatch thread
				// serially to enforce sane program flow
				
				int rowIndex = tablePrice.getSelectedRow();
				
				if(rowIndex == -1) {
					return;
				}
				
				PriceRow priceRow = ((PriceTableModel)tablePrice.getModel()).getRow(rowIndex);
				VideoGame vg = priceRow.getVideoGame();
				
				queryEditor.setupAndShow(vg.getTitle(), database.getSearchQuery(vg));
				String newQuery = queryEditor.getQuery();
				
				// null means no change is intended to be made
				if(newQuery == null) {
					return;
				}
				
				// quick and dirty check to make sure we're not accidentally saving bogus data
				if(newQuery.length() > 10) {
					database.saveSearchQuery(vg, newQuery);
					priceRow.update(); // start pulling in data
					//((PriceTableModel)tablePrice.getModel()).updatePrice(vg); // update any duplicate rows immediately
				}
			}
		});
		popupMenu.add(editQueryItem);
		
		popupMenu.addSeparator();
		
		JMenuItem removeItem = new JMenuItem("Remove");
		removeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int rowIndex = tablePrice.getSelectedRow();
				
				if(rowIndex == -1) {
					return;
				}
				
				PriceTableModel model = (PriceTableModel) tablePrice.getModel();
				model.removeRow(rowIndex);
			}
		});
		popupMenu.add(removeItem);
		
		tablePrice.setComponentPopupMenu(popupMenu);
		JScrollPane scrollPrice = new JScrollPane(tablePrice);
		
		GridBagConstraints gbc_scrollPrice = new GridBagConstraints();
		gbc_scrollPrice.gridheight = 2;
		gbc_scrollPrice.weightx = 0.5;
		gbc_scrollPrice.fill = GridBagConstraints.BOTH;
		gbc_scrollPrice.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPrice.gridx = 1;
		gbc_scrollPrice.gridy = 2;
		panel.add(scrollPrice, gbc_scrollPrice);
		
		textSearch.addMouseListener(new ContextMenuMouseListener());
		//textArea.setComponentPopupMenu(new BasicContextMenu());
		
		queryEditor = new QueryEditor(frame);
		
		
		

		
		priceTableModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				// just update the total on any change; sloppy but effective
				double total = priceTableModel.getPriceTotal();
				labelTotal.setText(NumberFormat.getCurrencyInstance().format(total));
			}
		});
	}
	
	
	
	private void autocompleteSelect() {
		int selectedRow = tableSearch.getSelectedRow();
		
		if(selectedRow == -1) {
			return;
		}
		
		SearchTableModel searchModel = (SearchTableModel) tableSearch.getModel();
		VideoGame vg = searchModel.getVideoGameByRow(selectedRow);
		
		PriceTableModel priceModel = (PriceTableModel) tablePrice.getModel();
		priceModel.addRow(vg);
		
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
		
		tableSearch.setSelectedRow(newRow);
		tableSearch.scrollRectToVisible(tableSearch.getCellRect(newRow, 0, true));
	}
	
	
	
	private void updateSearchResults(String query) {
		SearchTableModel model = (SearchTableModel) tableSearch.getModel();
		model.clear();
		
		if(query.length() == 0) {
			return;
		}
		
		long startTime = System.nanoTime();
		List<SearchRow> autocomplete = searchSimMetrics(query, database.getAllVideoGames());
		
		long endTime = (System.nanoTime() - startTime) / 1000000;
		
		log.debug("Autocomplete match time: {}ms", endTime);
		
		int limit = Integer.MAX_VALUE;
		for(SearchRow result : autocomplete) {
			if(limit-- <= 0) {
				break;
			}
			
			model.addRow(result);
		}
		
		if(tableSearch.getRowCount() > 0) {
			tableSearch.setSelectedRow(0); // select the first item by default when typing a search string
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
	
	
	
	private List<SearchRow> searchSimMetrics(String text, List<VideoGame> videoGameList) {
		List<SearchRow> matches = new ArrayList<SearchRow>();
		
		// good results with a Smith-Waterman metric
		
		//float matchCutoff = Math.min((text.length() + 1) * 0.05f, 0.5f);
		float matchCutoff = 0.75f;
		
		for(VideoGame vg : videoGameList) {
			float score = stringMetric.compare(text, vg.getTitle());
			if(score >= matchCutoff) {
				matches.add(new SearchRow(vg, score));
			}
		}
		
		Collections.sort(matches, Collections.reverseOrder());
		
		return matches;
	}
	
	
	
	private void shutdown() {
		log.info("Shutting down...");
		updater.shutdown();
	}
}
