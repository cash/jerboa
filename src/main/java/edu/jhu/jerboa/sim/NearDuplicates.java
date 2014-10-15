// Copyright 2010-2012 Benjamin Van Durme. All rights reserved.
// This software is released under the 2-clause BSD license.
// See jerboa/LICENSE, or http://cs.jhu.edu/~vandurme/jerboa/LICENSE

// Benjamin Van Durme, vandurme@cs.jhu.edu, 27 Aug 2012

package edu.jhu.jerboa.sim;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

import edu.jhu.jerboa.processing.Shingler;
import edu.jhu.jerboa.util.*;

import java.io.BufferedReader;
import java.util.logging.Logger;

/**
   @author Benjamin Van Durme

   Near duplicate detection via LSH
   
   Depends on Jerboa properties:
     * PLEBIndex.P
     * PLEBIndex.comparator
     * SLSH.numBits
     * SLSH.poolSize
     * SLSH.seed
*/
public class NearDuplicates {
  private static final Logger logger = Logger.getLogger(NearDuplicates.class.getName());
  SLSH slsh;

  public NearDuplicates () throws Exception {
    slsh = new SLSH();
  }

  /**
     Builds a signature based on features
  */
  public void add (String key, Iterator<String> features) {
    slsh.update(key,features);
    slsh.buildSignature(key,true);
  }

  /**
    Finds duplicates for each member of the signatures added
  */
  public Hashtable<String,LinkedList<String>> findDuplicates (double cosineThreshold, int maxCandidates) throws Exception {
    Hashtable<String,LinkedList<String>> duplicates = new Hashtable<String,LinkedList<String>>();
    PLEBIndex<String> pleb = new PLEBIndex<String>();
    int numSorts = JerboaProperties.getInt("PLEBIndex.P",4);
    // beamWidth calculations in PLEB use integer division
    int beamWidth = 2 * (int)Math.ceil(maxCandidates / 2.0);
    // PLEB always includes self with the matches so we ask for an additional match
    maxCandidates++;
    
    pleb.initialize(slsh.signatures, numSorts, true);
    
    for (String key : pleb.keys) {
      KBest<String> best = pleb.kbest(key, maxCandidates, beamWidth, numSorts);
      LinkedList<String> matches = new LinkedList<String>();
      for (SimpleImmutableEntry<String,Double> dupe : best.toArray()) {
        if (!dupe.getKey().equals(key) && dupe.getValue() > cosineThreshold) {
          matches.add(dupe.getKey());
        }
      }
      duplicates.put(key, matches);
    }

    return duplicates;
  }
  
  /**
     Reads in a file containing one document per line, assigning the key to be
     the line number, prints out the duplicates.
  */
  public static void main (String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Usage: java -DJerboaProperties.filename=... NearDuplicates FILE cosineThreshold maxCandidates\n");
      System.exit(-1);
    }
    double cosineThreshold = Double.parseDouble(args[1]);
    int maxCandidates = Integer.parseInt(args[2]);
    BufferedReader reader = FileManager.getReader(args[0]);
    
    NearDuplicates nd = new NearDuplicates();
    
    int lineNumber = 0;
    String line;
    while ((line = reader.readLine()) != null) {
      nd.add("" + lineNumber, new Shingler(line.split("\\s+")));
      lineNumber++;
    }
    reader.close();

    Hashtable<String,LinkedList<String>> duplicates = nd.findDuplicates(cosineThreshold, maxCandidates);

    for (Map.Entry<String,LinkedList<String>> entry : duplicates.entrySet()) {
      System.out.print(entry.getKey() + "\t");
      for (String match : entry.getValue()) {
        System.out.print(match + ",");
      }
      System.out.println();
    }
  }
}
