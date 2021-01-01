package autoscrabble.word;

/** Word base class. */
public abstract class Word {
  protected String word;

  public Word(String word) {
    this.word = word;
  }

  public String getWord() {
    return word;
  }

  public void setWord(String word) {
    this.word = word;
  }
}
