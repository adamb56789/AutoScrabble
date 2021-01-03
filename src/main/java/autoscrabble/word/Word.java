package autoscrabble.word;

/** Word base class. */
public abstract class Word {
  public String string;
  public final int length;

  public Word(String word) {
    this.string = word;
    length = word.length();
  }
}
