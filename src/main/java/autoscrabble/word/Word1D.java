package autoscrabble.word;

/** A word with an index specifying its starting position in a line */
public class Word1D {
  private final char[] chars;
  private final int startIndex;

  public Word1D(char[] chars, int startIndex) {
    this.chars = chars;
    this.startIndex = startIndex;
  }

  @Override
  public String toString() {
    return "Word1D{" + "startIndex=" + startIndex + ", word='" + String.valueOf(chars) + '\'' + '}';
  }

  public int getStartIndex() {
    return startIndex;
  }

  public char[] getChars() {
    return chars;
  }

  public String getAsString() {
    return String.valueOf(chars);
  }
}
