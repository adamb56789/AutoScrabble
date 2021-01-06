package autoscrabble.word;

/** Word base class. */
public abstract class Word {
  public final int length;
  public String string;

  public Word(String word) {
    this.string = word;
    length = word.length();
  }
}
