package autoscrabble.word;

import autoscrabble.Direction;
import autoscrabble.Rater;

import java.util.Arrays;

/** A word with a specified starting location and direction. */
public class LocatedWord extends BlankRequiredWord {
  public final int x;
  public final int y;
  public final Direction direction;
  public final boolean isHorizontal;

  public LocatedWord(Word1D word, int x, int y, Direction direction) {
    this(word.string, x, y, direction, word.blankRequirements, word.blanksNeeded);
  }

  protected LocatedWord(
      String word,
      int x,
      int y,
      Direction direction,
      int[] blankRequirements,
      boolean needsBlanks) {
    super(word, blankRequirements, needsBlanks);
    this.x = x;
    this.y = y;
    this.direction = direction;
    isHorizontal = direction == Direction.HORIZONTAL;
  }

  public RatedWord getRatedWord(Rater rater) {
    return new RatedWord(string, x, y, direction, rater.rate(this));
  }
}
