package autoscrabble.word;

/** A word with an index specifying its starting position in a line */
public class Word1D extends Word {
  public final int startIndex;
  public final int[] blankRequirements;
  public final int[] placedLetterFrequency;
  public final int blanksUsed;

  public Word1D(
      String word,
      int startIndex,
      int[] blankRequirements,
      int blanksUsed,
      int[] placedLetterFrequency) {
    super(word);
    this.startIndex = startIndex;
    this.blankRequirements = blankRequirements;
    this.blanksUsed = blanksUsed;
    this.placedLetterFrequency = placedLetterFrequency;
  }

  @Override
  public String toString() {
    return "Word1D{" + "startIndex=" + startIndex + ", word='" + string + '\'' + '}';
  }
}
