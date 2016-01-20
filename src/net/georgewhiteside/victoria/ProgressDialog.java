package net.georgewhiteside.victoria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;

public class ProgressDialog extends JDialog {

	private JProgressBar progressBar;
	
	public ProgressDialog() {
		this(null);
	}

	public ProgressDialog(Frame owner) {
		super(owner);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
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
		
		JLabel lblCharactersPer = new JLabel("Updating sale data...");
		GridBagConstraints gbc_lblCharactersPer = new GridBagConstraints();
		gbc_lblCharactersPer.fill = GridBagConstraints.BOTH;
		gbc_lblCharactersPer.insets = new Insets(0, 0, 5, 0);
		gbc_lblCharactersPer.gridx = 0;
		gbc_lblCharactersPer.gridy = 0;
		panel.add(lblCharactersPer, gbc_lblCharactersPer);
		
		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		panel.add(progressBar, gbc_progressBar);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] {100, 100};
		gbl_panel_1.rowHeights = new int[] {0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JButton btnHide = new JButton("Hide");
		btnHide.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnHide = new GridBagConstraints();
		gbc_btnHide.insets = new Insets(0, 8, 0, 5);
		gbc_btnHide.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnHide.gridx = 0;
		gbc_btnHide.gridy = 0;
		panel_1.add(btnHide, gbc_btnHide);
		
		btnHide.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setAlignmentX(Component.CENTER_ALIGNMENT);
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 8, 0, 0);
		gbc_btnCancel.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		panel_1.add(btnCancel, gbc_btnCancel);
		
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}
	
	public void setMaximum(int value) {
		progressBar.setMaximum(value);
	}
	
	public void setMinimum(int value) {
		progressBar.setMinimum(value);
	}
	
	public void setProgress(int value) {
		progressBar.setValue(value);
	}
	
	@Override
	public void setVisible(boolean b) {
		setLocationRelativeTo(getOwner());
		super.setVisible(b);
	}
}
