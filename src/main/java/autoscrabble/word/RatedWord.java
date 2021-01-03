package autoscrabble.word;

import autoscrabble.Direction;

/** A word with a rating as well as location and direction. */
public class RatedWord extends LocatedWord {
  private final double rating;

  public RatedWord(String word, int x, int y, Direction direction, double rating) {
    super(word, x, y, direction, null, false);
    this.rating = rating;
  }

  public double getRating() {
    return rating;
  }

  @Override
  public String toString() {
    return "RatedWord{" + "rating=" + rating + ", word='" + string + '\'' + '}';
  }
}
