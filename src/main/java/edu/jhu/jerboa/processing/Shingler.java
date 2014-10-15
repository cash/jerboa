package edu.jhu.jerboa.processing;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Implements bigram shingling
 */
public class Shingler implements Iterator<String> {
  protected String [] words;
  protected int offset1, offset2;
  
  public Shingler (String [] words) {
    this.words = words;
    offset1 = 0;
    offset2 = 1;
  }

  @Override
  public boolean hasNext () {
    return offset2 < words.length;
  }

  @Override
  public String next () {
    try {
      return words[offset1++] + " " + words[offset2++];
    } catch (ArrayIndexOutOfBoundsException ex) {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove () {
    throw new UnsupportedOperationException();
  }

}
