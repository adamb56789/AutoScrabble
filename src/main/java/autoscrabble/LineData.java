package autoscrabble;

public class LineData {
  public final char[] line;
  public final boolean[] alreadyRated;
  public final boolean containsEntireWord;
  public final int letterIndex;
  public final int firstPlacedTileIndex;

  public LineData(
      char[] line,
      boolean[] alreadyRated,
      boolean containsEntireWord,
      int letterIndex,
      int firstPlacedTileIndex) {
    this.line = line;
    this.alreadyRated = alreadyRated;
    this.containsEntireWord = containsEntireWord;
    this.letterIndex = letterIndex;
    this.firstPlacedTileIndex = firstPlacedTileIndex;
  }
}
