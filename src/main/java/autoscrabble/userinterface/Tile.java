package autoscrabble.userinterface;

import autoscrabble.word.Letter;

import java.awt.*;

public final class Tile {
  private static final Font LETTER_FONT = new Font("Arial", Font.PLAIN, 30);
  private static final Font SCORE_FONT = new Font("Arial", Font.PLAIN, 12);

  private static final Color TEXT_COLOUR = Color.black;

  private static final int TILE_LETTER_X = 4;
  private static final int TILE_LETTER_Y = 32;
  private static final int TILE_SCORE_Y = 34;
  private static final int TILE_SCORE_X = 29;
  private static final int TILE_SCORE_X_CORRECTION = 4;

  private static final Color SELECTION_COLOUR = Color.red;
  private static final int SELECTION_INTERIOR_OVERLAP = 2;
  private static final int SELECTION_LINE_WEIGHT = 4;

  /** Draw a tile with the letter at the specified location. */
  public static void paintTile(Graphics g, char letter, int x, int y) {
    // The blank tile image
    g.drawImage(BoardComp.tileImage, x, y, null);

    // The letter
    g.setFont(LETTER_FONT);
    g.setColor(TEXT_COLOUR);
    if (letter != '_') { // Do not draw if is a blank
      g.drawString(String.valueOf(letter), x + TILE_LETTER_X, y + TILE_LETTER_Y);
    }

    // The score
    g.setFont(SCORE_FONT);
    String score = Integer.toString(Letter.letterScore(letter));
    int scoreX = x + TILE_SCORE_X;
    if (score.length() > 1) { // If it is a bigger number it needs more space
      scoreX = x + TILE_SCORE_X - TILE_SCORE_X_CORRECTION;
    }
    g.drawString(score, scoreX, y + TILE_SCORE_Y);
  }

  /** Draw a selection box at the specified location */
  public static void drawSelectionBox(Graphics2D g, int x, int y) {
    g.setColor(SELECTION_COLOUR);
    int rectSize = BoardComp.SQUARE_SIZE + SELECTION_LINE_WEIGHT - 2 * SELECTION_INTERIOR_OVERLAP;
    int squareOffset = SELECTION_INTERIOR_OVERLAP - SELECTION_LINE_WEIGHT / 2;

    g.setStroke(new BasicStroke(SELECTION_LINE_WEIGHT));
    g.drawRect(x + squareOffset, y + squareOffset, rectSize, rectSize);
  }
}
