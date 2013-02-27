package org.geworkbench.bison.datastructure.bioobjects.microarray;

import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;

public interface DSDemandResultSet extends DSAncillaryDataSet<DSMicroarray> {
	public void setResult(Object[][] result);
    public void setEdge(Object[][] edge);
    public void setModule(Object[][] module);
    public Object[][] getResult();    
    public Object[][] getEdge();
    public Object[][] getModule();
}
