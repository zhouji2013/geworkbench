package org.geworkbench.engine.preferences;

import org.geworkbench.bison.util.colorcontext.AbsoluteWhiteColorContext;
import org.geworkbench.bison.util.colorcontext.ColorContext;
import org.geworkbench.bison.util.colorcontext.DefaultColorContext;
import org.geworkbench.bison.util.colorcontext.ExpressionPValueColorContext;
import org.geworkbench.util.Util;

import java.io.File;

/**
 * @author John Watkinson
 */
public class GlobalPreferences {

    public static final String PREF_VISUALIZATION = "Visualization";
    public static final String PREF_GENEPIX_COMPUTATION = "Genepix Value Computation";
    public static final String PREF_TEXT_EDITOR = "Text Editor";
    public static final String RCM_URL = "Remote Components URL";
    public static final String PREF_MARKER_LOAD = "Marker Load Options";
    public static final String RWSP_URL = "Remote Workspaces URL";
    public static final String MAX_NETWORK_OBJECT_NUMBER = "Soft Limit on Cytoscape Network Objects (nodes+edges)";
    public static final String R_LOCATION = "R Location (Rscript.exe and its folder)";
    public static final String R_LIBPATH = "R package directory (leave it blank for default path)";
    
    public static final String[] VISUALIZATION_VALUES = new String[]{"Absolute(zero=black)", "Relative", "Absolute(zero=white)"};
    @SuppressWarnings("unchecked")
	public static final Class<? extends ColorContext>[] VISUALIZATON_COLOR_CONTEXTS = new Class[]{DefaultColorContext.class, ExpressionPValueColorContext.class, AbsoluteWhiteColorContext.class};

    public static final String[] GENEPIX_VALUES = new String[]
            {
                    "Option 1: (Mean F635 - Mean B635) / (Mean F532 - Mean B532)",
                    "Option 2: (Median F635 - Median B635) / (Median F532 - Median B532)",
                    "Option 3: (Mean F532 - Mean B532) / (Mean F635 - Mean B635)",
                    "Option 4: (Median F532 - Median B532) / (Median F635 - Median B635)"
            };
    public static final String DEFAULT_TEXT_EDITOR = "c:/windows/system32/notepad.exe";
    public static final String DEFAULT_TEXT_EDITOR_MAC = "";

    public static final String VERSION = System.getProperty("application.version");
    public static final String DEFAULT_RCM_URL = "http://cagridnode.c2b2.columbia.edu:8080/"+VERSION+"/componentRepository";
    
    public static final String[] MARKER_LOAD_VALUES = new String[]{"Load markers in the original order", "Load markers ordered by gene name", "Load markers ordered by probe set ID"};
    public static final int ORIGINAL = 0, SORTED_GENE = 1, SORTED_PROBE = 2, MAX_INTERACTION_NUM = 5000;
    
    public static final String DEFAULT_R_LOCATION = "";
    public static final String DEFAULT_R_LIBPATH = "";

    public static final String DEFAULT_RWSP_URL = "http://genspace.cs.columbia.edu:8080/axis2/services";

    private static GlobalPreferences instance;

    public static GlobalPreferences getInstance() {
        if (instance == null) {
            instance = new GlobalPreferences();
        }
        return instance;
    }

    private Preferences prefs;

    private GlobalPreferences() {
        prefs = new Preferences();

        // TEXT EDITOR
        FileField field1 = new FileField(GlobalPreferences.PREF_TEXT_EDITOR);
        // Default value
        if (!Util.isRunningOnAMac()) {
            field1.setValue(new File(GlobalPreferences.DEFAULT_TEXT_EDITOR));
        } else {
            field1.setValue(new File(DEFAULT_TEXT_EDITOR_MAC));
        }

        // Visualization
        ChoiceField field2 = new ChoiceField(GlobalPreferences.PREF_VISUALIZATION, GlobalPreferences.VISUALIZATION_VALUES);
        field2.setSelection(1);

        // GENEPIX VALUE COMPUTATION
        ChoiceField field3 = new ChoiceField(GlobalPreferences.PREF_GENEPIX_COMPUTATION, GlobalPreferences.GENEPIX_VALUES);
        field3.setSelection(0);

        // RCM URL
        TextField field4 = new TextField(GlobalPreferences.RCM_URL);
        field4.setValue(GlobalPreferences.DEFAULT_RCM_URL);
        
        // Color Context
        
        // Marker order/value options on load
        ChoiceField field5 = new ChoiceField(GlobalPreferences.PREF_MARKER_LOAD, GlobalPreferences.MARKER_LOAD_VALUES);
        field5.setSelection(0);

        // RWSP URL
        TextField field6 = new TextField(GlobalPreferences.RWSP_URL);
        field6.setValue(GlobalPreferences.DEFAULT_RWSP_URL);
        
        // reasonable number of interactions for create cytoscape network 
        IntegerField field7 = new IntegerField(MAX_NETWORK_OBJECT_NUMBER);
        field7.setValue(MAX_INTERACTION_NUM);
        
        // R location 
        TextField field8 = new TextField(R_LOCATION);
        field8.setValue(GlobalPreferences.DEFAULT_R_LOCATION );

        // R package installation directory 
        TextField field9 = new TextField(R_LIBPATH);
        field9.setValue(GlobalPreferences.DEFAULT_R_LIBPATH );

        prefs.addField(field1);
        prefs.addField(field2);
        prefs.addField(field3);
        prefs.addField(field4);
        prefs.addField(field5);
        prefs.addField(field6);
        prefs.addField(field7);
        prefs.addField(field8);
        prefs.addField(field9);

        // Load stored values
        PreferencesManager manager = PreferencesManager.getPreferencesManager();
        manager.fillPreferences(null, prefs);
    }

    public void displayPreferencesDialog() {
        PreferencesManager manager = PreferencesManager.getPreferencesManager();
        prefs = manager.showPreferencesDialog(null, prefs, null);        
    }

    public String getTextEditor() {
        return prefs.getField(PREF_TEXT_EDITOR).toString();
    }

    public int getGenepixComputationMethod() {
        return ((ChoiceField)prefs.getField(PREF_GENEPIX_COMPUTATION)).getSelection();
    }

    public String getRCM_URL() {
        return prefs.getField(RCM_URL).toString();
    }
    
    public Class<? extends ColorContext> getColorContextClass() {
        int pref = ((ChoiceField) prefs.getField(PREF_VISUALIZATION)).getSelection();
        return VISUALIZATON_COLOR_CONTEXTS[pref];
    }
    
    public int getMarkerLoadOptions() {
    	return ((ChoiceField)prefs.getField(PREF_MARKER_LOAD)).getSelection();
    }

    public String getRWSP_URL() {
        return prefs.getField(RWSP_URL).toString();
    }
    
    public int getMAX_INTERACTION_NUMBER() {
        return ((IntegerField)prefs.getField(MAX_NETWORK_OBJECT_NUMBER)).getValue();
    }
    
    public String getRLocation() {
        return prefs.getField(R_LOCATION).toString();
    }
    
    public String getRLibPath() {
        String rLibPath = prefs.getField(R_LIBPATH).toString().trim();
        if(rLibPath.endsWith("\\") || rLibPath.endsWith("/")) {
        	rLibPath = rLibPath.substring(0, rLibPath.length()-1);
        }
        return rLibPath;
    }
}
