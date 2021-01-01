package autoscrabble.word;

/** A letter with a specified location and whether or not letter score bonuses can apply. */
public class Letter {
  private final char letter;
  private final int x;
  private final int y;
  private final boolean justPlaced;

  public Letter(char letter, int x, int y, boolean justPlaced) {
    this.letter = letter;
    this.x = x;
    this.y = y;
    this.justPlaced = justPlaced;
  }

  /**
   * Gets the score of the given letter.
   *
   * @param letter a char containing the letter
   * @return the score as an int
   */
  public static int letterScore(char letter) {
    return switch (letter) {
      case 'A', 'U', 'T', 'S', 'R', 'O', 'N', 'L', 'I', 'E' -> 1;
      case 'D', 'G' -> 2;
      case 'B', 'C', 'P', 'M' -> 3;
      case 'F', 'Y', 'W', 'V', 'H' -> 4;
      case 'K' -> 5;
      case 'J', 'X' -> 8;
      case 'Q', 'Z' -> 10;
      default -> 0;
    };
  }

  public int letterScore() {
    return letterScore(letter);
  }

  public boolean justPlaced() {
    return justPlaced;
  }

  public char getChar() {
    return letter;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }
}
