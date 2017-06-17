package models;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Candidates - a helper class
 */
public class Candidates {
  public static final String[] electable = new String[]{"A", "B", "C", "D"};

  public static boolean isValid(String candidate){
    return Arrays.stream(electable).anyMatch(candidate::equals);
  }

  public static String asString(){
    return Arrays.stream(electable).collect(Collectors.joining(" "));
  }

}
