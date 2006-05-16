package org.geworkbench.util.sequences;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JViewport;

import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.
        DSSeqRegistration;
import org.geworkbench.util.patterns.*;
import org.geworkbench.util.promoter.pattern.Display;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SequenceViewWidgetPanel extends JPanel {

    final int xOff = 60;
    final int yOff = 20;
    final int xStep = 5;
    final int yStep = 14;
    double scale = 1.0;

    int selected = 0;
    int maxSeqLen = 1;
    private String displayInfo = "";
    //ArrayList  selectedPatterns   = null;
    DSCollection<DSMatchedPattern<DSSequence,
            DSSeqRegistration>> selectedPatterns = null;
    DSSequenceSet sequenceDB = null;
    HashMap<CSSequence,
            PatternSequenceDisplayUtil> sequencePatternmatches;
    boolean showAll = false;
    private boolean lineView;
    private boolean singleSequenceView;
    private final static Color SEQUENCEBACKGROUDCOLOR = Color.BLACK;
    private double yBasescale;
    private int xBaseCols;
    private int[] eachSeqStartRowNum;
    private double xBasescale;
    private int seqXclickPoint = 0;
    private DSSequence selectedSequence;


    public SequenceViewWidgetPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    void jbInit() throws Exception {

    }

    public void initialize(HashMap<CSSequence,
                           PatternSequenceDisplayUtil> patternSeqMatches,
                           DSSequenceSet seqDB,
                           boolean isLineView) {
        sequencePatternmatches = patternSeqMatches;
        sequenceDB = seqDB;
        lineView = isLineView;
        repaint();

    }


    //public void initialize(ArrayList patterns, CSSequenceSet seqDB) {
    public void initialize(DSCollection<DSMatchedPattern<DSSequence,
                           DSSeqRegistration>> matches, DSSequenceSet seqDB) {

        initialize(matches, seqDB, true);
    }

    /**
     * THe inistialization of the panel.
     * @param matches DSCollection
     * @param seqDB DSSequenceSet
     * @param isLineView boolean
     */
    public void initialize(DSCollection<DSMatchedPattern<DSSequence,
                           DSSeqRegistration>> matches, DSSequenceSet seqDB,
                           boolean isLineView) {
        selectedPatterns = matches;
        sequenceDB = seqDB;
        lineView = isLineView;
        repaint();
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (lineView) {
            if (!singleSequenceView) {
                paintText(g);
            } else {
                paintSingleSequence(g);
            }
        } else {
            paintFullView(g);

        }
    }


    private void paintFullView(Graphics g) {
        singleSequenceView = false; //make sure when the view shifts, the singlesequenceview is not selected.
        if (sequenceDB != null) {
            // DSSequence theone = sequenceDB.getSequence(selected);
            JViewport scroller = (JViewport)this.getParent();
            Rectangle r = scroller.getViewRect();
            int rowId = 0;
            double y = yOff + 3;
            double xscale = 0.1;
            double yscale = 0.1;
            int cols = 1;
            boolean setupScale = true;
            eachSeqStartRowNum = new int[sequenceDB.size()];
            int seqId = 0;
            Font f = new Font("Courier New", Font.PLAIN, 11);
            ((Graphics2D) g).setRenderingHint(RenderingHints.
                                              KEY_ANTIALIASING,
                                              RenderingHints.
                                              VALUE_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics(f);
            g.setFont(f);
            for (Object seq : sequenceDB) {
                DSSequence theone = (DSSequence) seq;
                if (theone != null) {
                    String asc = theone.getSequence();
                    Rectangle2D r2d = fm.getStringBounds(asc, g);
                    int width = this.getWidth();

                    //Set up scales.
                    if (setupScale) {
                        xscale = (r2d.getWidth() + 3) / (double) (asc.length());
                        yscale = 1.3 * r2d.getHeight();
                        yBasescale = yscale;
                        cols = (int) (width / xscale) - 8;
                        xBaseCols = cols;
                        xBasescale = xscale;
                        setupScale = false;
                    }

                    String lab = theone.getLabel();
                    g.setColor(SEQUENCEBACKGROUDCOLOR);
                    //            if (lab.length() > 10) {
                    //                g.drawString(lab.substring(0, 10), 2, y + 3);
                    //            }
                    //            else {
                    g.drawString(lab, 2, (int) (y));
                    //            }
                    y += 1 * yscale;
                    int begin = 0 - cols;
                    int end = 0;
                    eachSeqStartRowNum[seqId] = rowId;

                    while (end < asc.length()) {
                        rowId++;

                        begin = end;
                        end += cols;
                        String onepiece = "";
                        if (end > asc.length()) {
                            onepiece = asc.substring(begin, asc.length());
                        } else {
                            onepiece = asc.substring(begin, end);

                        }
                        g.setColor(SEQUENCEBACKGROUDCOLOR);
                        g.drawString(onepiece, (int) (6 * xscale), (int) y);
                        y += 1 * yscale;
                    }
                    rowId++;

                    if (sequencePatternmatches != null) {
                        PatternSequenceDisplayUtil psd = sequencePatternmatches.
                                get(
                                        theone);
                        TreeSet<PatternLocations>
                                patternsPerSequence = psd.getTreeSet();
                        if (patternsPerSequence != null &&
                            patternsPerSequence.size() > 0) {
                            for (PatternLocations pl : patternsPerSequence) {
                                DSSeqRegistration reg = pl.getRegistration();
                                if (reg != null) {
                                    drawPattern(g, theone, reg.x1, xscale,
                                                yscale,
                                                eachSeqStartRowNum[seqId], cols,
                                                PatternOperations.
                                                getPatternColor(pl.getHashcode()),
                                                pl.getAscii());

                                }
                            }
                        }
                    }
                    seqId++; //
                }

            } //end processing sequences.

            int maxY = (int) y + yOff;
            setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
            revalidate();

        }
    }

    private void paintSingleSequence(Graphics g) {
        if (sequenceDB != null) {
            selected = Math.min(selected, sequenceDB.size() - 1);
            DSSequence theone = sequenceDB.getSequence(selected);
            int rowId = 0;
            int y = yOff;
            if (theone != null) {
                Font f = new Font("Courier New", Font.PLAIN, 11);
                ((Graphics2D) g).setRenderingHint(RenderingHints.
                                                  KEY_ANTIALIASING,
                                                  RenderingHints.
                                                  VALUE_ANTIALIAS_ON);
                FontMetrics fm = g.getFontMetrics(f);
                String asc = theone.getSequence();
                Rectangle2D r2d = fm.getStringBounds(asc, g);
                double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
                double yscale = 1.3 * r2d.getHeight();
                int width = this.getWidth();
                int cols = (int) (width / xscale) - 8;
                g.setFont(f);
                JViewport scroller = (JViewport)this.getParent();
                Rectangle r = scroller.getViewRect();
                String lab = theone.getLabel();
                y += (int) (rowId * yscale);
                g.setColor(SEQUENCEBACKGROUDCOLOR);
                if (lab.length() > 10) {
                    g.drawString(lab.substring(0, 10), 2, y + 3);
                } else {
                    g.drawString(lab, 2, y + 3);
                }

                int begin = 0 - cols;
                int end = 0;

                while (end < asc.length()) {
                    rowId++;
                    y = yOff + (int) (rowId * yscale);

                    begin = end;
                    end += cols;
                    String onepiece = "";
                    if (end > asc.length()) {
                        onepiece = asc.substring(begin, asc.length());
                    } else {
                        onepiece = asc.substring(begin, end);

                    }
                    g.drawString(onepiece, (int) (6 * xscale), y + 3);
                }
                if (sequencePatternmatches != null) {
                    PatternSequenceDisplayUtil psd = sequencePatternmatches.get(
                            theone);
                    TreeSet<PatternLocations>
                            patternsPerSequence = psd.getTreeSet();
                    if (patternsPerSequence != null &&
                        patternsPerSequence.size() > 0) {
                        for (PatternLocations pl : patternsPerSequence) {
                            DSSeqRegistration reg = pl.getRegistration();
                            if (reg != null) {
                                drawPattern(g, theone, reg.x1, xscale,
                                            yscale, 0, cols,
                                            PatternOperations.
                                            getPatternColor(pl.getHashcode()),
                                            pl.getAscii());

                            }
                        }
                    }
                }

                int maxY = y + yOff;
                setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
                revalidate();

            }
        }
    }

//    private void paintSingleSequence(Graphics g) {
//        if (sequenceDB != null) {
//            selected = Math.min(selected, sequenceDB.size() - 1);
//            DSSequence theone = sequenceDB.getSequence(selected);
//            int rowId = 0;
//            int y = yOff;
//            if (theone != null) {
//                Font f = new Font("Courier New", Font.PLAIN, 11);
//                ((Graphics2D) g).setRenderingHint(RenderingHints.
//                                                  KEY_ANTIALIASING,
//                                                  RenderingHints.
//                                                  VALUE_ANTIALIAS_ON);
//                FontMetrics fm = g.getFontMetrics(f);
//                String asc = theone.getSequence();
//                Rectangle2D r2d = fm.getStringBounds(asc, g);
//                double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
//                double yscale = 1.3 * r2d.getHeight();
//                int width = this.getWidth();
//                int cols = (int) (width / xscale) - 8;
//
//                g.setFont(f);
//                JViewport scroller = (JViewport)this.getParent();
//                Rectangle r = scroller.getViewRect();
//                String lab = theone.getLabel();
//                y += (int) (rowId * yscale);
//                g.setColor(SEQUENCEBACKGROUDCOLOR);
//                //            if (lab.length() > 10) {
//                //                g.drawString(lab.substring(0, 10), 2, y + 3);
//                //            }
//                //            else {
//                g.drawString(lab, 2, y + 3);
//                //            }
//
//                int begin = 0 - cols;
//                int end = 0;
//
//                //rowId++; //uncom by xq
//                while (end < asc.length()) {
//                    rowId++;
//                    y = yOff + (int) (rowId * yscale);
//
//                    begin = end;
//                    end += cols;
//                    String onepiece = "";
//                    if (end > asc.length()) {
//                        onepiece = asc.substring(begin, asc.length());
//                    } else {
//                        onepiece = asc.substring(begin, end);
//
//                    }
//                    g.drawString(onepiece, (int) (6 * xscale), y + 3);
//                }
//
//                if (selectedPatterns != null) {
//                    for (int row = 0; row < selectedPatterns.size(); row++) {
//                        DSMatchedSeqPattern pattern = (DSMatchedSeqPattern)
//                                selectedPatterns.get(row);
//                        PatternOperations.setPatternColor(new Integer(pattern.
//                                hashCode()),
//                                PatternOperations.
//                                getPatternColor(row));
//                        int seqId = 0;
//                        if (pattern != null) {
//
//                            for (int locusId = 0;
//                                               locusId < pattern.getSupport();
//                                               locusId++) {
//                                seqId = ((CSMatchedSeqPattern) pattern).getId(
//                                        locusId);
//                                DSPatternMatch<DSSequence,
//                                        DSSeqRegistration>
//                                        sp = pattern.get(locusId);
//
//                                if (showAll) {
//                                    int newIndex[] = sequenceDB.
//                                            getMatchIndex();
//                                    if (newIndex != null &&
//                                        newIndex[seqId] != -1) {
//                                        DSSequence hitSeq = sp.getObject();
//                                        if (hitSeq != null &&
//                                            theone.equals(
//                                                hitSeq)) {
//                                            Color c = PatternOperations.
//                                                    getPatternColor(
//                                                    row);
//
//                                            g.setColor(c);
//
//                                            drawPattern(g, sp, xscale,
//                                                    yscale,
//                                                    0,
//                                                    cols,
//                                                    c, pattern.getASCII());
//                                            break;
//
//                                        }
//
//                                    }
//
//                                } else {
//
//                                    for (int i = 0; i < sequenceDB.size(); i++) {
//
//                                        DSSequence hitSeq = sp.getObject();
//                                        if (hitSeq != null &&
//                                            theone.equals(
//                                                hitSeq)) {
//                                            Color c = PatternOperations.
//                                                    getPatternColor(
//                                                    row);
//
//                                            g.setColor(c);
//
//                                            drawPattern(g, sp, xscale,
//                                                    yscale,
//                                                    0,
//                                                    cols,
//                                                    c, pattern.getASCII());
//
//                                            break;
//
//                                        }
//                                    }
//
//                                }
//                            }
//
//                        }
//                    }
//                }
//
//                int maxY = y + yOff;
//                setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
//                revalidate();
//
//            }
//        }
//    }
    private void paintGraphic(Graphics g) {
        Font f = new Font("Courier New", Font.PLAIN, 10);
        if (sequenceDB != null) {
            int rowId = -1;

            int seqNo = sequenceDB.getSequenceNo();

            scale = Math.min(5.0,
                             (double) (this.getWidth() - 20 - xOff) /
                             (double) maxSeqLen);
            g.clearRect(0, 0, getWidth(), getHeight());
            // draw the patterns
            g.setFont(f);
            JViewport scroller = (JViewport)this.getParent();
            Rectangle r = new Rectangle();
            r = scroller.getViewRect();

            for (int seqId = 0; seqId < seqNo; seqId++) {
                rowId++;
                drawSequence(g, seqId, seqId, maxSeqLen);
            }

            //  for (DSPattern pattern : patternMatches.keySet()) {
            // List<DSPatternMatch<DSSequence, DSSeqRegistration>> matches = selectedPatterns;

            if ((selectedPatterns != null) && (selectedPatterns.size() > 0)) {
                for (Object pattern : selectedPatterns) {
                    CSMatchedSeqPattern pat = (CSMatchedSeqPattern) pattern;
                    int lastSeqId = -1;
                    for (int locusId = 0; locusId < pat.getSupport(); locusId++) {
                        int seqId = pat.getId(locusId);
                        if (seqId > lastSeqId) {
                            rowId++;
                            //   drawSequence(g, rowId, seqId, maxSeqLen);
                            lastSeqId = seqId;
                        }
                        drawPattern(g, rowId, locusId, pat, r,
                                    PatternOperations.getPatternColor(pat.
                                hashCode()));
                    }

                }
                // drawPattern(g, selectedPatterns, r, (Display) patternDisplay.get(pattern));
            }

            //   }
            int maxY = (seqNo + 1) * yStep + yOff;
            setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
            revalidate();

        } else {

        }

    }

//For Line view.

    private void paintText(Graphics g) {
        Font f = new Font("Courier New", Font.PLAIN, 10);

        if (sequenceDB != null) {
            int rowId = -1;

            //int maxSeqLen = sequenceDB.getMaxLength();
            int seqNo = sequenceDB.getSequenceNo();

            scale = Math.min(5.0,
                             (double) (this.getWidth() - 20 - xOff) /
                             (double) maxSeqLen);
            // System.out.println("IN SVWPanel: " + scale + maxSeqLen);
            g.clearRect(0, 0, getWidth(), getHeight());
            // draw the patterns
            g.setFont(f);
            JViewport scroller = (JViewport)this.getParent();
            Rectangle r = new Rectangle();
            r = scroller.getViewRect();

            for (int seqId = 0; seqId < seqNo; seqId++) {
                rowId++;
                drawSequence(g, seqId, seqId, maxSeqLen);
                CSSequence sequence = (CSSequence) sequenceDB.get(seqId);
                if (sequencePatternmatches != null) {
                    PatternSequenceDisplayUtil psd = sequencePatternmatches.
                            get(sequence);
                    if (psd != null) {
                        TreeSet<PatternLocations>
                                patternsPerSequence = psd.getTreeSet();
                        if (patternsPerSequence != null &&
                            patternsPerSequence.size() > 0) {
                            for (PatternLocations pl : patternsPerSequence) {
                                DSSeqRegistration reg = pl.getRegistration();
                                if (reg != null) {
                                    drawPattern(g, seqId, reg.x1,
                                                reg.length(),
                                                r,
                                                PatternOperations.
                                                getPatternColor(pl.getHashcode()));
                                }
                            }
                        }
                    }
                }
            }

            int maxY = (rowId + 1) * yStep + yOff;
            setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
            revalidate();
        }
    }


//    private void paintText(Graphics g) {
//        Font f = new Font("Courier New", Font.PLAIN, 10);
//
//        if (sequenceDB != null) {
//            int rowId = -1;
//            int[] rows = {};
//
//            //int maxSeqLen = sequenceDB.getMaxLength();
//            int seqNo = sequenceDB.getSequenceNo();
//
//            if (sequenceDB.getSequenceNo() == 0) {
//                //Don't make sense here. xq
////                if (selectedPatterns != null) {
////                    for (int row = 0; row < selectedPatterns.size(); row++) {
////                        DSMatchedSeqPattern pattern = (DSMatchedSeqPattern)
////                                selectedPatterns.get(row);
////                        if (pattern instanceof CSMatchedSeqPattern) {
////                            CSMatchedSeqPattern pat = (CSMatchedSeqPattern)
////                                    pattern;
////                            if ((pat != null) && (pat.getSupport() > 0)) {
////                                seqNo = Math.max(seqNo,
////                                                 pat.getId(pat.getSupport() - 1));
////                            }
////                        }
////                    }
////                }
//            }
//            scale = Math.min(5.0,
//                             (double) (this.getWidth() - 20 - xOff) /
//                             (double) maxSeqLen);
//            // System.out.println("IN SVWPanel: " + scale + maxSeqLen);
//            g.clearRect(0, 0, getWidth(), getHeight());
//            // draw the patterns
//            g.setFont(f);
//            JViewport scroller = (JViewport)this.getParent();
//            Rectangle r = new Rectangle();
//            r = scroller.getViewRect();
//            if ((rows.length == 1) && showAll && (selectedPatterns != null)) {
//
//            } else {
//                for (int seqId = 0; seqId < seqNo; seqId++) {
//                    rowId++;
//                    drawSequence(g, seqId, seqId, maxSeqLen);
//                }
//                if (selectedPatterns != null) {
//                    for (int row = 0; row < selectedPatterns.size(); row++) {
//                        DSMatchedSeqPattern pattern = (DSMatchedSeqPattern)
//                                selectedPatterns.get(row);
//                        PatternOperations.setPatternColor(new Integer(pattern.
//                                hashCode()),
//                                PatternOperations.getPatternColor(row));
//
//                        if (pattern != null) {
//                            if (pattern.getClass().isAssignableFrom(
//                                    CSMatchedSeqPattern.class) ||
//                                pattern.getClass().isAssignableFrom(
//                                        CSMatchedHMMSeqPattern.class)) {
//                                CSMatchedSeqPattern pat = (CSMatchedSeqPattern)
//                                        pattern;
//                                if (pattern != null) {
//                                    for (int locusId = 0;
//                                            locusId < pattern.getSupport();
//                                            locusId++) {
//                                        int seqId = pat.getId(locusId);
//
//                                        if (showAll) {
//                                            int newIndex[] = sequenceDB.
//                                                    getMatchIndex();
////                                            System.out.println(newIndex +
////                                                    " is null? in svwp" +
////                                                    sequenceDB.size());
//                                            if (newIndex != null &&
//                                                seqId < newIndex.length &&
//                                                newIndex[seqId] != -1) {
//                                                System.out.println(
//                                                        "SVWP PAINT PATTEN: " +
//                                                        seqId + " " +
//                                                        newIndex[seqId] +
//                                                        locusId);
//                                                if (drawPattern(g,
//                                                        newIndex[seqId],
//                                                        locusId, pat,
//                                                        r,
//                                                        PatternOperations.
//                                                        getPatternColor(
//                                                        row))) {
//                                                    break;
//                                                }
//                                            } else {
////                                                System.out.println(
////                                                        "Something wrong here" +
////                                                        locusId);
//                                            }
//
//                                        } else {
//                                            if (seqId < sequenceDB.size()) {
//                                                if (drawPattern(g, seqId,
//                                                        locusId,
//                                                        pat,
//                                                        r,
//                                                        PatternOperations.
//                                                        getPatternColor(
//                                                        row))) {
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            int maxY = (rowId + 1) * yStep + yOff;
//            setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
//            revalidate();
//        }
//    }

    void setShowAll(boolean all) {
        showAll = all;
    }

    public void setMaxSeqLen(int maxSeqLen) {
        this.maxSeqLen = maxSeqLen;
    }

    public void setlineView(boolean lineView) {
        this.lineView = lineView;
        revalidate();
    }

    public void setDisplayInfo(String displayInfo) {
        this.displayInfo = displayInfo;
    }

    public void setSeqXclickPoint(int seqXclickPoint) {
        this.seqXclickPoint = seqXclickPoint;
    }

    public void setSelectedSequence(DSSequence selectedSequence) {
        this.selectedSequence = selectedSequence;
    }

    void drawSequence(Graphics g, int rowId, int seqId, double len) {
        String lab = ">seq " + seqId;
        if (sequenceDB.getSequenceNo() > 0) {
            DSSequence theSequence = sequenceDB.getSequence(seqId);
            len = (double) theSequence.length();
            lab = theSequence.getLabel();

        }
        int y = yOff + rowId * yStep;
        int x = xOff + (int) (len * scale);
        g.setColor(SEQUENCEBACKGROUDCOLOR);
        if (lab.length() > 10) {
            g.drawString(lab.substring(0, 10), 4, y + 3);
        } else {
            g.drawString(lab, 4, y + 3);
        }
        g.drawLine(xOff, y, x, y);
    }

    boolean drawPattern(Graphics g, int rowId, int locusId,
                        CSMatchedSeqPattern pat, Rectangle r, Color color) {

        int y = yOff + rowId * yStep;
        if (y > r.y) {
            if (y > r.y + r.height) {
                return true;
            }
            double x0 = pat instanceof CSMatchedHMMSeqPattern ?
                        ((CSMatchedHMMSeqPattern) pat).getStart(locusId) :
                        (double) pat.getOffset(locusId);
            double dx = pat instanceof CSMatchedHMMSeqPattern ?
                        (((CSMatchedHMMSeqPattern) pat).getEnd(locusId) - x0) :
                        pat.getASCII().length();
            int xa = xOff + (int) (x0 * scale) + 1;
            int xb = xa + (int) (dx * scale) - 1;
            g.setColor(color);
            g.draw3DRect(xa, y - 2, xb - xa, 4, false);
        }
        return false;
    }

    boolean drawPattern(Graphics g, int rowId, int xStart, int length,
                        Rectangle r, Color color) {

        int y = yOff + rowId * yStep;
        if (y > r.y) {
            if (y > r.y + r.height) {
                return true;
            }
            double x0 = xStart;
            double dx = length;
            int xa = xOff + (int) (x0 * scale) + 1;
            int xb = xa + (int) (dx * scale) - 1;
            g.setColor(color);
            g.draw3DRect(xa, y - 2, xb - xa, 4, false);
        }
        return false;
    }


    private void drawPattern(Graphics g, DSPatternMatch<DSSequence,
                             DSSeqRegistration> sp, double xscale,
                             double yscale, int yBase, int cols, Color color,
                             String highlight) {

        int length = sp.getRegistration().length(); //very strange, the length is incorrect.
        length = highlight.length();
        int offset = sp.getRegistration().x1;
        int x = (int) ((6 + offset % cols) * xscale);
        double y = ((yBase + 2 + (offset / cols)) * yscale);
        int xb = (int) (length * xscale);

        int height = (int) (1.3 * yscale);

        DSSequence hitSeq = sp.getObject();
        String hitSeqStr = hitSeq.getSequence().substring(offset,
                offset + length);
        if (offset % cols + length <= cols) {
            g.clearRect(x, (int) y - height / 2, xb, height);
            g.setColor(SEQUENCEBACKGROUDCOLOR);
            g.drawString(hitSeqStr,
                         x, (int) (y - 1 * yscale + yOff + 3));
            g.setColor(color);
            g.drawString(highlight, x, (int) (y - 1 * yscale + yOff + 3));
            g.draw3DRect(x, (int) y - height / 2, xb, height, false);

        } else {

            int startx = (int) (6 * xscale);
            int endx = (int) ((cols + 6) * xscale);
            int k = (offset + length) / cols - offset / cols;
            g.clearRect(x, (int) y - height / 2, endx - x, height);
            g.setColor(SEQUENCEBACKGROUDCOLOR);
            g.drawString(hitSeqStr.substring(0, cols - offset % cols), x,
                         (int) (y - 1 * yscale + yOff + 3));
            g.setColor(color);
            g.drawString(highlight.substring(0, cols - offset % cols), x,
                         (int) (y - 1 * yscale + yOff + 3));
            g.draw3DRect(x, (int) y - height / 2, endx - x, height, true);
            for (int i = 1; i < k; i++) {
                g.clearRect(startx, (int) (y - height / 2 + (i * yscale)),
                            endx - startx, height);
                g.setColor(SEQUENCEBACKGROUDCOLOR);
                g.drawString(hitSeqStr.substring(cols - offset % cols +
                                                 (k - 1) * cols,
                                                 cols - offset % cols +
                                                 k * cols), startx,
                             (int) (y + (k - 1) * yscale + yOff + 3));
                g.setColor(color);
                g.drawString(highlight.substring(cols - offset % cols +
                                                 (k - 1) * cols,
                                                 cols - offset % cols +
                                                 k * cols), startx,
                             (int) (y + (k - 1) * yscale + yOff + 3));
                g.draw3DRect(startx, (int) (y - height / 2 + (i * yscale)),
                             endx - startx, height, true);
            }
            g.clearRect(startx,
                        (int) (y - height / 2 + (k * yscale)),
                        (int) (((offset + length) % cols) * xscale),
                        height
                    );
            g.setColor(SEQUENCEBACKGROUDCOLOR);
            g.drawString(hitSeqStr.substring(cols - offset % cols +
                                             (k - 1) * cols), startx,
                         (int) (y + (k - 1) * yscale + yOff + 3));
            g.setColor(color);
            g.drawString(highlight.substring(cols - offset % cols), startx,
                         (int) (y + (k - 1) * yscale + yOff + 3));
            g.draw3DRect(startx,
                         (int) (y - height / 2 + (k * yscale)),
                         (int) (((offset + length) % cols) * xscale),
                         height, true);

        }
        //g.drawString(highlight, x, (int) (y - 1 * yscale + yOff + 3));
    }

//changed for the simplifed use,should replace the above method.


    private void drawPattern(Graphics g, DSSequence hitSeq, int offset,
                             double xscale,
                             double yscale, int yBase, int cols, Color color,
                             String highlight) {

        int length = 0; //sp.getRegistration().length(); //very strange, the length is incorrect.
        length = highlight.length();
        // int offset = sp.getRegistration().x1;
        int x = (int) ((6 + offset % cols) * xscale);
        double y = ((yBase + 2 + (offset / cols)) * yscale);
        int xb = (int) (length * xscale);

        int height = (int) (1.3 * yscale);

        String hitSeqStr = hitSeq.getSequence().substring(offset,
                offset + length);
        if (offset % cols + length <= cols) {
            g.clearRect(x, (int) y - height / 2, xb, height);
            g.setColor(SEQUENCEBACKGROUDCOLOR);
            g.drawString(hitSeqStr.toUpperCase(),
                         x, (int) (y - 1 * yscale + yOff + 3));
            g.setColor(color);
            g.drawString(highlight.toUpperCase(), x,
                         (int) (y - 1 * yscale + yOff + 3));
            g.draw3DRect(x, (int) y - height / 2, xb, height, false);

        } else {

            int startx = (int) (6 * xscale);
            int endx = (int) ((cols + 6) * xscale);
            int k = (offset + length) / cols - offset / cols;
            g.clearRect(x, (int) y - height / 2, endx - x, height);
            g.setColor(SEQUENCEBACKGROUDCOLOR);
            g.drawString(hitSeqStr.substring(0, cols - offset % cols).
                         toUpperCase(), x,
                         (int) (y - 1 * yscale + yOff + 3));
            g.setColor(color);
            g.drawString(highlight.substring(0, cols - offset % cols).
                         toUpperCase(), x,
                         (int) (y - 1 * yscale + yOff + 3));
            g.draw3DRect(x, (int) y - height / 2, endx - x, height, true);
            for (int i = 1; i < k; i++) {
                g.clearRect(startx, (int) (y - height / 2 + (i * yscale)),
                            endx - startx, height);
                g.setColor(SEQUENCEBACKGROUDCOLOR);
                g.drawString(hitSeqStr.substring(cols - offset % cols +
                                                 (k - 1) * cols,
                                                 cols - offset % cols +
                                                 k * cols).toUpperCase(),
                             startx,
                             (int) (y + (k - 1) * yscale + yOff + 3));
                g.setColor(color);
                g.drawString(highlight.substring(cols - offset % cols +
                                                 (k - 1) * cols,
                                                 cols - offset % cols +
                                                 k * cols).toUpperCase(),
                             startx,
                             (int) (y + (k - 1) * yscale + yOff + 3));
                g.draw3DRect(startx, (int) (y - height / 2 + (i * yscale)),
                             endx - startx, height, true);
            }
            g.clearRect(startx,
                        (int) (y - height / 2 + (k * yscale)),
                        (int) (((offset + length) % cols) * xscale),
                        height
                    );
            g.setColor(SEQUENCEBACKGROUDCOLOR);
            g.drawString(hitSeqStr.substring(cols - offset % cols +
                                             (k - 1) * cols).toUpperCase(),
                         startx,
                         (int) (y + (k - 1) * yscale + yOff + 3));
            g.setColor(color);
            g.drawString(highlight.substring(cols - offset % cols).toUpperCase(),
                         startx,
                         (int) (y + (k - 1) * yscale + yOff + 3));
            g.draw3DRect(startx,
                         (int) (y - height / 2 + (k * yscale)),
                         (int) (((offset + length) % cols) * xscale),
                         height, true);

        }
        //g.drawString(highlight, x, (int) (y - 1 * yscale + yOff + 3));
    }

    /**
     *
     * @param g Graphics
     * @param sp DSPatternMatch
     * @param r Rectangle
     * @param xscale double
     * @param yscale double
     * @param cols int
     * @param dp Display
     */

    private void drawPattern(Graphics g, CSMatchedSeqPattern pattern,
                             Rectangle r,
                             double xscale, double yscale, int cols, Display dp) {
        for (int j = 0; j < pattern.getSupport(); j++) {
            DSSequence matchSequence = pattern.get(j).getObject();

        }
    }

    boolean drawFlexiPattern(Graphics g, int rowId, double x0,
                             CSMatchedSeqPattern pat, Rectangle r, Color color) {
        int y = yOff + rowId * yStep;
        if (y > r.y) {
            if (y > r.y + r.height) {
                return true;
            }
            double dx = pat.getExtent();
            int xa = xOff + (int) (x0 * scale) + 1;
            int xb = xa + (int) (dx * scale) - 1;
            g.setColor(color);
            g.draw3DRect(xa, y - 2, xb - xa, 4, false);
        }
        return false;
    }

    public int getMaxSeqLen() {
        return maxSeqLen;
    }

    public boolean islineView() {
        return lineView;
    }

    public String getDisplayInfo() {
        return displayInfo;
    }

    public int getSeqXclickPoint() {
        return seqXclickPoint;
    }

    public DSSequence getSelectedSequence() {
        return selectedSequence;
    }

    private int getSeqDx(int x) {

        int seqDx = (int) ((double) (x - xOff) / scale);
        return seqDx;
    }

    public void this_mouseClicked(MouseEvent e) {
        setTranslatedParameters(e);

        if (e.getClickCount() == 2) {

            this.flipLineView();
            this.repaint();
        }

    }

    /**
     * getTranslatedParameters
     *
     * @param e MouseEvent
     */
    public void setTranslatedParameters(MouseEvent e) {
        int y = e.getY();
        int x = e.getX();

        if (!lineView) {
            selected = getSeqIdInFullView(y);
            if (selected < eachSeqStartRowNum.length) {
                seqXclickPoint = (int) ((int) ((y - yOff - 1 -
                                                ((double) eachSeqStartRowNum[
                                                 selected]) *
                                                yBasescale) / yBasescale) *
                                        xBaseCols +
                                        x / xBasescale -
                                        5);
            }
        } else {
            if (!singleSequenceView) {
                selected = getSeqId(y);
                seqXclickPoint = getSeqDx(x);

            } else {

                seqXclickPoint = (int) ((int) ((y - yOff - 1) / yBasescale) *
                                        xBaseCols +
                                        x / xBasescale -
                                        5);
            }
        }
        if (selected < sequenceDB.size()) {
            selectedSequence = sequenceDB.getSequence(selected);
        }
        if (selectedSequence != null) {
            displayInfo = "For sequence " + selectedSequence.getLabel() +
                          ", total length: " +
                          selectedSequence.length();
            if (sequencePatternmatches != null) {
                PatternSequenceDisplayUtil psu = sequencePatternmatches.get(
                        selectedSequence);
                if (psu != null && psu.getTreeSet() != null) {
                    displayInfo += ", pattern number: " + psu.getTreeSet().size();
                }
            }
            if ((seqXclickPoint <= selectedSequence.length()) &&
                (seqXclickPoint > 0)) {
                this.setToolTipText("" + seqXclickPoint);
                displayInfo += ". Current location: " + seqXclickPoint;
            }
        }

    }

    /**
     * getSeqIdInFullView
     *
     * @param y int
     * @return int
     */
    private int getSeqIdInFullView(int y) {
        double yBase = (y - yOff - 3) / yBasescale + 1;
        if (eachSeqStartRowNum != null) {
            for (int i = 0; i < eachSeqStartRowNum.length; i++) {
                if (eachSeqStartRowNum[i] > yBase) {
                    return Math.max(0, i - 1);
                }

            }
        }
        return eachSeqStartRowNum.length - 1;
    }

    public void flipLineView() {
        singleSequenceView = !singleSequenceView;
        //lineView = !lineView;
    }

    public int getSeqId(int y) {
        int seqId = (y - yOff + 5) / yStep;
        return seqId;
    }

//    public int getSeqDx(int x) {
//        double scale = Math.min(5.0,
//                                (double) (this.getWidth() - 20 - xOff) /
//                                (double) sequenceDB.getMaxLength());
//        int seqDx = (int) ((double) (x - xOff) / scale);
//
//        return seqDx;
//    }

    public void this_mouseMoved(MouseEvent e) {
        setTranslatedParameters(e);
        if (!lineView) {
            mouseOverFullView(e);
        } else {
            mouseOverLineView(e);

        }

    }

//   private void paintText(Graphics g) throws ArrayIndexOutOfBoundsException {
//
//          if (sequenceDB != null) {
//              DSSequence theone = sequenceDB.getSequence(selected);
//
//              if (theone != null) {
//                  Font f = new Font("Courier New", Font.PLAIN, 11);
//                  ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//                  FontMetrics fm = g.getFontMetrics(f);
//                  String asc = theone.getSequence();
//                  Rectangle2D r2d = fm.getStringBounds(asc, g);
//                  double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
//                  double yscale = 1.3 * r2d.getHeight();
//                  int width = this.getWidth();
//                  int cols = (int) (width / xscale) - 8;
//                  int rowId = 0;
//                  g.setFont(f);
//                  JViewport scroller = (JViewport) this.getParent();
//                  Rectangle r = scroller.getViewRect();
//                  String lab = theone.getLabel();
//                  int y = yOff + (int) (rowId * yscale);
//                  g.setColor(SEQUENCEBACKGROUDCOLOR);
//                  //            if (lab.length() > 10) {
//                  //                g.drawString(lab.substring(0, 10), 2, y + 3);
//                  //            }
//                  //            else {
//                  g.drawString(lab, 2, y + 3);
//                  //            }
//
//                  int begin = 0 - cols;
//                  int end = 0;
//                  //            rowId++;
//                  while (end < asc.length()) {
//                      rowId++;
//                      y = yOff + (int) (rowId * yscale);
//
//                      begin = end;
//                      end += cols;
//                      String onepiece = "";
//                      if (end > asc.length()) {
//                          onepiece = asc.substring(begin, asc.length());
//                      } else {
//                          onepiece = asc.substring(begin, end);
//
//                      }
//                      g.drawString(onepiece, (int) (6 * xscale), y + 3);
//                  }
//                  for (DSPattern pattern : patternMatches.keySet()) {
//                      List<DSPatternMatch<DSSequence, DSSeqRegistration>> matches = patternMatches.get(pattern);
//                      if (matches != null) {
//                          for (int i = 0; i < matches.size(); i++) {
//                              DSPatternMatch<DSSequence, DSSeqRegistration> match = matches.get(i);
//                              DSSequence sequence = match.getObject();
//                              if (sequence.getSerial() == selected) {
//                                  drawPattern(g, match, r, xscale, yscale, cols, (Display) patternDisplay.get(pattern));
//                              }
//                          }
//                      }
//                  }
//
//                  int maxY = y + yOff;
//                  setPreferredSize(new Dimension(this.getWidth() - yOff, maxY));
//                  revalidate();
//
//              }
//          }
//    }
    private void mouseOverFullView(MouseEvent e) throws
            ArrayIndexOutOfBoundsException {

        if (sequenceDB == null) {
            return;
        }
        int x1 = e.getX();
        int y1 = e.getY();

        Font f = new Font("Courier New", Font.PLAIN, 11);
        Graphics g = this.getGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                          RenderingHints.VALUE_ANTIALIAS_ON);
        FontMetrics fm = g.getFontMetrics(f);
        if (sequenceDB.getSequence(selected) == null ||
            sequenceDB.getSequence(selected).getSequence() == null) {
            return;
        }
        String asc = sequenceDB.getSequence(selected).getSequence();
        Rectangle2D r2d = fm.getStringBounds(asc, g);
        double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
        double yscale = 1.3 * r2d.getHeight();
        int width = this.getWidth();
        int cols = (int) (width / xscale) - 8;
        int dis = (int) ((int) ((y1 - yOff - 1) / yscale) * cols + x1 / xscale -
                         5);
        if (sequenceDB.getSequence(selected) != null) {
            if (((y1 - yOff - 1) / yscale > 0) && (dis > 0) &&
                (dis <= sequenceDB.getSequence(selected).length())) {
                this.setToolTipText("" + dis);
            }
        }

        String display = "";
//       for (DSMatchedPattern pattern : selectedPatterns) {
//           List<DSPatternMatch<DSSequence, DSSeqRegistration>> matches = selectedPatterns.get(pattern);
//           if ((matches != null) && (matches.size() > 0)) {
//               for (int i = 0; i < matches.size(); i++) {
//                   DSPatternMatch<DSSequence, DSSeqRegistration> match = matches.get(i);
//                   DSSequence sequence = match.getObject();
//                   if (selected == sequence.getSerial()) {
//                       DSSeqRegistration reg = match.getRegistration();
//                       if (dis >= reg.x1 && dis <= reg.x2) {
//                           display = "Pattern:" + pattern;
//                           //displayInfo(display);
//                       }
//                   }
//               }
//           }
//       }
    }

    private void mouseOverLineView(MouseEvent e) throws
            ArrayIndexOutOfBoundsException {
        int y = e.getY();
        int x = e.getX();
        //displayInfo = "";
        if (!singleSequenceView) {
            int seqid = getSeqId(y);

            if (sequenceDB == null) {
                return;
            }
            int off = this.getSeqDx(x);
            DSSequence sequence = sequenceDB.getSequence(seqid);
            if (sequence != null) {
//                displayInfo = "Length of " + sequence.getLabel() + ": " +
//                              sequence.length();
                if ((off <= sequenceDB.getSequence(seqid).length()) && (off > 0)) {
                    this.setToolTipText("" + off);
                    //  displayInfo += ". Current location: " + off;
                }
            }
        } else {

            Font f = new Font("Courier New", Font.PLAIN, 11);
            Graphics g = this.getGraphics();
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                              RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g.getFontMetrics(f);
            if (sequenceDB.getSequence(selected) == null ||
                sequenceDB.getSequence(selected).getSequence() == null) {
                return;
            }
            DSSequence sequence = sequenceDB.getSequence(selected);
            String asc = sequence.getSequence();
            displayInfo = "Length of " + sequence.getLabel() + ": " +
                          sequence.length();
            Rectangle2D r2d = fm.getStringBounds(asc, g);
            double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
            double yscale = 1.3 * r2d.getHeight();
            int width = this.getWidth();
            int cols = (int) (width / xscale) - 8;
            int dis = (int) ((int) ((y - yOff - 1) / yscale) * cols +
                             x / xscale -
                             5);
            if (sequenceDB.getSequence(selected) != null) {
                if (x >= 6 * xscale && x <= (cols + 6) * xscale &&
                    ((y - yOff - 1) / yscale > 0) && (dis > 0) &&
                    (dis <= sequenceDB.getSequence(selected).length())) {
                    this.setToolTipText("" + dis);
                    displayInfo += ". Current location: " + dis;
                }
            }

        }
//       for (DSPattern pattern : patternMatches.keySet()) {
//           List<DSPatternMatch<DSSequence, DSSeqRegistration>> matches = patternMatches.get(pattern);
//           if (matches != null) {
//               for (int i = 0; i < matches.size(); i++) {
//                   DSPatternMatch<DSSequence, DSSeqRegistration> match = matches.get(i);
//                   DSSequence sequence = match.getObject();
//                   if (sequence.getSerial() == seqid) {
//                       DSSeqRegistration reg = match.getRegistration();
//                       if ((off > reg.x1 - 5) && (off < reg.x2 + 4)) {
//
//                           //displayInfo(pattern.toString());
//
//                       }
//                   }
//               }
//           }
//       }
    }

    /**
     * initialize
     */
    public void initialize() {
    }


}
