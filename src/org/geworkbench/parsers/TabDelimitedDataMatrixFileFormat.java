package org.geworkbench.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ProgressMonitorInputStream;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.parsers.resources.Resource;
import org.geworkbench.util.AffyAnnotationUtil;
import org.geworkbench.util.Util;

/**
 * Sequence and Pattern Plugin
 * 
 * @author yc2480
 * @version $Id$
 * 
 */
public class TabDelimitedDataMatrixFileFormat extends DataSetFileFormat {

	static Log log = LogFactory.getLog(TabDelimitedDataMatrixFileFormat.class);

	private static final String commentSign1 = "#";
	private static final String commentSign2 = "!";
	private static final String columnSeperator = "\t";
	private static final String lineSeperator = "\n";
	private static final String[] maExtensions = { "txt", "tsv" };
	private static final String duplicateLabelModificator = "_2";

	ExpressionResource resource = new ExpressionResource();
	TabDelimitedFilter maFilter = null;
	private int possibleMarkers = 0;

	/**
	 * 
	 */
	public TabDelimitedDataMatrixFileFormat() {
		formatName = "Tab-Delimited Data Matrix";
		maFilter = new TabDelimitedFilter();
		Arrays.sort(maExtensions);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.FileFormat#getResource(java.io.File)
	 */
	public Resource getResource(File file) {
		try {
			resource.setReader(new BufferedReader(new FileReader(file)));
			resource.setInputFileName(file.getName());
		} catch (IOException ioe) {
			ioe.printStackTrace(System.err);
		}
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.FileFormat#getFileExtensions()
	 */
	public String[] getFileExtensions() {
		return maExtensions;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.FileFormat#checkFormat(java.io.File)
	 *      FIXME In here we should also check (among other things) that: The
	 *      values of the data points respect their expected type. IMPORTANT!
	 *      After Mantis #1551, this step will be required even if you don't
	 *      want to checkFormat.
	 */
	public boolean checkFormat(File file) throws InterruptedIOException {
		boolean columnsMatch = true;
		boolean noDuplicateMarkers = true;
		boolean noDuplicateArrays = true;
		BufferedReader reader = null;
		ProgressMonitorInputStream progressIn = null;
		try {
			FileInputStream fileIn = new FileInputStream(file);
		    progressIn = new ProgressMonitorInputStream(
					null, "Checking File Format", fileIn);
			reader = new BufferedReader(new InputStreamReader(
					progressIn));
			 
			String line = null;
			int totalColumns = 0;
			List<String> markers = new ArrayList<String>();
			List<String> arrays = new ArrayList<String>();
			int lineIndex = 0;
			int headerLineIndex = 0; 
			
			while ((line = reader.readLine()) != null) { // for each line
				if ((line.indexOf(commentSign1) < 0)
						&& (line.indexOf(commentSign2) != 0)
						&& (line.length() > 0)) {// we'll skip comments and
					// anything before header
					if (headerLineIndex == 0)// no header detected yet, then
						// this is the header.
						headerLineIndex = lineIndex;
					String token = null;
					int columnIndex = 0;
					int accessionIndex = 0;
					StringTokenizer st = new StringTokenizer(line,
							columnSeperator + lineSeperator);
					while (st.hasMoreTokens()) { // for each column
						token = st.nextToken().trim();
						if (token.equals("")) {// header
							accessionIndex = columnIndex;
						} else if ((headerLineIndex > 0) && (columnIndex == 0)) {
							/*
							 * if this line is after header, then first column
							 * should be our marker name
							 */
							if (markers.contains(token)) {// duplicate markers
								noDuplicateMarkers = false;
								log.error("Duplicate Markers: "+token);
								return false;
							} else {
								markers.add(token);
							}
						} else if (headerLineIndex == lineIndex) {
							/*
							 * this is header line
							 */
							if (arrays.contains(token)) {// duplicate arrays
								noDuplicateArrays = false;
								log.error("Duplicate Arrays labels " + token
										+ " in " + file.getName());
								return false;
							} else {
								arrays.add(token);
							}
						}
						columnIndex++;
						lineIndex++;
					}
					/* check if column match or not */
					if (headerLineIndex > 0) {
						/*
						 * if this line is real data, we assume lines after
						 * header are real data. (we might have bug here)
						 */
						if (totalColumns == 0) /* not been set yet */
							totalColumns = columnIndex - accessionIndex;
						else if (columnIndex != totalColumns)// if not equal
							columnsMatch = false;
					}
				}
			}
			possibleMarkers = markers.size();
			fileIn.close();
		
		} catch (java.io.InterruptedIOException ie) {
			if ( progressIn.getProgressMonitor().isCanceled())
			{			    
				throw ie;				 
			}			 
			else
			   ie.printStackTrace();
		} catch (Exception e) {
			log.error(formatName+" file format exception: " + e);
			e.printStackTrace();
			 
		} finally {
    		try {
				reader.close();
			} catch (IOException e) {
				log.error(formatName+" file reader close exception: " + e);
				e.printStackTrace();
			}
		}
		if (columnsMatch && noDuplicateMarkers && noDuplicateArrays)
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.microarray.DataSetFileFormat#getDataFile(java.io.File)
	 */
	@Override
	public DSDataSet<? extends DSBioObject> getDataFile(File file) throws InputFileFormatException, InterruptedIOException{
		  
		  return (DSDataSet<? extends DSBioObject>) getMArraySet(file, null);
	    
	}

	@Override
	public DSDataSet<? extends DSBioObject> getDataFile(File file,
			String compatibilityLabel) throws InputFileFormatException,
			InterruptedIOException {
		  return (DSDataSet<? extends DSBioObject>) getMArraySet(file, compatibilityLabel);
	}
	 
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.FileFormat#getMArraySet(java.io.File)
	 */
	private CSMicroarraySet getMArraySet(File file, String compatibilityLabel)
			throws InputFileFormatException, InterruptedIOException {

		CSMicroarraySet maSet = new CSMicroarraySet();
		try
		{
			maSet = getMArraySetBase(file, compatibilityLabel, true);
		} catch (InputFileFormatException e) {
			throw e;
		} catch (InterruptedIOException ie) {
			throw ie;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return maSet;
	}
	
	/*
	 * (non-Javadoc)
	 * get DataSet from file without asking for annotation
	 */
	public DSDataSet<? extends DSBioObject> getDataFileSkipAnnotation(File file) throws InputFileFormatException, InterruptedIOException{
	
		CSMicroarraySet maSet = new CSMicroarraySet();
		try
		{
			maSet = getMArraySetBase(file, null, false);
		} catch (InputFileFormatException e) {
			throw e;
		} catch (InterruptedIOException ie) {
			throw ie;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return maSet;	    
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.FileFormat#getMArraySet(java.io.File)
	 * ask for annotation if annotation is true
	 * skip annotation if annotation is false
	 */
	private CSMicroarraySet getMArraySetBase(File file, String compatibilityLabel, boolean annotation)
			throws InputFileFormatException, InterruptedIOException {

		try
		{
		 if (!checkFormat(file)) {
			log
					.info("TabDelimitedDataMatrixFileFormat::getMArraySet - "
							+ "Attempting to open a file that does not comply with the "
							+ "Tab-Delimited Data Matrix file format.");
			 throw new InputFileFormatException(
					"Attempting to open a file that does not comply with the "
							+ "Tab-Delimited Data Matrix file format.");
		 }
		} catch (InterruptedIOException ie) {
			throw ie;
		}
		CSMicroarraySet maSet = new CSMicroarraySet();
		String fileName = file.getName();		
		maSet.setLabel(fileName);
		maSet.setFile(file);
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			if (in != null) {
				String header = in.readLine();
				if (header == null) {
					throw new InputFileFormatException("File is empty.");
				}
				while (header != null
						&& (header.startsWith(commentSign1) || header
								.startsWith(commentSign2))
						|| StringUtils.isEmpty(header)) {
					header = in.readLine();
				}
				if (header == null) {
					throw new InputFileFormatException(
							"File is empty or consists of only comments.\n"
									+ formatName+" format expected");
				}

				/* for mantis issue:1349 */
				header = StringUtils.replace(header, "\"", "");

				StringTokenizer headerTokenizer = new StringTokenizer(header,
						columnSeperator, false);
				int n = headerTokenizer.countTokens();
				if (n <= 1) {
					throw new InputFileFormatException(
							"Attempting to open a file that does not comply with the Tab-Delimited Data Matrix format.\n"
									+ "Invalid header: " + header);
				}
				n -= 1;

				String line = in.readLine();
				line = StringUtils.replace(line, "\"", "");
				int m = 0;

				/* Skip first token */
				headerTokenizer.nextToken();

				HashSet<String> arrayNames = new HashSet<String>();
				for (int i = 0; i < n; i++) {
					String arrayName = headerTokenizer.nextToken();
					//assign unique names for duplicated array labels
					arrayName = Util.getUniqueName(arrayName, arrayNames);
					arrayNames.add(arrayName);

					CSMicroarray array = new CSMicroarray(i, possibleMarkers,
							arrayName,
							DSMicroarraySet.affyTxtType);
					maSet.add(array);
					/*
					 * FIXME: this will only fix one duplicate per unique label.
					 * should handle unlimited duplicate.
					 */
					if (maSet.size() != (i + 1)) {
						log.info("We got a duplicate label of array");
						array.setLabel(array.getLabel()
								+ duplicateLabelModificator);
						maSet.add(array);
					}
				}
				while ((line != null) // modified for mantis issue: 1349
						&& (!StringUtils.isEmpty(line))
						&& (!line.trim().startsWith(commentSign2))) {
					String[] tokens = line.split(columnSeperator);
					int length = tokens.length;
					if (length != (n + 1)) {
						log.error("Warning: Could not parse line #" + (m + 1)
								+ ". Line should have " + (n + 1)
								+ " lines, has " + length + ".");
						if ((m == 0) && (length == n + 2))
							// TODO Is this file from R's RMA, without first
							// column in header?
							throw new InputFileFormatException(
									"Attempting to open a file that does not comply with the "
											+ formatName+ " format."
											+ "\n"
											+ "Warning: Could not parse line #"
											+ (m + 1)
											+ ". Line should have "
											+ (n + 1)
											+ " columns, but it has "
											+ length
											+ ".\n"
											+ "This file looks like R's RMA format, which needs manually add a tab in the beginning of the header to make it a valid RMA format.");
						else
							throw new InputFileFormatException(
									"Attempting to open a file that does not comply with the "
											+ formatName+" format." + "\n"
											+ "Warning: Could not parse line #"
											+ (m + 1) + ". Line should have "
											+ (n + 1) + " columns, but it has "
											+ length + ".");
					}
					String markerName = new String(tokens[0].trim());
					CSExpressionMarker marker = new CSExpressionMarker(m);
					marker.setLabel(markerName);
					maSet.getMarkers().add(m, marker);
					for (int i = 0; i < n; i++) {
						String valString = "";
						if ((i + 1) < tokens.length) {
							valString = tokens[i + 1];
						}
						if (valString.trim().length() == 0) {
							// put values directly into CSMicroarray inside of
							// maSet
							Float v = Float.NaN;
							CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(
									v);
							DSMicroarray microarray = (DSMicroarray)maSet.get(i);
							microarray.setMarkerValue(m, markerValue);
							if (v.isNaN()) {
								markerValue.setMissing(true);
							} else {
								markerValue.setPresent();
							}
						} else {
							float value = Float.NaN;
							try {
								value = Float.parseFloat(valString);
							} catch (NumberFormatException nfe) {
								log.info("We expect a number, but we got: "
										+ valString);
							}
							// put values directly into CSMicroarray inside of
							// maSet
							Float v = value;
							CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(
									v);
							try {
								DSMicroarray microarray = (DSMicroarray)maSet.get(i);
								microarray.setMarkerValue(m, markerValue);
							} catch (IndexOutOfBoundsException ioobe) {
								log.error("i=" + i + ", m=" + m);
							}
							if (v.isNaN()) {
								markerValue.setMissing(true);
							} else {
								markerValue.setPresent();
							}
						}
					}
					m++;
					line = in.readLine();
					line = StringUtils.replace(line, "\"", "");
				}
				// Set chip-type
				if (compatibilityLabel == null) {
					if (annotation)
						AffyAnnotationUtil.matchAffyAnnotationFile(maSet);
				} else {
					maSet.setCompatibilityLabel(compatibilityLabel);
				}
				
				for (DSGeneMarker marker : maSet.getMarkers()) {
					String token = marker.getLabel();
					String[] locusResult = AnnotationParser.getInfo(token,
							AnnotationParser.LOCUSLINK);
					String locus = "";
					if ((locusResult != null)
							&& (!locusResult[0].trim().equals(""))) {
						locus = locusResult[0].trim();
					}
					if (locus.compareTo("") != 0) {
						try {
							marker.setGeneId(Integer.parseInt(locus));
						} catch (NumberFormatException e) {
							log.info("Couldn't parse locus id: " + locus);
						}
					}
					String[] geneNames = AnnotationParser.getInfo(token,
							AnnotationParser.ABREV);
					if (geneNames != null) {
						marker.setGeneName(geneNames[0]);
					}

					marker.getUnigene().set(token);

				}
			}
		} catch (InputFileFormatException e) {
			throw e;
		} catch (InterruptedIOException ie) {
			throw ie;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
    		try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		maSet.getMarkers().correctMaps();
		return maSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.FileFormat#getFileFilter()
	 */
	public FileFilter getFileFilter() {
		return maFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.microarray.DataSetFileFormat#getDataFile(java.io.File[])
	 */
	public DSDataSet<? extends DSBioObject> getDataFile(File[] files) {
		// org.geworkbench.components.parsers.microarray.DataSetFileFormat
		// abstract method
		throw new UnsupportedOperationException(
				"Method getDataFile(File[] files) not yet implemented.");
	}

	private class TabDelimitedFilter extends FileFilter {

		public String getDescription() {
			return getFormatName();
		}

		public boolean accept(File f) {
			boolean returnVal = false;
			for (int i = 0; i < maExtensions.length; ++i)
				if (f.isDirectory() || f.getName().toLowerCase().endsWith(maExtensions[i])) {
					return true;
				}
			return returnVal;
		}
	}
}
