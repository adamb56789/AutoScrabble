import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;


public class Scrabble extends JFrame implements KeyListener{
    //<editor-fold defaultstate="collapsed" desc="Declaring variables">
    private final Interface draw;
    public static boolean foobar;
    public static boolean giveUp;
    public static boolean useBlank = true;
    public static String[] dict;
//    public static String[] hand = {"A", "B", "C", "D", "E", "F", "G", "H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","A", "B", "C", "D", "E", "F", "G", "H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","A", "B", "C", "D", "E", "F", "G", "H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","A", "B", "C", "D", "E", "F", "G", "H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
    public static String[] hand = {"A", "B", "C", "D", "E", "F", "G"};
    public static String[] handT = new String[26*4];
    public static boolean[][] rated;
    public static char[][] board = {
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
        {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '}
    };
//</editor-fold>
    
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            Scrabble frame = new Scrabble();
            frame.setTitle("Scrabble Perfect");
            frame.setResizable(false);
            frame.setMinimumSize(new Dimension(750, 750));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(frame.draw);
            frame.pack();
            frame.setLocationRelativeTo(frame);
            frame.setVisible(true);
            frame.getContentPane().setBackground(Color.decode("#F3F3F3"));
        });
        
        int lines = 0;
        try {
            lines = countLines("Dictionary.txt")+1;
            System.out.println("There are " + lines + " words in the dictionary.");
        } catch (IOException ex) {}
        dict = new String[lines];
            try{
            FileReader fR = new FileReader("Dictionary.txt");
            try (BufferedReader bR = new BufferedReader(fR)) {
                for(int i=0;i<lines;i++){
                    dict[i]=bR.readLine();
                }
            }
        }catch(Exception e){}
        
        anagram.loadD(dict);
            
        rated = new boolean[15][15];
    }
    
    @SuppressWarnings({"LeakingThisInConstructor", "CallToThreadStartDuringObjectConstruction"})
    public Scrabble(){
        this.draw=new Interface();
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        Thread animationThread = new Thread(() -> {
            while (true) {
                repaint();
                try {Thread.sleep(30);} catch (Exception ex) {}
                if(foobar){
                    foobar = false;
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        animationThread.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if(Interface.selected[0] != -1){
            if(Character.isAlphabetic(e.getKeyChar())){
                board[Interface.selected[1]][Interface.selected[0]] = Character.toUpperCase(e.getKeyChar());
                rated[Interface.selected[1]][Interface.selected[0]] = true;
            }
            if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE){
                board[Interface.selected[1]][Interface.selected[0]] = ' ';
                rated[Interface.selected[1]][Interface.selected[0]] = false;
            }
        }
        if(Interface.handSelect != -1){
            if(Character.isAlphabetic(e.getKeyChar())) hand[Interface.handSelect] = (e.getKeyChar()+"").toUpperCase();
            if(e.getKeyChar() == KeyEvent.VK_BACK_SPACE){
                hand[Interface.handSelect] = "";
            }
            if(e.getKeyChar() == '-'){
                hand[Interface.handSelect] = "_";
            }
        }
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getExtendedKeyCode() == KeyEvent.VK_ENTER){
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            Thread2 thread = new Thread2();
        }
        if(e.getExtendedKeyCode() == KeyEvent.VK_SHIFT){
            useBlank = false;
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            Thread2 thread = new Thread2();
        }
        if (Interface.handSelect == -1) {
            if (e.getExtendedKeyCode() == KeyEvent.VK_LEFT && Interface.selected[0] > 0) {
                Interface.selected[0]--;
            } else if (e.getExtendedKeyCode() == KeyEvent.VK_RIGHT && Interface.selected[0] < 14) {
                Interface.selected[0]++;
            } else if (e.getExtendedKeyCode() == KeyEvent.VK_UP && Interface.selected[1] > 0) {
                Interface.selected[1]--;
            } else if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN && Interface.selected[1] < 14) {
                Interface.selected[1]++;
            }
        }else{
            if (e.getExtendedKeyCode() == KeyEvent.VK_UP && Interface.handSelect > 0) {
                Interface.handSelect--;
            } else if (e.getExtendedKeyCode() == KeyEvent.VK_DOWN && Interface.handSelect < 6) {
                Interface.handSelect++;
            }
        }
        if(e.getExtendedKeyCode() == KeyEvent.VK_ESCAPE) giveUp = true;
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    
    public static int countLines(String filename) throws IOException {
    try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
        byte[] c = new byte[1024];
        int count = 0;
        int readChars;
        boolean empty = true;
        while ((readChars = is.read(c)) != -1) {
            empty = false;
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n') {
                    ++count;
                }
            }
        }
        return (count == 0 && !empty) ? 1 : count;
    }
}
    
    public static boolean validBoard(String[] move){
        char[][] boardT = new char[15][15];
        
        for(int i=0; i<board.length; i++){
            System.arraycopy(board[i], 0, boardT[i], 0, board[i].length);
        }
        
        if (!"check".equals(move[0])) {
            for (int i = 0; i < move[0].length(); i++) {
                if ("h".equals(move[3])) {
                    boardT[Integer.parseInt(move[2])][Integer.parseInt(move[1]) + i] = move[0].charAt(i);
                } else {
                    boardT[Integer.parseInt(move[2]) + i][Integer.parseInt(move[1])] = move[0].charAt(i);
                }
            }
        }
        
        char[][] lines = new char[30][15];
        
        //Copy rows
        System.arraycopy(boardT, 0, lines, 0, 15);
        //Copy columns
        for(int i = 15; i < 30; i++){ 
            for(int j = 0; j < 15; j++){
                lines[i][j] = boardT[j][i-15];
            }
        }

        for(int i = 0; i < 30; i++){
            for(int j = 0; j < 15; j++){
                try {
                    if (Character.isAlphabetic(lines[i][j]) && Character.isAlphabetic(lines[i][j + 1])) {
                        String word = lines[i][j] + "";
                        int k = 1;
                        
                        if (!Character.isAlphabetic(lines[i][j + k])) {
                            word = "";
                        }
                        
                        int f = 0;
                        
                        try {
                            while (Character.isAlphabetic(lines[i][j + k])) {
                                word += lines[i][j + k];
                                k++;
                                f++;
                            }
                        } catch (Exception e) {}
                        j += f;
                        
                        if (!anagram.isWord(word)) {
                            return false;
                        }
                        
                    }
                } catch (Exception e) {
                }
            }
        }
        
        return true;
    }
    
    public static String[][] joinArray(String[][] array1, String[][] array2, int l) {
        String[][] array3 = new String[array1.length+array2.length][l];
        System.arraycopy(array1, 0, array3, 0, array1.length);
        System.arraycopy(array2, 0, array3, array1.length, array2.length);
        return array3;
    }
    
    public static String[][] findWord(int[][] s, String[] Hand){
        int gapL = Math.abs(s[0][0]-s[1][0])+Math.abs(s[0][1]-s[1][1])+1;
        String[] location = new String[gapL];
        if(s[0][1]-s[1][1] == 0){//Horizontal
            for(int i = 0; i < gapL; i++){
                location[i] = board[s[0][1]][s[0][0]+i]+"";
                if(board[s[0][1]][s[0][0]+i] == ' '){
                    location[i] = "";
                }
            }
        }else{//Vertical
            for(int i = 0; i < gapL; i++){
                location[i] = board[s[0][1]+i][s[0][0]]+"";
                if(board[s[0][1]+i][s[0][0]] == ' '){
                    location[i] = "";
                }
            }
        }
        //If line is blank then skip
        boolean isBlank = true;
        for (int i = 0; i < 15; i++) {
            if (!"".equals(location[i])) {
                isBlank = false;
            }
        }
        if(isBlank){
            String[][] re = {};
            return re;
        }
        //Calculate mode - normal (0), blanks on the board (1), blanks in the hand (2)
        int mode = 0;
        for(int i = 0; i < location.length; i++){
            try {
                if (Character.isLowerCase(location[i].charAt(0))) {
                    mode = 1;
                }
            } catch (Exception e) {}
        }
        for(int i = 0; i < location.length; i++){
            try {
                if ("_".equals(hand[i])) {
                    mode = 2;
                }
            } catch (Exception e) {}
        }
        String[][] validWords = anagram.getWords(location, Hand, mode);
        String[][] wordScores = new String[validWords.length][5];
        for(int i = 0; i < validWords.length; i++){
            
            String[][] word = new String[validWords[i][0].length()][5];
            for(int j = 0; j < validWords[i][0].length(); j++){

                word[j][0] = validWords[i][0].charAt(j)+"";
                if(s[0][1]-s[1][1] == 0){//Horizontal
                    word[j][1] = s[0][1]+"";
                    int temp = s[0][0]+j+Integer.parseInt(validWords[i][1]);
                    word[j][2] = temp+"";
                }else{//Vertical
                    int temp = s[0][1]+j+Integer.parseInt(validWords[i][1]);
                    word[j][1] = temp+"";
                    word[j][2] = s[0][0]+"";
                }

                if(!"".equals(location[Integer.parseInt(validWords[i][1])+j])){
                    word[j][3] = "n";
                }

                try {
                    if (Character.isLowerCase(location[Integer.parseInt(validWords[i][1]) + j].charAt(0))) {
                        word[j][4] = "y";
                    }
                } catch (Exception e) {}
            }
            wordScores[i][0] = validWords[i][0];


            if(s[0][1]-s[1][1] == 0){//Horizontal
                wordScores[i][1] = (s[0][0]+validWords[i][1])+"";
                wordScores[i][2] = s[0][1]+"";
                wordScores[i][3] = "h";
            }else{//Vertical
                wordScores[i][1] = s[0][0]+"";
                wordScores[i][2] = (s[0][1]+validWords[i][1])+"";
                wordScores[i][3] = "v";
            }
            wordScores[i][4] = Rate.rate(wordScores[i])+"";
//            wordScores[i][4] = Rate.rateWord(word,location)+"";
        }
        return wordScores;
    }
    
    public static void onEnter(){
        String[] arr = {"check"};
        if(!validBoard(arr)){
            Interface.message = "Current board not valid";
            foobar = true;
            return;
        }
        System.arraycopy(hand, 0, handT, 0, hand.length);
        if(!useBlank){
            useBlank = true;
            for(int i = 0; i < hand.length; i++){
                if("_".equals(hand[i])){
                    hand[i] = "";
                    i = 9000001;
                }
            }
        }
        Interface.message = "Searching for words...";
        Interface.handSelect = -1;
        double timer1 = System.currentTimeMillis();
        
        int blankL = -1;
        for(int i = 0; i < hand.length; i++){ //Getting blank location
            if(hand[i].equals("_")){
                if(blankL != -1){
                    Interface.message = "Only one blank allowed";
                    foobar = true;
                    return;
                }
                blankL = i;
            }
        }
        
        String[][] validWord1 = {};
        if(blankL == -1){ //No blank
            for(int i = 0; i < 15; i++){
                int[][] select1 = {{i,0},{i,14}};
                validWord1 = joinArray(validWord1,findWord(select1,hand),5);
                int[][] select2 = {{0,i},{14,i}};
                validWord1 = joinArray(validWord1,findWord(select2,hand),5);
            }
        }else{ //blank
            for(int i = 0; i < 15; i++){
                int[][] select1 = {{i,0},{i,14}};
                validWord1 = joinArray(validWord1,findWord(select1,hand),5);
                int[][] select2 = {{0,i},{14,i}};
                validWord1 = joinArray(validWord1,findWord(select2,hand),5);
            }
        }
        if(validWord1.length == 0){
            Interface.message = "No words found";
            foobar = true;
            System.arraycopy(handT, 0, hand, 0, hand.length);
            return;
        }else{
            Arrays.sort(validWord1, (String[] a, String[] b) -> Double.compare(Double.parseDouble(b[4]), Double.parseDouble(a[4])));
            for (int i = 0; i < validWord1.length; i++) {
                Interface.message = "Checking word " + validWord1[i][0] + " which would score " + validWord1[i][4] + " (press ESC to cancel)";
                if(validBoard(validWord1[i])){
                    Interface.message = "Found ";
                    Interface.message += validWord1[i][0] + " at (" + ((char)(Integer.parseInt(validWord1[i][1])+97)) + (15-Integer.parseInt(validWord1[i][2])) + ")";
                    if("h".equals(validWord1[i][3])){
                        Interface.message += " horizontally";
                    }else{
                        Interface.message += " vertically";
                    }
                    Interface.message += ", scoring at least " + validWord1[i][4] + ", ";
                    i = 9000001;
                }
                if(giveUp){
                    giveUp = false;
                    Interface.message = "Cancelled ";
                    i = 9000001;
                }
            }
        }
        
        double timer2 = System.currentTimeMillis();
        System.out.println("1 took " + anagram.one);
        System.out.println("2 took " + anagram.two);
        System.out.println("3 took " + anagram.three);
        System.out.println("4 took " + anagram.four);
        System.out.println("Total is " + (anagram.one + anagram.two + anagram.three + anagram.four));
        anagram.one = 0; anagram.two = 0; anagram.three = 0; anagram.four = 0;
        System.out.println("Took " + (timer2 - timer1) + " ms");
        if(Interface.message.equals("Cancelled ")){
            Interface.message += "after " + (timer2-timer1) + " ms";
        }else{
            Interface.message += "in " + (timer2-timer1) + " ms";
        }
        System.out.println(Interface.message);
        foobar = true;
        System.arraycopy(handT, 0, hand, 0, hand.length);
    }
}