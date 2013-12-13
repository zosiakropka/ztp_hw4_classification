import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zofia Sobocinska
 */
public class Main {

  static String inputFile;
  static int size;

  /**
   *
   * @param args
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("usage: java Main <input_file> <size>");
    }
    inputFile = args[0];
    size = Integer.parseInt(args[1]);
    try {
      Classification classification = new Classification(inputFile, size);
      String test = classification.test();
      System.out.println("Classification output: " + test);
    } catch (FileNotFoundException |
            Classification.Vect.Reader.WrongNumbersCountException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
