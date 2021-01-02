package autoscrabble;

import autoscrabble.userinterface.BoardComp;
import autoscrabble.userinterface.RackComp;

import javax.swing.*;
import java.awt.*;

public class App {
  public static final Color BACKGROUND_COLOUR = Color.decode("#F3F3F3");

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    var board = new Board();
    var rack = new RackComp(board);
    frame.add(new BoardComp(board, rack));
    frame.add(rack);
    frame.setLayout(new FlowLayout());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setTitle("AutoScrabble");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    frame.getContentPane().setBackground(BACKGROUND_COLOUR);
  }
}
