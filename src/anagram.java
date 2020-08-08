import java.util.*;


public class anagram {
    public static int[][] letterCount;
    public static String[][] characters;
    public static String[] dict;
    public static int[] dictLength;
    public static double one = 0;
    public static double two = 0;
    public static double three = 0;
    public static double four = 0;
    
    public static void loadD(String[] d){
        dictLength = new int[d.length];
        dict = Scrabble.dict;
        letterCount = new int[d.length][26];
        for(int i = 0; i < d.length; i++){//counting the number of each letter
            int l = d[i].length();
            for(int j = 0; j < l; j++){
                letterCount[i][Character.getNumericValue(d[i].charAt(j))-10]++;
            }
            dictLength[i] = d[i].length();
        }
        
        //Seperating word into characters
        characters = new String[d.length][1];
        for(int i = 0; i < d.length;i++){
            int l = d[i].length();
            characters[i] = new String[l];
            for(int j = 0; j < l; j++){
                characters[i][j] = d[i].charAt(j)+"";
            }
        }
    }
    
    public static boolean isWord(String s){
        for(int i = 0; i < dict.length; i++){
            if(s.toUpperCase().equals(dict[i])){
                return true;
            }
        }
        return false;
    }
    
    public static String sortString(String str){
        // put the characters into an array
        Character[] chars = new Character[str.length()];
        for (int i = 0; i < chars.length; i++)
            chars[i] = str.charAt(i);

        // sort the array
        Arrays.sort(chars, (Character c1, Character c2) -> {
            int cmp = Character.compare(
                    Character.toLowerCase(c1),
                    Character.toLowerCase(c2)
            );
            if (cmp != 0) return cmp;
            return Character.compare(c1, c2);
        });

        // rebuild the string
        StringBuilder sb = new StringBuilder(chars.length);
        for (char c : chars) sb.append(c);
        str = sb.toString();
        return str;
    }
    
    public static String[][] getWords(String[] l, String[] h, int mode){
        //Counting max possible length of words, possible future sorting???
        double lol = System.nanoTime();
        int maxLength = 0;
        for(String i : h){
            if(!"".equals(i)){
                maxLength++;
            }
        }
        for(String i : l){
            if(!"".equals(i)){
                maxLength++;
            }
        }
        
        String[][] validList1 = new String[dict.length][2];
        int validList1n = 0;
        int n = 0;
                                        double t1 = System.nanoTime();  //Section 1
        for(int i = 0; i < dict.length; i++){//Getting words that contain the specification
            for(int j = 0; j < l.length-dict[i].length()+1; j++){
                boolean validWord = true;
                int m = 0;
                for(int k = j; k < dict[i].length()+j; k++){
                    if(!l[k].equals("")){
                        m += 1;
                    }
                    if (mode == 0) {
                        if ((!l[k].equals(characters[i][k - j]))) {
                            if (!l[k].equals("")) {
                                validWord = false;
                            }
                        }
                    }else{
                        if ((!l[k].toUpperCase().equals(characters[i][k - j]))) {
                            if (!l[k].equals("")) {
                                validWord = false;
                            }
                        }
                    }
                    
                    if(!validWord){
                        k = 9000001;
                    }
                }
                if(validWord && m != 0){
                    validList1[n][0] = dict[i];
                    validList1[n][1] = j+"";
                    validList1n++;
                    n++;
                }
            }
        }
                                one += (System.nanoTime()-t1)/1000000;
                                double t2 = System.nanoTime();  //Section 2
        int[] handCount = new int[27];

        for(int i = 0; i < h.length; i++){
            try {
                handCount[Character.getNumericValue(h[i].charAt(0))-10]++;
            } catch (Exception e) {}
        }
                                two += (System.nanoTime()-t2)/1000000;
                                double t3 = System.nanoTime(); //Section 3
        String[][] validList2temp = new String[validList1n][2];
        if(mode == 2) validList2temp = new String[validList1n*26][2];
        int validList2n = 0;
        
        if(mode != 2){//Normal mode
            //<editor-fold defaultstate="collapsed" desc="Filtering out words that do not contain letters in the hand">
            for(int i = 0; i < validList1n; i++){
                int[] charCount = new int[26];
                int k = 0;
                int lettersPlaced = 0;
                for(int j = Integer.parseInt(validList1[i][1]); j < validList1[i][0].length()+Integer.parseInt(validList1[i][1]);j++){
                    if(l[j].equals("")){
                        lettersPlaced++;
                        charCount[Character.getNumericValue(validList1[i][0].charAt(k))-10]++;
                    }
                    k++;
                }

                boolean valid = true;
                for(int j = 0; j < 26; j++){
                    if(lettersPlaced == 0){
                        valid = false;
                    }
                    if(charCount[j] > handCount[j]){
                        valid = false;
                    }
                }
                if(valid){
                    validList2temp[validList2n] = validList1[i];
                    validList2n++;
                }
            }
//</editor-fold>
        }else{//With blank(s) in hand
            //<editor-fold defaultstate="collapsed" desc="Filtering out words that do not contain letters in the hand">
            for(int i = 0; i < validList1n; i++){
                int[] charCount = new int[26];
                int k = 0;
                int lettersPlaced = 0;
                for(int j = Integer.parseInt(validList1[i][1]); j < validList1[i][0].length()+Integer.parseInt(validList1[i][1]);j++){
                    if(l[j].equals("")){
                        lettersPlaced++;
                        charCount[Character.getNumericValue(validList1[i][0].charAt(k))-10]++;
                    }
                    k++;
                }
                for (int m = 0; m < 26; m++) {
                    handCount[m]++;
                    boolean valid = true;
                    boolean blankUsed = false;
                    for (int j = 0; j < 26; j++) {
                        if (lettersPlaced == 0) {
                            valid = false;
                        }
                        if (charCount[j] <= handCount[j]) {
                            if(j == m){
                                if(j-1 != -1){
                                    if((charCount[j] > handCount[j]-1)){
                                        blankUsed = true;
                                    }
                                }
                            }
                        }else{
                            valid = false;
                        }
                    }
                    
                    if (valid) {
                        validList2temp[validList2n] = validList1[i];
                        if (blankUsed) {
                            for (int o = 0; o < validList2temp[validList2n][0].length(); o++) { //Make the first blank letter lower case (not 100% ideal)
                                try {
                                    if (Character.getNumericValue(validList2temp[validList2n][0].charAt(o)) - 10 == m) {
                                        if ("".equals(l[Integer.parseInt(validList2temp[validList2n][1])+o])) {
                                            validList2temp[validList2n][0] = validList2temp[validList2n][0].substring(0, o) + Character.toLowerCase(validList2temp[validList2n][0].charAt(o)) + validList2temp[validList2n][0].substring(o + 1);
                                            o = 90000001;
                                        }
                                    }
                                } catch (Exception e) {}
                            }
                        }
                        validList2n++;
                    }
                    handCount[m]--;
                }
            }
//</editor-fold>
        }
        
        String[][] validList2 = new String[validList2n][2];
        System.arraycopy(validList2temp, 0, validList2, 0, validList2n);
                                three += (System.nanoTime()-t3)/1000000;
                                double t4 = System.nanoTime();  //Section 4
        String[][] validList3temp = new String[validList2n][2];
        int validList3n = 0;
        //<editor-fold defaultstate="collapsed" desc="Removing words that conflict with nearby letters">
        for(int i = 0; i < validList2n; i++){
            boolean valid = true;
            
            try{
                if(!"".equals(l[Integer.parseInt(validList2[i][1])-1])){
                    valid = false;
                }
            }catch(Exception e){}
            
            try{
                if(!"".equals(l[Integer.parseInt(validList2[i][1])+validList2[i][0].length()])){
                    valid = false;
                }
            }catch(Exception e){}
            
            if(valid){
                validList3temp[validList3n] = validList2[i];
                validList3n++;
            }
        }
//</editor-fold>
                                four += (System.nanoTime()-t4)/1000000;
        String[][] validList3 = new String[validList3n][2];
        System.arraycopy(validList3temp, 0, validList3, 0, validList3n);
        return validList3;
    }
    
    public static String[] firstTurn(){
        return null;
    }
    
}
