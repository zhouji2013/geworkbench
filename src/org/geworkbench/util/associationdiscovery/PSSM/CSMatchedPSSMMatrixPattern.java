package org.geworkbench.util.associationdiscovery.PSSM;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.pattern.CSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.util.DSPValue;
import org.geworkbench.util.associationdiscovery.cluster.CSMatchedMatrixPattern;

import java.util.HashMap;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 */

public class CSMatchedPSSMMatrixPattern extends CSMatchedMatrixPattern {
    public class GeneScore implements Comparable {
        public int microarrayId = -1;
        public double score = 0.0;
        public int invalid = 0;

        public int compareTo(Object gs) {
            Double s1 = new Double(score);
            Double s2 = new Double(((GeneScore) gs).score);
            return s1.compareTo(s2);
        }
    }

    double maNo = 0.0;
    double threshold = 0;
    //ArrayList Genes        = new ArrayList();
    org.geworkbench.bison.util.Normal distribution = new org.geworkbench.bison.util.Normal();
    static double sigmaX = 2.8;


    public GeneScore score(DSMicroarray chip) {
        return null;
    }

    protected CSPSSMMatrixPattern pattern = null;

    public CSMatchedPSSMMatrixPattern(CSMatchedMatrixPattern aPattern, DSMicroarraySet microarraySet) {
        super(null);
        // assigns all the pattern instance variables
        this.matches.addAll(aPattern.matches());
        this.pattern = new CSPSSMMatrixPattern();
        // Determines if alleles are matched independently
        pattern.isAllele = (microarraySet.getType() == DSMicroarraySet.snpType);
    }

    /*
    public void trainPSSM(DSMicroarraySet microarraySet) {
        // The PSSM is generated by computing the observed counts for each individual allele n(i)
        // a pseudo-count Nb(i) = 5 * ac. This is estimated according to the optimal method in:
        // "Using substitution probabilities to improve position-specific scoring matrices"
        // by Jorja G. Henikoff and Steven Henikoff*
        // where ac is the number of allele counts in the column.
        mean           = 0.0;
        sigma          = 0.0;
        pssm           = new HashMap[getMarkerIdNo()];
        maNo           = getMicroArrayIdNo();
        HashMap PSSMFG = new HashMap();
        HashMap PSSMBG = new HashMap();
        // First generate the counts for the background population
        for(int markerId = 0; markerId < markerIdNo; markerId++) {
            int geneId = getMarkerId(markerId);
            pssm[markerId] = new HashMap();
            PSSMBG = new HashMap();
            //PSSMBG.put(nullKey, new GeneValue(1));
            for(int j = 0; j < microarraySet.getMicroarrayNo(); j++) {
                boolean undefined = microarraySet.get(j).getMarker(geneId).isUndefined();
                Double  keyA = null;
                Double  keyB = null;
                IMarker marker = microarraySet.get(j).getMarker(geneId);
                double v0 = 0;
                double v1 = 0;
                if(isAllele) {
                    JMarkerGenotype gt = (JMarkerGenotype)marker;
                    v0 = (double)gt.getAllele(0);
                    v1 = (double)gt.getAllele(1);
                } else {
                    JMarkerExpression ge = (JMarkerExpression)marker;
                    v0 = ge.getSignal();
                }
                if(!undefined) {
                    keyA = new Double(v0);
                    keyB = new Double(v1);
                    GeneValue gv = (GeneValue)PSSMBG.get(keyA);
                    if (gv != null) {
                        gv.count++;
                    } else {
                        PSSMBG.put(keyA, new GeneValue(1));
                    }
                    if (isAllele) {
                        gv = (GeneValue)PSSMBG.get(keyB);
                        if (gv != null) {
                            gv.count++;
                        } else {
                            PSSMBG.put(keyB, new GeneValue(1));
                        }
                    }
                }
            }
            // Now compute the distribution for the pattern population
            PSSMFG.clear();
            for(int j = 0; j < maNo; j++) {
                int     chipId    = getMicroArrayId(j);
                boolean undefined = microarraySet.get(chipId).getMarker(geneId).isUndefined();
                if(!undefined) {
                    IMarker marker = microarraySet.get(chipId).getMarker(geneId);
                    double v0 = 0;
                    double v1 = 0;
                    if(isAllele) {
                        JMarkerGenotype gt = (JMarkerGenotype)marker;
                        v0 = (double)gt.getAllele(0);
                        v1 = (double)gt.getAllele(1);
                    } else {
                        JMarkerExpression ge = (JMarkerExpression)marker;
                        v0 = ge.getSignal();
                    }
                    Double    keyA = new Double(v0);
                    Double    keyB = new Double(v1);
                    GeneValue gv  = (GeneValue)PSSMFG.get(keyA);
                    if(gv != null) {
                        gv.count ++;
                    } else {
                        PSSMFG.put(keyA, new GeneValue(1));
                    }
                    if(isAllele) {
                        gv  = (GeneValue)PSSMFG.get(keyB);
                        if(gv != null) {
                            gv.count ++;
                        } else {
                            PSSMFG.put(keyB, new GeneValue(1));
                        }
                    }
                }
            }
            // Now compute the pseudo counts
            pseudoNo[markerId] = 5 * PSSMFG.size();
            // Now assign the PSSM scores as the log ration of the weighed averages of the counts and pseudo counts
            // and the background probability
            double mean  = 0.0;
            double sigma = 0.0;
            double pTot  = 0.0;
            Set keys = PSSMBG.keySet();
            Iterator it = keys.iterator();
            while(it.hasNext()) {
                Double key = (Double)it.next();
                GeneValue gvBg  = (GeneValue)PSSMBG.get(key);
                GeneValue gvFg  = (GeneValue)PSSMFG.get(key);
                double    countBg = gvBg.count/2.0;
                double    countFg = 0;
                if(gvFg != null) {
                    countFg = gvFg.count/2.0;
                }
                double pBg     = countBg / (double)microarraySet.getMicroarrayNo();
                double pMarker1 = (countFg + pseudoNo[markerId] * pBg)/(pseudoNo[markerId] + maNo);
                double pMarker2 = (countFg + 0.5 * pBg)/(0.5 + maNo);
                double score1   = Math.log(pMarker1/pBg);
                double score2   = Math.log(pMarker2/pBg);
                pssm[markerId].put(key, new GeneValue(score1));
                mean  += score2 * pMarker2;
                sigma += score2 * score2 * pMarker2;
                pTot  += pMarker2;
            }
            double pMarker = Math.log(pseudoNo[markerId] / (pseudoNo[markerId] + maNo));
            pssm[markerId].put(nullKey, new GeneValue(pMarker));
            mean  += mean;
            sigma += sigma - mean*mean;
        }
        sigma = Math.sqrt(sigma);
    }
*/
    private void getParameters() {
    }

    /*
        public void inflate(DSMicroarraySet set) {
            ArrayList results = new ArrayList();
            Iterator chipIt = set.iterator(ClassificationCriteria.selAll);
            while (chipIt.hasNext()) {
                IMicroarray chip = (IMicroarray)chipIt.next();
                if(this.isAMatch(chip)) {
                    results.add(new Integer(chip.getSerial()));
                }
            }
            setMicroArrayIds(new int[results.size()]);
            setMicroArrayIdNo(results.size());
            Iterator it = results.iterator();
            int id = 0;
            while(it.hasNext()) {
                int chipId = ((Integer)it.next()).intValue();
                getMicroArrayIds()[id++] = chipId;
            }
        }
        public void checkAdditionalGenes(DSMicroarraySet set, double entropyThr) {
            //*JGTConsole.ClearLog();
            ArrayList genes = new ArrayList();
            for(int geneId = 0; geneId < set.getMarkerNo(); geneId++) {
                HashMap histogram = new HashMap();
                for(int id = 0; id < maNo; id++) {
                    int chipId = getMicroArrayId(id);
                    Double key = null;
                    IMarker spot = set.get(chipId).getMarker(geneId);
                    if(spot.isValid()) {
                        key = new Double(spot.getSignal());
                    } else {
                        key = new Double((int)(256 * Math.random()));
                    }
                    GeneValue gv = (GeneValue)histogram.get(key);
                    if(gv == null) {
                        histogram.put(key, new GeneValue(1));
                    } else {
                        gv.count++;
                    }
                }
                Iterator iterator = histogram.values().iterator();
                double entropy = 0.0;
                while(iterator.hasNext()) {
                    GeneValue gv = (GeneValue)iterator.next();
                    double  p     = (double)gv.count/maNo;
                    entropy -= p * Math.log(p);
                }
                if(entropy < entropyThr) {
                    //*JGTConsole.ListLog("Entropy ["+ geneId +"]: "+entropy);
                    genes.add(new Integer(geneId));
                }
            }
            markerId = new int[genes.size()];
            for(int i = 0; i < genes.size(); i++) {
                markerId[i] = ((Integer)genes.get(i)).intValue();
            }
        }
        public GeneScore score(IMicroarray chip) {
            GeneScore gscore = new GeneScore();
            // For each gene in the pattern find if the value is the same
            for(int j =0; j < markerIdNo; j++) {
                int           geneId   = getMarkerId(j);
                //JGeneProperty property = Properties[geneId];
                double        pValueA = 0.0;
                double        pValueB = 0.0;
                if(chip.getMarker(geneId).isUndefined()) {
                    gscore.invalid++;
                    pValueA = ((GeneValue)pssm[j].get(nullKey)).count;
                } else {
                    IMarker marker = chip.getMarker(geneId);
                    double    v0  = 0.0;
                    double    v1  = 0.0;
                    if(isAllele) {
                        JMarkerGenotype gt = (JMarkerGenotype)marker;
                        v0 = (double)gt.getAllele(0);
                        v1 = (double)gt.getAllele(1);
                    } else {
                        JMarkerExpression ge = (JMarkerExpression)marker;
                        v0 = ge.getSignal();
                    }
                    Double    key = new Double(v0);
                    GeneValue gv  = (GeneValue)pssm[j].get(key);
                    if(gv != null) { // We have observed this value in the phenotype
                        pValueA = gv.count;
                    } else {
                        pValueA = pseudoNo[j];
                    }
                    if(isAllele) {
                        key = new Double(v1);
                        gv  = (GeneValue)pssm[j].get(key);
                        if(gv != null) { // We have observed this value in the phenotype
                            pValueB = gv.count;
                        } else {
                            pValueB = pseudoNo[j];
                        }
                    }
                }
                gscore.score += pValueA;
                gscore.score += pValueB;
            }
            return gscore;
        }
        public GeneScore monteCarloScore() {
            GeneScore score = new GeneScore();
            double value = 0.0;
            double count = 0;
            double weigh = 1.0;
            for(int i = 0; i < markerIdNo; i++) {
                double no    = pseudoNo[i] + microarrayIdNo;
                int geneId = getMarkerId(i);
                //JGeneProperty property = Properties[geneId];
                // Assign a pseudo-count so that values that are not realized
                // in the training set can be properly accounted for
                int       rand     = (int)(Math.random() * (microarrayIdNo + pseudoNo[i]));
                int       totCount = 0;
                boolean   found    = false;
                GeneValue gv       = null;
                Iterator it = pssm[i].keySet().iterator();
                while (it.hasNext()) {
                    Object key = it.next();
                    gv = (GeneValue)pssm[i].get(key);
                    totCount += gv.count;
                    if(rand < totCount) {
                        value = ((Double)key).doubleValue();
                        weigh = getScoreCountGene(i, value);
                        count = (int)gv.count;
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    count = 1;
                }
                double partialScore = Math.log(weigh * count/no);
                score.score += partialScore;
            }
            System.out.println();
            return score;
        }
        public double getScoreCountGene(int geneId, double value1) {
            double    totCount = 0.0;
            GeneValue gvalue   = null;
            if(value1 > 3) {
                Iterator it = pssm[geneId].keySet().iterator();
                while (it.hasNext()) {
                    Object key = it.next();
                    gvalue = (GeneValue)pssm[geneId].get(key);
                    double    value2 = ((Double)key).doubleValue();
                    double weigh = (1.0 + 1.0/(Math.abs(value2 - value1) + 1));
                    totCount = weigh * gvalue.count;
                }
            } else {
                Double key = new Double(value1);
                gvalue = (GeneValue)pssm[geneId].get(key);
                if(gvalue != null) {
                    totCount = gvalue.count;
                } else {
                    totCount = 1;
                }
            }
            return totCount;
        }

        public void train(DSMicroarraySet set) {
            int id = 0;
            Iterator chipIt = set.iterator(ClassificationCriteria.selAll);
            //*JGTConsole.ClearLog();
            while (chipIt.hasNext()) {
                IMicroarray chip = (IMicroarray)chipIt.next();
                if((id < getMicroArrayIdNo()) && (getMicroArrayId(id) != chip.getSerial())) {
                    GeneScore score = score(chip);
                    //*JGTConsole.ListLog("Chip {" + chip.GetSerial() + "}: " + score.Invalid);
                    if(score.invalid == 0) {
                        distribution.add(score.score);
                    }
                } else {
                    id++;
                }
            }
            isTrained = true;
        }
        public void monteCarloTrain() {
            //Iterator chipIt = Chips.Iterator(JSelection.SelAll);
            //while (chipIt.hasNext()) {
            //JGeneChip chip = (JGeneChip)chipIt.next();
            //if(chip.GetSelection() == JSelection.SelControl) {
            for(int i = 0; i < 1000; i++) {
                GeneScore score = monteCarloScore();
                if(score.invalid == 0) {
                    distribution.add(score.score);
                }
                //}
            }
            isTrained = true;
        }
        public boolean isAMatch(IMicroarray chip) {
            if(!isTrained) {
                monteCarloTrain();
            }
            GeneScore score = score(chip);
            getParameters();
            String string = new String("Chip [" + chip.getSerial() +"]: " + score.score + " " + (distribution.getMean() - sigmaX * distribution.getSigma())+" "+distribution.getMean()+" "+distribution.getSigma());
            //*JenGeniousApp.Frame.jPLogicTab.scoreList.addElement(string);
            if(score.score > distribution.getMean() - sigmaX * distribution.getSigma()) {
                return true;
            }
            return false;
        }
        public boolean isAStrictMatch(IMicroarray chip) {
            for(int j =0; j < markerIdNo; j++) {
                int           geneId   = getMarkerId(j);
                //JGeneProperty property = Properties[geneId];
                double        count    = 1.0;
                double        weigh    = 1.0;
                if(chip.getMarker(geneId).isUndefined()) {
                    return false;
                } else {
                    Double key = new Double(chip.getMarker(geneId).getSignal());
                    GeneValue gv = (GeneValue)pssm[j].get(key);
                    if(gv == null) {
                        return false;
                    }
                }
            }
            return true;
        }
     */
    public void addMicroArray(DSMicroarray array) {
        DSPatternMatch<DSMicroarray, DSPValue> match = new CSPatternMatch<DSMicroarray, DSPValue>(array);
        pattern.pssm = new HashMap[getPattern().markers().length];
    }
    /*
   public boolean matchesMicroarray(DSMicroarraySet set, int mArrayId) {
       GeneScore score = score(set.get(mArrayId));
       if(score.score > threshold) {
           return true;
       }
       return false;
   }
   public void setThreshold(double thr) {
       threshold = thr;
   }
   public double getThreshold() {
       return threshold;
   }
   public static double optimalIntersection(double x0, double s0, double x1, double s1) {
       if(s0 == 0) {
           return x0;
       }
       if(s1 == 0) {
           return x1;
       }
       double ss0 = s0*s0;
       double ss1 = s1*s1;
       double a = ss0 - ss1;
       double b = -2.0 * (x1 * ss0 - x0 * ss1);
       double c = ss0*x1*x1 - ss1*x0*x0 -2.0 * ss0 * ss1 * Math.log(s0/s1);
       double ansatz = Math.sqrt(Math.abs(b*b) - 4.0 * a * c);
       double solution0 = (-b + ansatz)/(2.0 * a);
       double solution1 = (-b - ansatz)/(2.0 * a);
       if((solution0 > Math.min(x0,x1)) && (solution0 < Math.max(x0,x1))) {
           NormalDistribution normal1 = new NormalDistribution(x0, s0);
           NormalDistribution normal2 = new NormalDistribution(x1, s1);
           double v1 = normal1.getCDF(solution0);
           double v2 = normal2.getCDF(solution0);
           return solution0;
       } else {
           NormalDistribution normal1 = new NormalDistribution(x0, s0);
           NormalDistribution normal2 = new NormalDistribution(x1, s1);
           double v1 = normal1.getDensity(solution1);
           double v2 = normal2.getDensity(solution1);
           return solution1;
       }
   }
          */
}
