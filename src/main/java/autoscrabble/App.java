package autoscrabble;

import autoscrabble.userinterface.BoardView;
import autoscrabble.userinterface.RackView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class App {
  public static final Color BACKGROUND_COLOUR = Color.decode("#F3F3F3");
  public static final int PLAYER_COUNT = 4;

  public static void main(String[] args) {
    // Load the dictionary and prepare the word finder
    var inputStream = App.class.getClassLoader().getResourceAsStream("Dictionary.txt");
    assert inputStream != null;
    var streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    var reader = new BufferedReader(streamReader);
    var dictionary = new String[] {};
    try {
      dictionary = reader.lines().toArray(String[]::new);
      for (String line; (line = reader.readLine()) != null; ) {
        System.out.println(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    var wordFinder = new WordFinder(dictionary);
    var board = new Board(wordFinder);

    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    var gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    var boardComp = new BoardView(board);
    panel.add(boardComp, gbc);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    var rackComp = new RackView(board, boardComp);
    panel.add(rackComp, gbc);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    panel.add(buttonPanel, gbc);

    var fastButton = new JButton();
    fastButton.setFocusable(false);
    fastButton.setText("Fast");
    fastButton.addActionListener(boardComp.getFindWordFast());
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.insets = new Insets(1, 0, 1, 0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    buttonPanel.add(fastButton, gbc);

    var resetButton = new JButton();
    resetButton.setFocusable(false);
    resetButton.setText("Reset");
    resetButton.addActionListener(
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            System.out.println("Reset");
            board.initialise();
            boardComp.resetStagedWord();
            rackComp.resetSelection();
            rackComp.requestFocus();
            panel.repaint();
          }
        });
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.insets = new Insets(1, 0, 1, 0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    buttonPanel.add(resetButton, gbc);

    var autoButton = new JButton();
    autoButton.setFocusable(false);
    autoButton.setText("Auto");
    autoButton.addActionListener(new autoModeAction(panel, board));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.insets = new Insets(1, 0, 1, 0);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    buttonPanel.add(autoButton, gbc);

    var outerPanel = new JPanel();
    outerPanel.setLayout(new GridBagLayout());
    gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    outerPanel.add(panel, gbc);

    var scrollPane = new JScrollPane(outerPanel);

    var frame = new JFrame();
    frame.add(scrollPane);
    frame.pack();
    rackComp.requestFocus(); // Start focus on the rack
    frame.setLocationRelativeTo(null);
    frame.setTitle("AutoScrabble");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    frame.getContentPane().setBackground(BACKGROUND_COLOUR);
  }

  private static class autoModeAction extends AbstractAction {
    private final JPanel panel;
    private final Board board;

    public autoModeAction(JPanel panel, Board board) {
      this.panel = panel;
      this.board = board;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      new Thread(() -> board.playAutomatically(panel, 0)).start();
    }
  }
}
