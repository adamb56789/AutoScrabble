package autoscrabble;

import java.util.Arrays;

public class LineData {
  public char[] line;
  public boolean[] alreadyRated;
  public boolean containsEntireWord;
  public int letterIndex;

  @Override
  public String toString() {
    return "LineData{" +
            "line=" + Arrays.toString(line) +
            '}';
  }

  public LineData(char[] line, boolean[] alreadyRated, boolean containsEntireWord, int letterIndex) {
    this.line = line;
    this.alreadyRated = alreadyRated;
    this.containsEntireWord = containsEntireWord;
    this.letterIndex = letterIndex;
  }
}
