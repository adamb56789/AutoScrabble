package autoscrabble.word;

/** A letter with a specified location and whether or not letter score bonuses can apply. */
public class Letter {
  private char letter;
  private int x;
  private int y;
  private boolean bonusAllowed;

  public Letter(char letter, int x, int y, boolean bonusAllowed) {
    this.letter = letter;
    this.x = x;
    this.y = y;
    this.bonusAllowed = bonusAllowed;
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

  public boolean bonusAllowed() {
    return bonusAllowed;
  }

  public void setBonusAllowed(boolean bonusAllowed) {
    this.bonusAllowed = bonusAllowed;
  }

  public char getChar() {
    return letter;
  }

  public void setLetter(char letter) {
    this.letter = letter;
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
}
