package autoscrabble;

import autoscrabble.word.LineWord;

import java.util.Arrays;
import java.util.List;

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

  public List<LineWord> getWords(char[] line, char[] h) {
    boolean rackContainsBlank = String.valueOf(h).chars().anyMatch(c -> c == '_');

    // Counting max possible length of words, possible future sorting???

    LineWord[] validList1 = new LineWord[dictionary.length];
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
          validList1[n] = new LineWord(dictionary[i], j);
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
    LineWord[] validList2temp = new LineWord[validList1n];
    if (rackContainsBlank) validList2temp = new LineWord[validList1n * 26];
    int validList2n = 0;

  if (!rackContainsBlank) { // Normal mode
      for (int i = 0; i < validList1n; i++) {
        int[] charCount = new int[26];
        int k = 0;
        int lettersPlaced = 0;
        for (int j = validList1[i].getStartIndex();
            j < validList1[i].getWord().length() + validList1[i].getStartIndex();
            j++) {
          if (line[j] == ' ') {
            lettersPlaced++;
            charCount[Character.getNumericValue(validList1[i].getWord().charAt(k)) - 10]++;
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
        for (int j = validList1[i].getStartIndex();
            j < validList1[i].getWord().length() + validList1[i].getStartIndex();
            j++) {
          if (line[j] == ' ') {
            lettersPlaced++;
            charCount[Character.getNumericValue(validList1[i].getWord().charAt(k)) - 10]++;
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
                  o < validList2temp[validList2n].getWord().length();
                  o++) { // Make the first blank letter lower case (not 100% ideal)
                if (Character.getNumericValue(validList2temp[validList2n].getWord().charAt(o)) - 10 == m) {
                  if (' ' == line[validList2temp[validList2n].getStartIndex() + o]) {
                    validList2temp[validList2n].setWord(
                        validList2temp[validList2n].getWord().substring(0, o)
                            + Character.toLowerCase(validList2temp[validList2n].getWord().charAt(o))
                            + validList2temp[validList2n].getWord().substring(o + 1));
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

    LineWord[] validList2 = new LineWord[validList2n];
    System.arraycopy(validList2temp, 0, validList2, 0, validList2n);
    LineWord[] validList3temp = new LineWord[validList2n];
    int validList3n = 0;
    for (int i = 0; i < validList2n; i++) {
      boolean valid = true;

      var i2 = validList2[i].getStartIndex() - 1;
      if (i2 > 0 && ' ' != line[i2]) {
        valid = false;
      }

      var i1 = validList2[i].getStartIndex() + validList2[i].getWord().length();
      if (i1 < line.length && ' ' != line[i1]) {
        valid = false;
      }

      if (valid) {
        validList3temp[validList3n] = validList2[i];
        validList3n++;
      }
    }
    LineWord[] validList3 = new LineWord[validList3n];
    System.arraycopy(validList3temp, 0, validList3, 0, validList3n);
    return List.of(validList3);
  }
}
