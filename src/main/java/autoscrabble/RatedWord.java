package autoscrabble;

public class RatedWord extends Word {
  private double rating;

  public RatedWord(String word, int x, int y, Direction direction, double rating) {
    super(word, x, y, direction);
    this.rating = rating;
  }

  public double getRating() {
    return rating;
  }

  public void setRating(double rating) {
    this.rating = rating;
  }
}
