package org.geworkbench.builtin.projects;

import java.io.IOException;

import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;

/**
 * <p>Title: Gene Expression Analysis Toolkit</p>
 * 
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author First Genetic Trust
 * @version $Id$
 */

public class DataSetNode extends ProjectTreeNode {
	
	private static final long serialVersionUID = 1423608759523479212L;
	
	private final DSDataSet<? extends DSBioObject> dataFile;
	@Override
	public DSDataSet<? extends DSBioObject> getDataset() { return dataFile; }
    
    DataSetNode(final DSDataSet<? extends DSBioObject> df) {
        dataFile = df;
        setUserObject(dataFile.getDataSetName());

        dirPropertyKey = "datanodeDir";
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // Include the criteria info if there is any
        CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
        CSAnnotationContextManager.SerializableContexts contexts = manager.getContextsForSerialization(dataFile);
        out.defaultWriteObject();
        out.writeObject(contexts);
        if (dataFile instanceof DSMicroarraySet) {
            DSMicroarraySet set = (DSMicroarraySet) dataFile;
            CSAnnotationContextManager.SerializableContexts markerContexts = manager.getContextsForSerialization(set.getMarkers());
            out.writeObject(markerContexts);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
        manager.setContextsFromSerializedObject(dataFile, (CSAnnotationContextManager.SerializableContexts) in.readObject());
        if (dataFile instanceof DSMicroarraySet) {
            DSMicroarraySet set = (DSMicroarraySet) dataFile;
            manager.setContextsFromSerializedObject(set.getMarkers(), (CSAnnotationContextManager.SerializableContexts) in.readObject());
        }
    }

}
