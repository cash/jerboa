package edu.jhu.jerboa.counting;

import java.util.logging.Logger;
import java.util.Hashtable;

import ilog.concert.*;
import ilog.cplex.*;

import edu.jhu.jerboa.util.JerboaProperties;
import edu.jhu.jerboa.classification.*;

/**
   @author Alex Clemmer <clemmer.alexander@gmail.com>
   @since 2012-5-22
*/
public class BloomParamOpt {
    private static Logger logger =
	Logger.getLogger(BloomParamOpt.class.getName());
    private static String propPrefix;

    private long numElements = -1;
    private long numBits;
    private double kmax;
    private Hashtable<String,Double> weights;

    private String outputFilename;

    public BloomParamOpt () throws Exception {
	this.propPrefix = "BloomParamOpt";
	
	this.weights = getWeights();
	this.numElements = JerboaProperties.getInt(propPrefix +
						   ".numElements",
						   this.weights.size());
	this.numBits =
	    parseNumBits(JerboaProperties.getString(propPrefix + ".numBits"));
	this.kmax = JerboaProperties.getDouble(propPrefix + ".kmax", 2);
	this.outputFilename = JerboaProperties.getString(propPrefix +
							 ".outputFilename");
    }
    
    public void optimize () {
	logger.info("Optimizing Bloom filter parameters");
    }

    /**
       Returns a hashtable that pairs features (strings) with their weights
       (doubles).
     */
    private Hashtable<String,Double> getWeights () throws Exception {
	String modelClass = JerboaProperties.getString(propPrefix +
						       ".modelClass");
	IClassifier classifier = (IClassifier)
	    Class.forName(modelClass).newInstance();
	classifier.initialize();
	classifier.readState();

	return classifier.getWeights();
    }

    private long parseNumBits (String input) {
	if (this.numElements < 0) {
	    throw new ClassFormatError("Called parseNumBits before " +
				       "BloomParamOpt.numElements was set");
	}

	int indexOfN = input.indexOf('n');
	int len = input.length();

	if (indexOfN == -1) {
	    return (long) Double.parseDouble(input);
	}
	else if (indexOfN == len - 1) {
	    return (long) (this.numElements *
			   Double.parseDouble(input.substring(0, len - 1)));
	}
	else {
	    throw new NumberFormatException("Invalid format to parseNumBits");
	}
    }
    
    public static void main(String[] args) {
	try {
	    BloomParamOpt optimizer = new BloomParamOpt();
	    optimizer.optimize();
	}
	catch (Exception err) {
	    logger.severe("BloomParamOpt failed to optimize parameters");
	    logger.severe(err.toString());
	    System.err.println("BloomParamOpt failed to optimize parameters");
	    System.err.println(err);
	    System.exit(1);
	}
    }
}