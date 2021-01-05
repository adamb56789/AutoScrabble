package autoscrabble.word;

import autoscrabble.Direction;

public class Line {
  public final String string;
  public final Direction direction;
  public final int index;

  public Line(String string, Direction direction, int index) {
    this.string = string;
    this.direction = direction;
    this.index = index;
  }
}
