package autoscrabble.word;

import autoscrabble.Direction;
import autoscrabble.Rater;

/**
 * A word with a specified starting location and direction.
 */
public class LocatedWord extends Word{
  private int x;
  private int y;
  private Direction direction;

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

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public Direction getDirection() {
    return direction;
  }

  public void setDirection(Direction direction) {
    this.direction = direction;
  }
}
