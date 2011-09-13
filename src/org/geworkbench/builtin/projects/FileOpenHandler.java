package org.geworkbench.builtin.projects;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InterruptedIOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.engine.config.rules.GeawConfigObject;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.parsers.AdjacencyMatrixFileFormat;
import org.geworkbench.parsers.DataSetFileFormat;
import org.geworkbench.parsers.FileFormat;
import org.geworkbench.parsers.InputFileFormatException;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;

/**
 * This class is refactored out of ProjectPanel to handle the file open action,
 * especially tackles the progress bar requirement for multiple files.
 *
 * @author zji
 * @version $Id$
 *
 */
public class FileOpenHandler {
	static Log log = LogFactory.getLog(FileOpenHandler.class);

	private final File[] dataSetFiles;
	private final FileFormat inputFormat;
	private final boolean mergeFiles;
	private final ProjectPanel projectPanel;
	private final JProgressBar projectPanelProgressBar;
	
	private static final String OUT_OF_MEMORY_MESSAGE = "In order to prevent data corruption,\n"
			+ "it is strongly suggested that you\n"
			+ "restart geWorkbench now.\n"
			+ "To increase memory available to\n"
			+ "geWorkbench, please refer to\n"
			+ "geWorkbench documentation.\n\n"
			+ "Exit geWorkbench?";
	private static final String OUT_OF_MEMORY_MESSAGE_TITLE = "Java total heap memory exception";

	FileOpenHandler(final File[] dataSetFiles, final FileFormat inputFormat,
			final boolean mergeFiles)
			throws InputFileFormatException {
		this.dataSetFiles = dataSetFiles;
		this.inputFormat = inputFormat;

		this.mergeFiles = mergeFiles;

		projectPanel = ProjectPanel.getInstance();
		projectPanelProgressBar = projectPanel.getProgressBar();
		projectPanelProgressBar.setStringPainted(true);
		projectPanelProgressBar.setString("Loading");
		projectPanelProgressBar.setIndeterminate(true);
		ProjectPanel.getInstance().getComponent().setCursor(Cursor
				.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}

	/**
	 *
	 */
	public void openFiles() {
		OpenMultipleFileTask task = new OpenMultipleFileTask();

		ProgressBarDialog pb = new ProgressBarDialog(GeawConfigObject.getGuiWindow(),
				"Files are being opened.", task);
		pb.setMessageAndNote(String.format("Completed %d out of %d files.", 0,
				dataSetFiles.length), String.format(
				"Currently being processed is %s.", dataSetFiles[0].getName()));
		
		task.progressBarDialog = pb;
		task.execute();

		task.addPropertyChangeListener(pb);
	}

	private class ProgressBarDialog extends JDialog implements ActionListener,
			PropertyChangeListener {
		private static final long serialVersionUID = -3259066552401380723L;

		private JLabel message = null;
		private JLabel note = null;
		private JButton cancelButton = null;

		private final OpenMultipleFileTask task;
		private void setMessageAndNote(String message, String note) {
			this.message.setText(message);
			this.note.setText(note);
			this.message.invalidate();
		}

		ProgressBarDialog(JFrame ownerFrame, String title, final OpenMultipleFileTask task) {
			// it is important to make it non-modal - for the same reason
			// customizing dialog is necessary
			// because this class FileOpenHandler is used within a file chooser
			// event handler, so it would leave the file open dialog open
			// otherwise
			super(ownerFrame, title, false);
			
			this.task = task;

			this.setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
			JPanel leftPanel = new JPanel();
			add(leftPanel);
			leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			JPanel rightPanel = new JPanel();
			add(rightPanel);
			rightPanel.setAlignmentY(Component.TOP_ALIGNMENT);
			rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

			JLabel icon = new JLabel(UIManager
					.getIcon("OptionPane.informationIcon"));
			icon.setAlignmentY(Component.TOP_ALIGNMENT);
			leftPanel.add(icon);

			// add two lines of messages
			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			message = new JLabel("");
			message.setAlignmentX(Component.LEFT_ALIGNMENT);
			rightPanel.add(message);
			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			note = new JLabel("");
			note.setAlignmentX(Component.LEFT_ALIGNMENT);
			rightPanel.add(note);

			JProgressBar progress = new JProgressBar(0, 100);
			progress.setIndeterminate(true);
			progress.setMinimumSize(new Dimension(300, 22));
			progress.setPreferredSize(new Dimension(300, 50));

			progress.setAlignmentX(Component.LEFT_ALIGNMENT);
			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			rightPanel.add(progress);

			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(this);
			cancelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
			rightPanel.add(cancelButton);
			rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

			pack();
			setLocationRelativeTo(ownerFrame);

			// disable exit
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

			//setVisible(true);
		}

		public void actionPerformed(ActionEvent e) {
			if (cancelButton == e.getSource()) {
				task.cancel(true);

				// task is really stopped when checking isCancel between reading
				// files, but UI should show canceled
				projectPanelProgressBar.setString("");
				projectPanelProgressBar.setIndeterminate(false);
				projectPanel.getComponent().setCursor(Cursor
						.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				dispose();
			}
		}

		/**
		 * Invoked when task's progress property changes.
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress" == evt.getPropertyName()) {
				int progress = (Integer) evt.getNewValue();
				String note = "";
				if (progress >= 0 && progress < dataSetFiles.length)
					note = String.format("Currently being processed is %s.",
							dataSetFiles[progress].getName());
				setMessageAndNote(String.format(
						"Completed %d out %d files.", progress,
						dataSetFiles.length), note);
			}
		}
	} // end of class ProgressBarDialog

	private class OpenMultipleFileTask extends SwingWorker<Void, Void> {
		ProgressBarDialog progressBarDialog;

		/*
		 * (non-Javadoc)
		 * @see org.geworkbench.util.threading.SwingWorker#done()
		 */
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected void done() {

			if (mergeFiles && dataSets[0] instanceof DSMicroarraySet) {
				DSMicroarraySet[] maSets = new DSMicroarraySet[dataSets.length];

				for (int i = 0; i < dataSets.length; i++) {
					maSets[i] = (DSMicroarraySet) dataSets[i];
				}
				projectPanel.doMergeSets(maSets);
			} else {
				boolean selected = false;
				for (int i = 0; i < dataSets.length; i++) {
					DSDataSet set = dataSets[i];

					if (set == null) {
						log.info("null dataset encountered");
						continue;
					}

					// Do initial color context update if it is a microarray
					if (set instanceof DSMicroarraySet) {
						ProjectPanel
								.addColorContext((DSMicroarraySet<DSMicroarray>) set);
					}

					if (set instanceof AdjacencyMatrixDataSet) {
						// adjacency matrix as added as a sub node
						AdjacencyMatrixDataSet adjMatrixDS = (AdjacencyMatrixDataSet) set;
						ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
								"Adjacency Matrix loaded", null, adjMatrixDS);
						projectPanel.addDataSetSubNode(adjMatrixDS);
						projectPanel.publishProjectNodeAddedEvent(event);
					} else {
						if (!selected) {
							projectPanel.addDataSetNode(set, true);
							selected = true;
						} else {
							projectPanel.addDataSetNode(set, false);
						}
					}
				}
			}

			progressBarDialog.dispose();
			projectPanelProgressBar.setString("");
			projectPanelProgressBar.setIndeterminate(false);
			projectPanel.getComponent().setCursor(Cursor
					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		@SuppressWarnings("rawtypes")
		private DSDataSet[] dataSets = null;
		/*
		 * (non-Javadoc)
		 * @see org.geworkbench.util.threading.SwingWorker#doInBackground()
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		protected Void doInBackground() throws Exception {
			int n = dataSetFiles.length;
			dataSets = new DSDataSet[n];

			DataSetFileFormat dataSetFileFormat = (DataSetFileFormat) inputFormat;
			if (dataSetFiles.length == 1) {
				try {
					// adjacency matrix need access to project node
					if (dataSetFileFormat instanceof AdjacencyMatrixFileFormat) {
						ProjectTreeNode selectedNode = ProjectPanel.getInstance()
								.getSelection().getSelectedNode();
						// it has to be a project node
						if (selectedNode instanceof ProjectNode) {
							ProjectNode projectNode = (ProjectNode) selectedNode;
							((AdjacencyMatrixFileFormat) dataSetFileFormat)
									.setProjectNode(projectNode);
						}
					}

					dataSets[0] = dataSetFileFormat.getDataFile(dataSetFiles[0]);
					dataSets[0].setAbsPath(dataSetFiles[0].getAbsolutePath());
				} catch (OutOfMemoryError er) {
					log.warn("Loading a single file memory error: " + er);
					int response = JOptionPane.showConfirmDialog(null,
							OUT_OF_MEMORY_MESSAGE, OUT_OF_MEMORY_MESSAGE_TITLE,
							JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE);
					if (response == JOptionPane.YES_OPTION) {
						System.exit(1);
					}
				} catch (InputFileFormatException iffe) {
					// Let the user know that there was a problem
					// parsing the file.
					JOptionPane.showMessageDialog(null, iffe.getMessage(),
							"Parsing Error", JOptionPane.ERROR_MESSAGE);
				}
				 catch (InterruptedIOException ie) {
					 projectPanelProgressBar.setString("");
					 projectPanelProgressBar.setIndeterminate(false);
					 projectPanel.getComponent().setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				       if ( ie.getMessage().equals("progress"))
					        return null;
				       else
				    	   ie.printStackTrace();

				 }
			} else {
				// multiple file selection is not supported for adjacency matrix
				if (dataSetFileFormat instanceof AdjacencyMatrixFileFormat) {
					JOptionPane.showMessageDialog(null, "multiple file selection is not supported for adjacency matrix");
					return null;
				}

				// watkin - none of the file filters implement the
				// multiple getDataFile method.
				// dataSets[0] =
				// ((DataSetFileFormat)inputFormat).getDataFile(dataSetFiles);
				// If the data sets are microarray sets, then merge them
				// if "merge files" is checked

				// invoking AnnotationParser.matchChipType with null dataset is
				// different from the previous algorithm.
				// also notice that this will block
				String chipType = AnnotationParser.matchChipType(null, "", false);; //FileOpenHandler.this.chipType;
				progressBarDialog.setVisible(true);

				for (int i = 0; i < dataSetFiles.length; i++) {
					if (isCancelled()) {
						return null;
					}
					File dataSetFile = dataSetFiles[i];
					try {
						try {
							dataSets[i] = dataSetFileFormat.getDataFile(dataSetFile, chipType);
							AnnotationParser.setChipType(dataSets[i], chipType);
							dataSets[i].setAbsPath(dataSetFiles[i].getAbsolutePath());
							if(dataSets[i] instanceof CSMicroarraySet) {
								((CSMicroarraySet)dataSets[i]).setAnnotationFileName(AnnotationParser.getLastAnnotationFileName());
							}
						} catch (OutOfMemoryError er) {
							log.warn("Loading multiple files memory error: " + er);
							int response = JOptionPane.showConfirmDialog(null,
									OUT_OF_MEMORY_MESSAGE, OUT_OF_MEMORY_MESSAGE_TITLE,
									JOptionPane.YES_NO_OPTION,
									JOptionPane.ERROR_MESSAGE);
							if (response == JOptionPane.YES_OPTION) {
								System.exit(1);
							}
						} catch (UnsupportedOperationException e) {
							log.warn("This data type doesn't support chip type overrides, will have to ask user again.");
							dataSets[i] = ((DataSetFileFormat) inputFormat)
									.getDataFile(dataSetFile);
						}
					} catch (InputFileFormatException iffe) {
						// Let the user know that there was a problem
						// parsing the file.
						JOptionPane
								.showMessageDialog(
										null,
										"The input file does not comply with the designated format.",
										"Parsing Error",
										JOptionPane.ERROR_MESSAGE);
						projectPanelProgressBar.setString("");
						projectPanelProgressBar.setIndeterminate(false);
						projectPanel.getComponent().setCursor(Cursor
								.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						return null;
					} // end of for loop
				    catch (InterruptedIOException ie) {
				    	projectPanelProgressBar.setString("");
				    	projectPanelProgressBar.setIndeterminate(false);
				    	projectPanel.getComponent().setCursor(Cursor
							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				       if ( ie.getMessage().equals("progress"))
					        return null;
				       else
				    	   ie.printStackTrace();

				     }

					setProgress(i + 1);
				}
			}

			return null;
		}

	}
}
