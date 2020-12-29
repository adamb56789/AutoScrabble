import javax.swing.*;
import java.awt.*;

public class App {
  public static void main(String[] args) {
    var gui = new Gui();
    Board board = new Board(gui);
    javax.swing.SwingUtilities.invokeLater(
        () -> {
          board.setTitle("Scrabble Perfect");
          board.setResizable(false);
          board.setMinimumSize(new Dimension(750, 750));
          board.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          board.getContentPane().add(gui);
          board.pack();
          board.setLocationRelativeTo(board);
          board.setVisible(true);
          board.getContentPane().setBackground(Color.decode("#F3F3F3"));
        });
  }
}
