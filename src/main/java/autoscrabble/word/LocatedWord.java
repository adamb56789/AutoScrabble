package autoscrabble.word;

import autoscrabble.Direction;
import autoscrabble.Rater;

/** A word with a specified starting location and direction. */
public class LocatedWord extends Word {
  private final int x;
  private final int y;
  private final Direction direction;

  public LocatedWord(String word, int x, int y, Direction direction) {
    super(word);
    this.x = x;
    this.y = y;
    this.direction = direction;
  }

  public RatedWord getRatedWord(Rater rater) {
    return new RatedWord(word, x, y, direction, rater.rate(this));
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public Direction getDirection() {
    return direction;
  }

  public boolean isHorizontal() {
    return direction == Direction.HORIZONTAL;
  }
}
