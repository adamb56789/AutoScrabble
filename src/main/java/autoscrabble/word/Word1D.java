package autoscrabble.word;

/** A word with an index specifying its starting position in a line */
public class Word1D extends Word {
  private final int startIndex;

  public Word1D(String word, int startIndex) {
    super(word);
    this.startIndex = startIndex;
  }

  @Override
  public String toString() {
    return "Word1D{" + "startIndex=" + startIndex + ", word='" + word + '\'' + '}';
  }

  public int getStartIndex() {
    return startIndex;
  }
}
