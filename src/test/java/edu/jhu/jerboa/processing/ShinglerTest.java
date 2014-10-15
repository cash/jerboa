package edu.jhu.jerboa.processing;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;

public class ShinglerTest {

  @Test
  public void testHasNext() {
    String [] data = {"one", "two", "three"};
    
    Shingler shingler = new Shingler(data);
    
    assertTrue(shingler.hasNext());
    shingler.next();
    assertTrue(shingler.hasNext());
    shingler.next();
    assertFalse(shingler.hasNext());
  }
  
  @Test
  public void testNext() {
    String [] data = {"one", "two", "three"};
    
    Shingler shingler = new Shingler(data);
    
    assertEquals("one two", shingler.next());
    assertEquals("two three", shingler.next());
  }
  
  @Test(expected=NoSuchElementException.class)
  public void testNextNoDataLeft() {
    String [] data = {"one", "two", "three"};
    
    Shingler shingler = new Shingler(data);
    
    shingler.next();
    shingler.next();
    shingler.next();    
  }

}
