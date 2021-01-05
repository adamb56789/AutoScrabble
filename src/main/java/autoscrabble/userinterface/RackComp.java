package autoscrabble.userinterface;

import autoscrabble.Board;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** A UI component for the rack and its tiles */
public class RackComp extends JComponent {
  public static final Color RACK_COLOUR = Color.decode("#8C4000");
  public static final int RACK_TILE_SPACING = 4;
  public static final int RACK_HEIGHT =
      Board.RACK_CAPACITY * BoardComp.SQUARE_SIZE + (Board.RACK_CAPACITY + 1) * RACK_TILE_SPACING;
  public static final int RACK_WIDTH = BoardComp.SQUARE_SIZE + 2 * RACK_TILE_SPACING;
  private static final String MOVE_UP = "move up";
  private static final String MOVE_DOWN = "move down";
  private final Board board;
  private int selection = 0;

  public RackComp(Board board) {
    this.board = board;
    var dimension = new Dimension(RACK_WIDTH, RACK_HEIGHT);
    setPreferredSize(dimension);
    setMinimumSize(dimension);
    // Add listeners
    addMouseListener(new SelectTileMouseListener());
    addKeyListener(new LetterKeyListener());

    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), MOVE_UP);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("LEFT"), MOVE_UP);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), MOVE_DOWN);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("RIGHT"), MOVE_DOWN);

    getActionMap().put(MOVE_UP, new MoveAction(-1));
    getActionMap().put(MOVE_DOWN, new MoveAction(1));

    addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusGained(FocusEvent e) {
            // Repaint on focus to update selection boxes
            getParent().repaint();
          }
        });
  }

  @Override
  protected void paintComponent(Graphics g) {
    ((Graphics2D) g)
        .setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    // Draw background
    g.setColor(RACK_COLOUR);
    g.fillRect(0, 0, RACK_WIDTH, RACK_HEIGHT);

    // Draw the tiles
    for (int i = 0; i < board.getRack().length; i++) {
      if (board.getRack()[i] != ' ') {
        if (board.getRack()[i] == '_' || Character.isAlphabetic(board.getRack()[i])) {
          int tileY = RACK_TILE_SPACING + i * (BoardComp.SQUARE_SIZE + RACK_TILE_SPACING);
          Tile.paintTile(g, board.getRack()[i], RACK_TILE_SPACING, tileY);
        }
      }
    }

    // Draw the selection if in focus
    if (selection != -1 && isFocusOwner()) {
      int tileY = RACK_TILE_SPACING + selection * (BoardComp.SQUARE_SIZE + RACK_TILE_SPACING);
      Tile.drawSelectionBox((Graphics2D) g, RACK_TILE_SPACING, tileY);
    }
  }

  private void moveSelectionBy(int i) {
    selection = Math.floorMod(selection + i, Board.RACK_CAPACITY);
  }

  public void resetSelection() {
    selection = 0;
  }

  private class MoveAction extends AbstractAction {
    private final int x;

    MoveAction(int x) {
      this.x = x;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      moveSelectionBy(x);
      repaint();
    }
  }

  private class SelectTileMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      requestFocus(); // Focus on click
      selection = (e.getY() + RACK_TILE_SPACING / 2) / (BoardComp.SQUARE_SIZE + RACK_TILE_SPACING);
      getParent().repaint(); // Repaint to update selection boxes
    }
  }

  private class LetterKeyListener extends KeyAdapter {
    @Override
    public void keyTyped(KeyEvent e) {
      char keyChar = e.getKeyChar();
      if (keyChar == KeyEvent.VK_BACK_SPACE) {
        board.placeInRack(' ', selection);
        moveSelectionBy(-1);
      } else if (Character.isAlphabetic(keyChar)) {
        board.placeInRack(keyChar, selection);
        moveSelectionBy(1);
      } else if (keyChar == ' ' || keyChar == '-') {
        board.placeInRack('_', selection);
        moveSelectionBy(1);
      }
      repaint();
    }
  }
}
