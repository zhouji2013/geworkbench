package org.geworkbench.components.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ProgressMonitorInputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;

/**  
 * @author Nikhil
 */
public class GeoSeriesMatrixParser {

	static Log log = LogFactory.getLog(SOFTFileFormat.class);
	private static final String commentSign1 = "#";
	private static final String commentSign2 = "!";
	private static final String commentSign3 = "^";
	private static final String columnSeperator = "\t";
	private static final String lineSeperator = "\n";
	private static final String duplicateLabelModificator = "_2";

	CSExprMicroarraySet maSet = new CSExprMicroarraySet();
	List<String> markArrays = new ArrayList<String>();
	
	private int possibleMarkers = 0; 
    
	/*
	 * (non-Javadoc) 
	 * @see org.geworkbench.components.parsers.FileFormat#checkFormat(java.io.File)
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
					null, "Loading data from " + file.getName(), fileIn);
			reader = new BufferedReader(new InputStreamReader(
					progressIn));
			 
			String line = null;
			int totalColumns = 0;
			List<String> markers = new ArrayList<String>();
			List<String> arrays = new ArrayList<String>();
			int lineIndex = 0;
			int headerLineIndex = 0; 
			
			while ((line = reader.readLine()) != null) { // for each line
				
				/*
				 * Adding comments that start with '!' and '#' from the GEO SOFT file to the Experiment Information tab 
				 */
				if (line.startsWith(commentSign1) || line.startsWith(commentSign2)) {
					//Ignoring the lines that has '!series_matrix_table_begin' and '!series_matrix_table_end'
					if(!line.equalsIgnoreCase("!series_matrix_table_begin") && !line.equalsIgnoreCase("!series_matrix_table_end")) {
						maSet.addDescription(line.substring(1));
					}
				}
				String[] mark = null;
				mark = line.split("\t");
			    if(mark[0].equals("!Sample_title")){
			    	for (int i=1;i<mark.length;i++){
			    		markArrays.add(mark[i]);
			    	}	
			    }
				if ((line.indexOf(commentSign1) < 0)
						&& (line.indexOf(commentSign2) != 0)
						&& (line.indexOf(commentSign3) != 0)
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
							 * this is header line for RMA file
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
			log.error("GEO SOFT check file format exception: " + e);
			e.printStackTrace();
			 
		} finally {
    		try {
				reader.close();
			} catch (IOException e) {
				log.error("GEO SOFT file reader close exception: " + e);
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
	 * @see org.geworkbench.components.parsers.FileFormat#getMArraySet(java.io.File)
	 */
	@SuppressWarnings("unchecked")
	public DSMicroarraySet getMArraySet(File file)
			throws InputFileFormatException, InterruptedIOException {

		final int extSeperater = '.';

		try
		{
		 if (!checkFormat(file)) {
			log
					.info("SOFTFileFormat::getMArraySet - "
							+ "Attempting to open a file that does not comply with the "
							+ "GEO SOFT file format.");
			 throw new InputFileFormatException(
					"Attempting to open a file that does not comply with the "
							+ "SOFT file format.");
		 }
		} catch (InterruptedIOException ie) {
			throw ie;
		}
		String fileName = file.getName();
		int dotIndex = fileName.lastIndexOf(extSeperater);
		if (dotIndex != -1) {
			fileName = fileName.substring(0, dotIndex);
		}
		maSet.setLabel(fileName);
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
								.startsWith(commentSign2) || header
								.startsWith(commentSign3))
						|| StringUtils.isEmpty(header)) {
					header = in.readLine();
				}
				
				if (header == null) {
					throw new InputFileFormatException(
							"File is empty or consists of only comments.\n"
									+ "SOFT File Format expected");
				}
				
				

				
				header = StringUtils.replace(header, "\"", "");

				StringTokenizer headerTokenizer = new StringTokenizer(header,
						columnSeperator, false);
				int n = headerTokenizer.countTokens();
				if (n <= 1) {
					throw new InputFileFormatException(
							"Attempting to open a file that does not comply with the SOFT File format.\n"
									+ "Invalid header: " + header);
				}
				n -= 1;

				String line = in.readLine();
				line = StringUtils.replace(line, "\"", "");
				int m = 0;

				/* Skip first token */
				headerTokenizer.nextToken();
				int duplicateLabels = 0;

				for (int i = 0; i < n; i++) {
					
					String arrayName = headerTokenizer.nextToken();
					String markAnn = markArrays.get(i);
					String markAnn1 = markAnn.replace("\"", "");
					String arrayName1 = arrayName 
										+ " ("
										+markAnn1
										+")";
					CSMicroarray array = new CSMicroarray(i, possibleMarkers,
							arrayName1, null, null, false,
							DSMicroarraySet.affyTxtType);
					maSet.add(array);
					System.out.print("\n");
					
					if (maSet.size() != (i + 1)) {
						log.info("We got a duplicate label of array");
						array.setLabel(array.getLabel()
								+ duplicateLabelModificator);
						maSet.add(array);
						duplicateLabels++;
					}
				}
				while ((line != null) 
						&& (!StringUtils.isEmpty(line))
						&& (!line.trim().startsWith(commentSign2))) {
					String[] tokens = line.split(columnSeperator);
					int length = tokens.length;
					if (length != (n + 1)) {
						log.error("Warning: Could not parse line #" + (m + 1)
								+ ". Line should have " + (n + 1)
								+ " lines, has " + length + ".");
						if ((m == 0) && (length == n + 2))
							
							throw new InputFileFormatException(
									"Attempting to open a file that does not comply with the "
											+ "SOFT file format."
											+ "\n"
											+ "Warning: Could not parse line #"
											+ (m + 1)
											+ ". Line should have "
											+ (n + 1)
											+ " columns, but it has "
											+ length
											+ ".\n"
											+ "This file looks like R's SOFT format, which needs manually add a tab in the beginning of the header to make it a valid SOFT format.");
						else
							throw new InputFileFormatException(
									"Attempting to open a file that does not comply with the "
											+ "SOFT format." + "\n"
											+ "Warning: Could not parse line #"
											+ (m + 1) + ". Line should have "
											+ (n + 1) + " columns, but it has "
											+ length + ".");
					}
					String markerName = new String(tokens[0].trim());
					CSExpressionMarker marker = new CSExpressionMarker(m);
					marker.setLabel(markerName);
					maSet.getMarkerVector().add(m, marker);		
					 
					for (int i = 0; i < n; i++) {
							String valString = "";
							if ((i + 1) < tokens.length) {
								valString = tokens[i + 1];
							}
							if (valString.trim().length() == 0) {
								// put values directly into CSMicroarray inside of
								// maSet
								Float v = Float.NaN;
								CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(v);
								maSet.get(i).setMarkerValue(m, markerValue);
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
								}
								// put values directly into CSMicroarray inside of
								// maSet
								Float v = value;
								CSExpressionMarkerValue markerValue = new CSExpressionMarkerValue(
										v);
								try {
									maSet.get(i).setMarkerValue(m, markerValue);
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
				String result = null;
				for (int i = 0; i < m; i++) {
					result = AnnotationParser.matchChipType(maSet, maSet
							.getMarkerVector().get(i).getLabel(), false);
					if (result != null) {
						break;
					}
				}
				if (result == null) {
					AnnotationParser.matchChipType(maSet, "Unknown", true);
				} else {
					maSet.setCompatibilityLabel(result);
				}
				for (DSGeneMarker marker : maSet.getMarkerVector()) {
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
		return maSet;
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List getOptions() {
		throw new UnsupportedOperationException(
				"Method getOptions() not yet implemented.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.components.parsers.microarray.DataSetFileFormat#getDataFile(java.io.File[])
	 */
	@SuppressWarnings("unchecked")
	public DSDataSet getDataFile(File[] files) {
		// TODO Implement this
		// org.geworkbench.components.parsers.microarray.DataSetFileFormat
		// abstract method
		throw new UnsupportedOperationException(
				"Method getDataFile(File[] files) not yet implemented.");
	}
}
