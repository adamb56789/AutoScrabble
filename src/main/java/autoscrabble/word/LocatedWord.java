package autoscrabble.word;

import autoscrabble.Direction;
import autoscrabble.Rater;

/** A word with a specified starting location and direction. */
public class LocatedWord extends Word1D {
  public final int x;
  public final int y;
  public final Direction direction;
  public final boolean isHorizontal;

  public LocatedWord(Word1D word, int x, int y, Direction direction) {
    super(word.string, word.startIndex, word.blankRequirements, word.blanksUsed, word.placedLetterFrequency);
    this.x = x;
    this.y = y;
    this.direction = direction;
    isHorizontal = direction == Direction.HORIZONTAL;
  }

  public RatedWord getRatedWord(Rater rater) {
    int rating = rater.rate(this);
    return new RatedWord(this, x, y, direction, rating, rating + rater.smartLetterRating(this));
  }
}
