package autoscrabble.word;

import autoscrabble.Direction;

/** A word with a rating as well as location and direction. */
public class RatedWord extends LocatedWord {
  private final int rating;
  private final double smartRating;

  public RatedWord(
      LocatedWord word, int x, int y, Direction direction, int rating, double smartRating) {
    super(word, x, y, direction);
    this.rating = rating;
    this.smartRating = smartRating;
  }

  public double getSmartRating() {
    return smartRating;
  }

  public int getRating() {
    return rating;
  }

  @Override
  public String toString() {
    return String.format(
        "%s, (%d,%d), %d, %.1f, %d", string, x, y, rating, smartRating, blanksUsed);
  }
}
