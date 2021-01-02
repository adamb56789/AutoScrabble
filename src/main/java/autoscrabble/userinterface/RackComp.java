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
  private int rackSelection = -1;

  public RackComp(Board board) {
    this.board = board;
    setPreferredSize(new Dimension(RACK_WIDTH, RACK_HEIGHT));
    // Add listeners
    addMouseListener(new SelectTileMouseListener());
    addKeyListener(new LetterKeyListener());

    setFocusable(true);

    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("UP"), MOVE_UP);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("LEFT"), MOVE_UP);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DOWN"), MOVE_DOWN);
    getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke("RIGHT"), MOVE_DOWN);

    getActionMap().put(MOVE_UP, new MoveAction(-1));
    getActionMap().put(MOVE_DOWN, new MoveAction(1));
  }

  @Override
  protected void paintComponent(Graphics g) {
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
    if (rackSelection != -1 && isFocusOwner()) {
      int tileY = RACK_TILE_SPACING + rackSelection * (BoardComp.SQUARE_SIZE + RACK_TILE_SPACING);
      Tile.drawSelectionBox((Graphics2D) g, RACK_TILE_SPACING, tileY);
    }
  }

  private class MoveAction extends AbstractAction {
    private final int x;

    MoveAction(int x) {
      this.x = x;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      rackSelection = Math.floorMod(rackSelection + x, Board.RACK_CAPACITY);
      repaint();
    }
  }

  private class SelectTileMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      requestFocus(); // Get focus for typing
      rackSelection =
          (e.getY() + RACK_TILE_SPACING / 2) / (BoardComp.SQUARE_SIZE + RACK_TILE_SPACING);

      getParent().repaint(); // Repaint the rack to clear selection box
    }
  }

  private class LetterKeyListener extends KeyAdapter {
    @Override
    public void keyTyped(KeyEvent e) {
      char keyChar = e.getKeyChar();
      if (Character.isAlphabetic(keyChar)) {
        board.placeInRack(keyChar, rackSelection);
      } else if (keyChar == KeyEvent.VK_BACK_SPACE) {
        board.placeInRack(' ', rackSelection);
      } else if (keyChar == ' ' || keyChar == '-') {
        board.placeInRack('_', rackSelection);
      }
      repaint();
    }
  }
}
