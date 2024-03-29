package org.geworkbench.bison.datastructure.biocollections.gsea;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nazaire
 * @version $Id$
 */
public class CSGSEAResultDataSet extends CSAncillaryDataSet<DSBioObject> implements DSGSEAResultDataSet {

	private static final long serialVersionUID = 1L;

	static Log log = LogFactory.getLog(CSGSEAResultDataSet.class);
	private String reportFile;


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CSGSEAResultDataSet(DSDataSet<? extends DSMicroarray> parent, String label, String reportFile) {
		super((DSDataSet) parent, label);
		this.reportFile = reportFile;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.bison.datastructure.biocollections.gsea.DSGSEAResultDataSet#getReportFile()
	 */
	public String getReportFile()
    {
		return reportFile;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.bison.datastructure.biocollections.gsea.DSGSEAResultDataSet#writeToFile(java.lang.String)
	 */
	public void writeToFile(String fileName)
    {
		File file = new File(fileName);

		try
        {
			file.createNewFile();
			if (!file.canWrite())
            {
				JOptionPane.showMessageDialog(null,
						"Cannot write to specified file.");
				return;
			}
		} catch (IOException io) {
			log.error(io);
		}
	}
}

