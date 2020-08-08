import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Interface extends JComponent implements MouseListener, MouseMotionListener{
    boolean foobar = true;
    int x, y;
    public static int[] selected = {-1,-1};
    public static int[][] wordSelect = {{-1,-1},{-1,-1}};
    public static int handSelect = -1;
    public static int wordSelectN = -1;
    public static int selectionMode = 0;
    public static String message = "";
    
    @Override
    public void paintComponent(Graphics g){
        if (foobar) {
            addMouseListener(this);
            addMouseMotionListener(this);
            foobar = false;
        }
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        g.setColor(Color.decode("#C8C2A8"));
        g.fillRect(0,0,672,672);
        
        Image tripleWord = new ImageIcon("Images/tripleword.png").getImage();
        Image doubleWord = new ImageIcon("Images/doubleword.png").getImage();
        Image tripleLetter = new ImageIcon("Images/tripleletter.png").getImage();
        Image doubleLetter = new ImageIcon("Images/doubleletter.png").getImage();
        Image start = new ImageIcon("Images/start.png").getImage();
        Image tile = new ImageIcon("Images/tile.png").getImage();
        Image tileBackground = new ImageIcon("Images/tileBackground.png").getImage();
        g.drawImage(tileBackground,690,16,this);
        
        Font large = new Font("Arial", Font.PLAIN, 30);
        Font medium = new Font("Arial", Font.PLAIN, 20);
        Font small = new Font("Arial", Font.PLAIN, 12);
        g2.setFont(medium);
        g2.setColor(Color.black);
        g2.drawString(message,15,710);
        g2.setFont(small);
        for(int i = 0; i < 15; i++){
            g2.drawString((char)(i+97)+"", 25+i*44, 682);
            g2.drawString((i+1)+"", 673, (15-i)*44-11);
        }
        g2.setFont(large);
        
        //draw the lines of the board
        g.setColor(Color.decode("#ECE8D9"));
        for(int i = 0; i < 16; i++){
            g.fillRect(i*44+4, 4, 4, 664);
            g.fillRect(4, i*44+4, 664, 4);
        }
        g.setColor(Color.black);
        g.fillRect(0, 0, 4, 668);
        g.fillRect(15*44+8, 0, 4, 672);
        g.fillRect(0, 0, 668, 4);
        g.fillRect(0, 15*44+8, 672, 4);
        
        //draw the images of the board
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < 15; j++){
                if(i == 7 && j == 7){
                    g.drawImage(start, j*44+4, i*44+4, this);
                }else{
                    switch(Rate.bonuses[j][i]){
                        case 'W': g.drawImage(tripleWord, j*44+4, i*44+4, this); break;
                        case 'w': g.drawImage(doubleWord, j*44+4, i*44+4, this); break;
                        case 'L': g.drawImage(tripleLetter, j*44+4, i*44+4, this); break;
                        case 'l': g.drawImage(doubleLetter, j*44+4, i*44+4, this); break;
                    }
                }
            }
        }
        
        //Draw the tiles
        for(int i = 0; i < 15; i++){
            for(int j = 0; j < 15; j++){
                if(Scrabble.board[j][i] != ' '){
                    g.drawImage(tile, i*44+8, j*44+8, this);
                    g2.setFont(large);
                    if(Character.isUpperCase(Scrabble.board[j][i])){
                        g2.drawString(Scrabble.board[j][i]+"", i*44+12, j*44+40);
                    }else if(Character.isLowerCase(Scrabble.board[j][i])){
                        g2.setFont(small);
                        g2.drawString("("+Scrabble.board[j][i]+")", i*44+18, j*44+32);
                        g2.drawString("0", i*44+37, j*44+42);
                        
                    }
                    g2.setFont(small);
                    //<editor-fold defaultstate="collapsed" desc="letter value switch">
                    int nx = 37;
                    if(Scrabble.board[j][i] == 'Q' || Scrabble.board[j][i] == 'Z'){
                        nx = 33;
                    }
                    int ny = 42;
                    switch(Scrabble.board[j][i]){
                        case 'A': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'B': g2.drawString("3", i*44+nx, j*44+ny);
                        break;
                        case 'C': g2.drawString("3", i*44+nx, j*44+ny);
                        break;
                        case 'D': g2.drawString("2", i*44+nx, j*44+ny);
                        break;
                        case 'E': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'F': g2.drawString("4", i*44+nx, j*44+ny);
                        break;
                        case 'G': g2.drawString("2", i*44+nx, j*44+ny);
                        break;
                        case 'H': g2.drawString("4", i*44+nx, j*44+ny);
                        break;
                        case 'I': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'J': g2.drawString("8", i*44+nx, j*44+ny);
                        break;
                        case 'K': g2.drawString("5", i*44+nx, j*44+ny);
                        break;
                        case 'L': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'M': g2.drawString("3", i*44+nx, j*44+ny);
                        break;
                        case 'N': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'O': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'P': g2.drawString("3", i*44+nx, j*44+ny);
                        break;
                        case 'Q': g2.drawString("10", i*44+nx, j*44+ny);
                        break;
                        case 'R': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'S': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'T': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'U': g2.drawString("1", i*44+nx, j*44+ny);
                        break;
                        case 'V': g2.drawString("4", i*44+nx, j*44+ny);
                        break;
                        case 'W': g2.drawString("4", i*44+nx, j*44+ny);
                        break;
                        case 'X': g2.drawString("8", i*44+nx, j*44+ny);
                        break;
                        case 'Y': g2.drawString("4", i*44+nx, j*44+ny);
                        break;
                        case 'Z': g2.drawString("10", i*44+nx, j*44+ny);
                        break;
                        default:
                    }
//</editor-fold>
                }
            }
        }
        
        //Draw the hand
        int sp = 6;
        int so = 0;
        for(int i = 0; i < Scrabble.hand.length; i++){
            if (!"".equals(Scrabble.hand[i])) {
                try {
                    if ("_".equals(Scrabble.hand[i]) || Character.isAlphabetic(Scrabble.hand[i].charAt(0))) {
                        g.drawImage(tile, 700 - sp, 20 + i * 44 - so, this);
                        g2.setFont(large);
                        if (Character.isUpperCase(Scrabble.hand[i].charAt(0))) {
                            g2.drawString(Scrabble.hand[i] + "", 704 - sp, i * 44 + 54 - so);
                        }
                        g2.setFont(small);
                        //<editor-fold defaultstate="collapsed" desc="letter value switch">
                        int nx = 29 - sp;
                        if ("Q".equals(Scrabble.hand[i]) || "Z".equals(Scrabble.hand[i])) {
                            nx = 25 - sp;
                        }
                        int ny = 42 + 12 - so;
                        switch (Scrabble.hand[i].charAt(0)) {
                            case 'A':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'B':
                                g2.drawString("3", 700 + nx, i * 44 + ny);
                                break;
                            case 'C':
                                g2.drawString("3", 700 + nx, i * 44 + ny);
                                break;
                            case 'D':
                                g2.drawString("2", 700 + nx, i * 44 + ny);
                                break;
                            case 'E':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'F':
                                g2.drawString("4", 700 + nx, i * 44 + ny);
                                break;
                            case 'G':
                                g2.drawString("2", 700 + nx, i * 44 + ny);
                                break;
                            case 'H':
                                g2.drawString("4", 700 + nx, i * 44 + ny);
                                break;
                            case 'I':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'J':
                                g2.drawString("8", 700 + nx, i * 44 + ny);
                                break;
                            case 'K':
                                g2.drawString("5", 700 + nx, i * 44 + ny);
                                break;
                            case 'L':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'M':
                                g2.drawString("3", 700 + nx, i * 44 + ny);
                                break;
                            case 'N':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'O':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'P':
                                g2.drawString("3", 700 + nx, i * 44 + ny);
                                break;
                            case 'Q':
                                g2.drawString("10", 700 + nx, i * 44 + ny);
                                break;
                            case 'R':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'S':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'T':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'U':
                                g2.drawString("1", 700 + nx, i * 44 + ny);
                                break;
                            case 'V':
                                g2.drawString("4", 700 + nx, i * 44 + ny);
                                break;
                            case 'W':
                                g2.drawString("4", 700 + nx, i * 44 + ny);
                                break;
                            case 'X':
                                g2.drawString("8", 700 + nx, i * 44 + ny);
                                break;
                            case 'Y':
                                g2.drawString("4", 700 + nx, i * 44 + ny);
                                break;
                            case 'Z':
                                g2.drawString("10", 700 + nx, i * 44 + ny);
                                break;
                            default:
                        }
//</editor-fold>
                    }
                } catch (Exception e) {}
            }
        }
        
        //Draw the selection
        if (selected[0] != -1) {
            g.setColor(Color.red);
            g.fillRect(selected[0] * 44 + 6, selected[1] * 44 + 6, 4, 44);
            g.fillRect(selected[0] * 44 + 46, selected[1] * 44 + 6, 4, 44);
            g.fillRect(selected[0] * 44 + 6, selected[1] * 44 + 6, 44, 4);
            g.fillRect(selected[0] * 44 + 6, selected[1] * 44 + 46, 44, 4);
        }
        if (handSelect != -1) {
            g.setColor(Color.red);
            g.fillRect(698-sp, 18 + handSelect * 44, 4, 44);
            g.fillRect(698 + 40-sp, 18 + handSelect * 44, 4, 44);
            g.fillRect(698-sp, 18 + handSelect * 44, 44, 4);
            g.fillRect(698-sp, 58 + handSelect * 44, 44, 4);
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        int sp = 6;
        selected[0] = (x-6)/44;
        selected[1] = (y-6)/44;
        if(x > 665 || y > 665){
            selected[0] = -1;
            if(x > 698-sp && x < 742-sp && y > 18 && y < 326){
                handSelect = (y-18)/44;
                selected[0] = -1;
                selected[1] = -1;
            }else{
                handSelect = -1;
            }
        }else{
            handSelect = -1;
        }
        if (e.getButton() == 3 && selected[0] != -1) {
            if (Character.isUpperCase(Scrabble.board[selected[1]][selected[0]])) {
                Scrabble.board[selected[1]][selected[0]] = Character.toLowerCase(Scrabble.board[selected[1]][selected[0]]);
            } else {
                Scrabble.board[selected[1]][selected[0]] = Character.toUpperCase(Scrabble.board[selected[1]][selected[0]]);
            }
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        x = e.getX();
        y = e.getY();
//        repaint();
    }
    
}