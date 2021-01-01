package autoscrabble;

import java.util.Arrays;

public class WordFinder {
  private char[][] characters;
  private String[] dictionary;

  public WordFinder(String[] dictionary) {
    loadDictionary(dictionary);
  }

  public void loadDictionary(String[] dict) {
    this.dictionary = dict;
    // Separating word into characters
    characters = new char[dict.length][1];
    for (int i = 0; i < dict.length; i++) {
      int l = dict[i].length();
      characters[i] = new char[l];
      for (int j = 0; j < l; j++) {
        characters[i][j] = dict[i].charAt(j);
      }
    }
  }

  public boolean isWord(String s) {
    // The binary search is >= 0 if the key is found
    return Arrays.binarySearch(dictionary, s.toUpperCase()) >= 0;
  }

  public String[][] getWords(char[] line, char[] h) {
    boolean rackContainsBlank = String.valueOf(h).chars().anyMatch(c -> c == '_');

    // Counting max possible length of words, possible future sorting???

    String[][] validList1 = new String[dictionary.length][2];
    int validList1n = 0;
    int n = 0;
    for (int i = 0; i < dictionary.length; i++) { // Getting words that contain the specification
      for (int j = 0; j < line.length - dictionary[i].length() + 1; j++) {
        boolean validWord = true;
        int m = 0;
        for (int k = j; k < dictionary[i].length() + j; k++) {
          if (line[k] != ' ') {
            m += 1;
          }
          if (!rackContainsBlank) {
            if ((line[k] != characters[i][k - j])) {
              if (line[k] != ' ') {
                validWord = false;
              }
            }
          } else {
            if ((Character.toUpperCase(line[k]) != characters[i][k - j])) {
              if (line[k] != ' ') {
                validWord = false;
              }
            }
          }

          if (!validWord) {
            break;
          }
        }
        if (validWord && m != 0) {
          validList1[n][0] = dictionary[i];
          validList1[n][1] = j + "";
          validList1n++;
          n++;
        }
      }
    }
    int[] handCount = new int[27];

    for (char c : h) {
      if (Character.isAlphabetic(c)) {
        handCount[Character.getNumericValue(c) - 10]++;
      }
    }
    String[][] validList2temp = new String[validList1n][2];
    if (rackContainsBlank) validList2temp = new String[validList1n * 26][2];
    int validList2n = 0;

  if (!rackContainsBlank) { // Normal mode
      for (int i = 0; i < validList1n; i++) {
        int[] charCount = new int[26];
        int k = 0;
        int lettersPlaced = 0;
        for (int j = Integer.parseInt(validList1[i][1]);
            j < validList1[i][0].length() + Integer.parseInt(validList1[i][1]);
            j++) {
          if (line[j] == ' ') {
            lettersPlaced++;
            charCount[Character.getNumericValue(validList1[i][0].charAt(k)) - 10]++;
          }
          k++;
        }

        boolean valid = true;
        for (int j = 0; j < 26; j++) {
          if (lettersPlaced == 0) {
            valid = false;
          }
          if (charCount[j] > handCount[j]) {
            valid = false;
          }
        }
        if (valid) {
          validList2temp[validList2n] = validList1[i];
          validList2n++;
        }
      }
    } else { // With blank(s) in hand
      for (int i = 0; i < validList1n; i++) {
        int[] charCount = new int[26];
        int k = 0;
        int lettersPlaced = 0;
        for (int j = Integer.parseInt(validList1[i][1]);
            j < validList1[i][0].length() + Integer.parseInt(validList1[i][1]);
            j++) {
          if (line[j] == ' ') {
            lettersPlaced++;
            charCount[Character.getNumericValue(validList1[i][0].charAt(k)) - 10]++;
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
              if (j == m) {
                if (j - 1 != -1) {
                  if ((charCount[j] > handCount[j] - 1)) {
                    blankUsed = true;
                  }
                }
              }
            } else {
              valid = false;
            }
          }

          if (valid) {
            validList2temp[validList2n] = validList1[i];
            if (blankUsed) {
              for (int o = 0;
                  o < validList2temp[validList2n][0].length();
                  o++) { // Make the first blank letter lower case (not 100% ideal)
                if (Character.getNumericValue(validList2temp[validList2n][0].charAt(o)) - 10 == m) {
                  if (' ' == line[Integer.parseInt(validList2temp[validList2n][1]) + o]) {
                    validList2temp[validList2n][0] =
                        validList2temp[validList2n][0].substring(0, o)
                            + Character.toLowerCase(validList2temp[validList2n][0].charAt(o))
                            + validList2temp[validList2n][0].substring(o + 1);
                    break;
                  }
                }
              }
            }
            validList2n++;
          }
          handCount[m]--;
        }
      }
    }

    String[][] validList2 = new String[validList2n][2];
    System.arraycopy(validList2temp, 0, validList2, 0, validList2n);
    String[][] validList3temp = new String[validList2n][2];
    int validList3n = 0;
    for (int i = 0; i < validList2n; i++) {
      boolean valid = true;

      var i2 = Integer.parseInt(validList2[i][1]) - 1;
      if (i2 > 0 && ' ' != line[i2]) {
        valid = false;
      }

      var i1 = Integer.parseInt(validList2[i][1]) + validList2[i][0].length();
      if (i1 < line.length && ' ' != line[i1]) {
        valid = false;
      }

      if (valid) {
        validList3temp[validList3n] = validList2[i];
        validList3n++;
      }
    }
    String[][] validList3 = new String[validList3n][2];
    System.arraycopy(validList3temp, 0, validList3, 0, validList3n);
    return validList3;
  }
}
