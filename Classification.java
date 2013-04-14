
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Zofia Sobocinska
 */
public class Classification {

	int n;
	NeuralNetwork neuralNetwork;
	Vector.Vectors vectors;

	/**
	 *
	 * @param filename Name of the file from which vector shoulb be read
	 * @param size Period length. size=n+1, where n is vector size.
	 * @throws FileNotFoundException
	 * @throws Classification.Vector.Reader.WrongNumbersCountException
	 */
	public Classification(String filename, int size)
					throws FileNotFoundException,
					Vector.Reader.WrongNumbersCountException {
		this.n = size - 1;
		Vector.Reader.Result readerResult = Vector.Reader.readFromFile(filename, n);

		neuralNetwork = readerResult.getNN();
		vectors = readerResult.getVectors();

		neuralNetwork.teach();
	}

	/**
	 * Runs vectors classification via NeuralNetwork for each unclassified vector
	 * from the file.
	 *
	 * @return output expected by homework task
	 */
	public String test() {
		String result = "";
		Iterator<Vector> it = vectors.iterator();
		while (it.hasNext()) {
			Vector vector = it.next();
			int output = neuralNetwork.classify(vector);
			result = result + output;
		}
		return result;
	}

	/**
	 * Class holding vector to classify
	 */
	public static class Vector extends ArrayList<Double> {

		static final long serialVersionUID = 2;
		private Vector normalized = null;

		void addInt(int value) {
			this.add((double) value);
		}

		/**
		 * Creates normalized reprezentation of the vector
		 *
		 * @return vector lineary normalized within [0,1]
		 */
		public Vector normalized() {
			if (normalized == null) {
				normalized = new Vector();
				double min = Collections.min(this);
				double max = Collections.max(this);
				for (Double val : this) {
					double nrm;
					if (min == max) {
						if (val < 0) {
							nrm = -1.0;
						} else if (val == 0) {
							nrm = 0.0;
						} else {
							nrm = 1.0;
						}
					} else if (-min > max) {
						nrm = val / (-min);
					} else {
						nrm = val / max;
					}
					normalized.add(nrm);
				}
			}
			return normalized;
		}

		/**
		 * Class to read input file.
		 */
		public static class Reader {

			/**
			 *
			 * @param filename Input file name
			 * @param n Vector length
			 * @return Class holding data read from file, devided into the neural
			 * network with learning set the and testing set.
			 * @throws FileNotFoundException
			 * @throws Classification.Vector.Reader.WrongNumbersCountException
			 */
			public static Result readFromFile(String filename, int n)
							throws FileNotFoundException, WrongNumbersCountException {

				Result result = new Result(n);

				File file = new File(filename);
				Scanner sc = new Scanner(file);
				while (sc.hasNext()) {
					int classId = sc.nextInt();
					Vector vector = scanVector(sc, n);
					if (classId != 0) {
						result.getNN().addProbe(new NeuralNetwork.Probe(classId, vector));
					} else {
						result.getVectors().add(vector);
					}
				}
				return result;
			}

			/**
			 *
			 * @param sc
			 * @param n
			 * @return next vector scanned from file
			 * @throws Classification.Vector.Reader.WrongNumbersCountException
			 */
			private static Vector scanVector(Scanner sc, int n)
							throws WrongNumbersCountException {
				Vector vector = new Vector();
				for (int i = 0; i < n; i++) {
					if (!sc.hasNext()) {
						throw new WrongNumbersCountException();
					}
					vector.addInt(sc.nextInt());
				}
				return vector;
			}

			/**
			 *
			 */
			public static class WrongNumbersCountException extends Exception {

				/**
				 *
				 */
				public WrongNumbersCountException() {
					super("Wrong numbers count in file!");
				}
				static final long serialVersionUID = 0;
			}

			/**
			 * Class holding result of file scanning - neural network with learning
			 * vectors and set of testing vectors.
			 */
			public static class Result {

				NeuralNetwork neuralNetwork;
				Vectors vectors;

				/**
				 * Creates set of testing vectors and neural network with 0.2 as
				 * learning ratio.
				 *
				 * @param n vector size
				 */
				public Result(int n) {
					neuralNetwork = new NeuralNetwork(n, 0.2);
					vectors = new Vectors();
				}

				/**
				 *
				 * @return neural network based on learning vectors scanned from file
				 */
				public NeuralNetwork getNN() {
					return neuralNetwork;
				}

				/**
				 *
				 * @return testing vectors scanned from file
				 */
				public Vectors getVectors() {
					return vectors;
				}
			}
		}

		/**
		 * Class holding whole set of vectors.
		 */
		public static class Vectors extends ArrayList<Vector> {

			static final long serialVersionUID = 1;
		}
	}

	/**
	 * Class representing neural network. It learns based on learning set and when
	 * learning is finished, it can classify vector to one of the recognized
	 * classes.
	 */
	public static class NeuralNetwork {

		private ArrayList<Probe> probes;
		private Neuron.Layer neurons;

		/**
		 * Class holding vector probe. It keeps vector and class that vector is
		 * expected to be classified to.
		 */
		public static class Probe {

			private int expected;
			private Vector vector;

			/**
			 * Creates probe for vector and its class provided.
			 *
			 * @param cls vector's class number
			 * @param vector vector
			 */
			public Probe(int cls, Vector vector) {
				this.expected = cls;
				this.vector = vector;
			}

			/**
			 *
			 * @return vector
			 */
			public Vector getVector() {
				return vector;
			}

			/**
			 *
			 * @return class which vector is expected to be classified to
			 */
			public int getExpected() {
				return expected;
			}
		}

		/**
		 * Adds new probe to neural network learning set.
		 *
		 * @param probe probe consisting of vector and expected class
		 */
		public void addProbe(Probe probe) {
			probes.add(probe);
			Integer cl = probe.getExpected();
			if (!neurons.containsKey(cl)) {
				neurons.add(cl);
			}
		}

		/**
		 *
		 *
		 * @param inputLength Size of the input vector.
		 * @param learnRatio The bigger learnRatio is, the faster network learns,
		 * but also learning step grows and it it harder for nn to hit the right
		 * weights values.
		 */
		public NeuralNetwork(int inputLength, double learnRatio) {
			this.probes = new ArrayList<>();
			neurons = new Neuron.Layer(inputLength, learnRatio);
		}

		/**
		 * Classifies vector.
		 *
		 * @param input vector to classified
		 * @return recognized class
		 */
		public int classify(Vector input) {
			neurons.setInput(input);
			return neurons.getOutput();
		}

		/**
		 * Start teaching neurons based on each probe.
		 */
		public void teach() {
			for (Probe probe : probes) {
				neurons.teach(probe);
			}
		}

		/**
		 * Class representing single neuron.
		 */
		private static class Neuron {

			private int cls;
			private int inputLength;
			private double[] weights;
			private Vector input;
			private double bias;
			private double learnRatio;

			/**
			 * Creates new neuron for specific class
			 *
			 * @param cls class assigned to this neuron.
			 * @param inputLength input vector size
			 * @param learnRatio learn ratio as described earlier in NeuralNetwork
			 * constructor.
			 */
			public Neuron(int cls, int inputLength, double learnRatio) {
				this.cls = cls;
				this.inputLength = inputLength;
				this.learnRatio = learnRatio;

				weights = new double[inputLength];
				Random rand = new Random();
				for (int i = 0; i < inputLength; i++) {
					weights[i] = rand.nextDouble();
				}

				bias = rand.nextDouble();
			}

			/**
			 * Performs single learning based on probe provided.
			 *
			 * @param probe probe with learning vector
			 */
			public void learn(Probe probe) {

				setInput(probe.getVector());
				double output = getOutput();
				double expected = (probe.getExpected() == cls) ? 1.0 : 0.0;
				double modifier = calcModifier(expected, output);

				adjustWeights(modifier);
			}

			/**
			 * Calculates weight modifier based on learning ratio and the recognition
			 * error.
			 *
			 * @param expected expected value
			 * @param received received value
			 * @return
			 */
			double calcModifier(double expected, double received) {
				return learnRatio * (expected - received) * received * (1.0 - received);
			}

			/**
			 * Adjusts weights based of modifier provided.
			 *
			 * @param modifier
			 */
			private void adjustWeights(double modifier) {

				Iterator<Double> it = input.iterator();
				for (int i = 0; i < inputLength; i++) {
					if (it.hasNext()) {
						double nxtInput = it.next();
						weights[i] += nxtInput * modifier;
					}
				}
				bias += modifier;
			}

			/**
			 * Sets given vector for learning or recognizing reasons.
			 *
			 * @param vector vector to set
			 */
			public void setInput(Vector vector) {
				this.input = vector.normalized();
			}

			/**
			 * Gets output based on vector previously set.
			 *
			 * @return
			 */
			public double getOutput() {
				double tmp = 0.0;
				Iterator<Double> it = input.iterator();
				for (int i = 0; i < inputLength; i++) {
					if (it.hasNext()) {
						double next = it.next();
						tmp += (next * weights[i]);
					}
				}
				tmp += bias;
				return activationFunction(tmp);
			}

			/**
			 * Function modifying output signal.
			 *
			 * @param signal
			 * @return signal activated
			 */
			private double activationFunction(double signal) {
				return 1.0 / (1.0 + Math.exp(-signal));
			}

			/**
			 * Class representing single network layer.
			 */
			public static class Layer extends HashMap<Integer, Neuron> {

				static final long serialVersionUID = 3;
				private int inputLength;
				private double learnRatio;

				/**
				 * Layer constructor
				 *
				 * @param inputLength size of the vector input
				 * @param learnRatio learn ratio as described earlier in NeuralNetwork
				 * constructor.
				 */
				public Layer(int inputLength, double learnRatio) {
					this.inputLength = inputLength;
					this.learnRatio = learnRatio;
				}

				/**
				 */
				}

				/**
				 * Teaches each vector based on probe provided
				 * 
				 * @param probe
				 */
				public void teach(Probe probe) {
					for (Integer nxtKey : keySet()) {
						get(nxtKey).learn(probe);
					}
				}

				/**
				 * Sets each neuron's input
				 * 
				 * @param input input vector
				 */
				public void setInput(Vector input) {
					for (Integer nxtKey : keySet()) {
						get(nxtKey).setInput(input);
					}
				}

				/**
				 * Compares values returned by neurons based on inputs previously set 
				 * and choses the most relevant vector class.
				 * 
				 * @return most relevant vector class
				 */
				public int getOutput() {
					double max = 0;
					int choice = 0;
					for (Integer nxtKey : keySet()) {
						double tmp = get(nxtKey).getOutput();
						if (tmp > max) {
							max = tmp;
							choice = nxtKey;
						}
					}
					return choice;
				}
			}
		}
	}
}
