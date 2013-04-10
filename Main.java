
/**
 *
 * @author Zofia Sobocinska
 */
public class Main {

	static String inputFile;
	static int n;

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("usage: java Main <input_file> <square_size>");
		}
		inputFile = args[0];
		n = Integer.parseInt(args[1]);

		Classification classification = new Classification(inputFile, n);
	}
}
