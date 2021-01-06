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

  public RatedWord(RatedWord word, double smartRating) {
    super(word, word.x, word.y, word.direction);
    rating = word.getRating();
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
            "%s at (%c%d) %s, scoring %s",
            string,
            ((char) (x + 97)),
            (15 - y),
            isHorizontal ? "horizontally" : "vertically",
            getRating());
  }
}
