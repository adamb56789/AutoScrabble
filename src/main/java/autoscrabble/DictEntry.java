package autoscrabble;

public class DictEntry {
  public static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
  final int alphabetMask;
  private final String word;

  public DictEntry(String word) {
    this.word = word;

    alphabetMask = createAlphabetMask(word);
  }

  public static int createAlphabetMask(String word) {
    int mask = 0;
    for (int i = 0; i < ALPHABET.length; i++) {
      char c = ALPHABET[i];
      if (word.indexOf(c) != -1) {
        mask |= 0b1 << i;
      }
    }
    return mask;
  }

  public String getWord() {
    return word;
  }

  public int getAlphabetMask() {
    return alphabetMask;
  }
}
