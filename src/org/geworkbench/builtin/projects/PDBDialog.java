package org.geworkbench.builtin.projects;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A popup window for user to enter a 4-digit PDB id;
 * the PDB file from RCSB website will be downloaded and displayed
 * 
 * @author mw2518
 * @version $Id: PDBDialog.java,v 1.4 2008-12-09 21:19:58 wangm Exp $
 * 
 */
class PDBDialog extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private Log log = LogFactory.getLog(this.getClass());

	private JTextField jt = new JTextField(4);
	public ProjectPanel pp = null;

	/*
	 * save pdb file from rcsb website to local disk, 
	 * let project panel open the downloaded pdb file
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		String pdbid = jt.getText();
		log.info("entered pdb: " + pdbid);
		String url = "http://www.rcsb.org/pdb/files/" + pdbid + ".pdb";
		String contents = getContent(url);

		String dir = LoadData.getLastDataDirectory() + "/webpdb/";
		File nd = new File(dir);
		if (!nd.exists()) {
			nd.mkdir();
		}
		String downloaded = dir + pdbid + ".pdb";
		log.info("download to: " + downloaded);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(downloaded));
			bw.write(contents);
			bw.close();

			File df = new File(downloaded);
			File[] fs = new File[] { df };
			pp.fileOpenAction(fs,
					new org.geworkbench.components.parsers.PDBFileFormat(),
					false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		setVisible(false);
		dispose();
	}

	/**
	 * specify parent project panel
	 * 
	 * @param mainpp
	 */
	public PDBDialog(ProjectPanel mainpp) {
		super("Open RCSB PDB File");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		pp = mainpp;
	}

	/*
	 * ask for user input and register with action listener
	 */
	public void create() {
		JButton btn = new JButton("Search RCSB PDB");
		btn.addActionListener(this);

		Container contentPane = getContentPane();
		contentPane.setLayout(new FlowLayout(FlowLayout.TRAILING));
		contentPane.add(new JLabel("Enter PDB ID:  "));

		contentPane.add(jt);
		contentPane.add(btn);
		pack();
		setVisible(true);
	}

	/**
	 * read web content from url fname, return as a string
	 * 
	 * @param fname
	 * @return
	 */
	private String getContent(String fname) {
		StringBuffer contents = null;
		try {
			URL url = new URL(fname);
			URLConnection uc = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(uc
					.getInputStream()));

			contents = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				contents.append(line);
				contents.append("\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.info("getContent notconnected error: " + fname);
		}
		return contents.toString();
	}

}
