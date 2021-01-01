package autoscrabble.word;

/** A word with an index specifying its starting position in a line */
public class LineWord extends Word {
  private int startIndex;

  public LineWord(String word, int startIndex) {
    super(word);
    this.startIndex = startIndex;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }
}
