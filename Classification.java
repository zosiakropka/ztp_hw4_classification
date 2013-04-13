
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
	 * @param input
	 * @param size
	 * @throws FileNotFoundException
	 * @throws Classification.Vector.Reader.WrongNumbersCountException
	 */
	public Classification(String input, int size)
					throws FileNotFoundException,
					Vector.Reader.WrongNumbersCountException {
		this.n = size - 1;
		Vector.Reader.Result readerResult = Vector.Reader.readFromFile(input, n);

		neuralNetwork = readerResult.getNN();
		vectors = readerResult.getVectors();

		neuralNetwork.learn();
	}

	/**
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
		Vector normalized = null;

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
//			if (min > max) max = min;
				for (Double val : this) {
					normalized.add((val-min) / (max-min));
				}
			}
			return normalized;
		}

		/**
		 *
		 */
		public static class Reader {

			/**
			 *
			 * @param filename
			 * @param n
			 * @return
			 * @throws FileNotFoundException
			 * @throws Classification.Vector.Reader.WrongNumbersCountException
			 */
			public static Result readFromFile(String filename, int n)
							throws FileNotFoundException, WrongNumbersCountException {

				String regexp = "\\D+";

				Result result = new Result(n);

				File file = new File(filename);
//				Scanner sc = new Scanner(file).useDelimiter(regexp);
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
			 * @return
			 * @throws Classification.Vector.Reader.WrongNumbersCountException
			 */
			static Vector scanVector(Scanner sc, int n)
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
			 *
			 */
			public static class Result {

				NeuralNetwork neuralNetwork;
				Vectors vectors;

				/**
				 *
				 * @param n
				 */
				public Result(int n) {
					neuralNetwork = new NeuralNetwork(n, 0.5);
					vectors = new Vectors();
				}

				/**
				 *
				 * @return
				 */
				public NeuralNetwork getNN() {
					return neuralNetwork;
				}

				/**
				 *
				 * @return
				 */
				public Vectors getVectors() {
					return vectors;
				}
			}
		}

		/**
		 *
		 */
		public static class Vectors extends ArrayList<Vector> {

			/**
			 *
			 */
			public Vectors() {
			}
			static final long serialVersionUID = 1;
		}
	}

	/**
	 *
	 */
	public static class NeuralNetwork {

		int inputLength;
		double learnRatio;
		ArrayList<Probe> probes;
		Neuron.Layer neurons;

		/**
		 *
		 */
		public static class Probe {

			int expected;
			Vector vector;

			/**
			 *
			 * @param cl
			 * @param vector
			 */
			public Probe(int cl, Vector vector) {
				this.expected = cl;
				this.vector = vector;
			}

			/**
			 *
			 * @return
			 */
			public Vector getVector() {
				return vector;
			}

			/**
			 *
			 * @return
			 */
			public int getExpected() {
				return expected;
			}
		}

		/**
		 *
		 * @param probe
		 */
		public void addProbe(Probe probe) {
			probes.add(probe);
			Integer cl = probe.getExpected();
			if (!neurons.contains(cl)) {
				neurons.add(cl);
			}
		}

		/**
		 *
		 * @param inputLength
		 * @param learnRatio
		 */
		public NeuralNetwork(int inputLength, double learnRatio) {
			this.learnRatio = learnRatio;
			this.inputLength = inputLength;
			this.probes = new ArrayList<>();
			neurons = new Neuron.Layer(inputLength, learnRatio);
		}

		/**
		 *
		 * @param input
		 * @return
		 */
		int classify(Vector input) {
			setInput(input);
			return getOutput();
		}

		/**
		 *
		 * @param input
		 */
		void setInput(Vector input) {
			for (Integer nxtKey : neurons.keySet()) {
				neurons.get(nxtKey).setInput(input);
			}
		}

		/**
		 *
		 * @return
		 */
		int getOutput() {
			double max = 0;
			int choice = 0;
			for (Integer nxtKey : neurons.keySet()) {
				double tmp = neurons.get(nxtKey).getOutput();
				if (tmp >= max) {
					max = tmp;
					choice = nxtKey;
				}
			}
			return choice;
		}

		/**
		 *
		 */
		public void learn() {
			for (Probe probe : probes) {
				learn(probe);
			}
		}

		/**
		 *
		 * @param probe
		 */
		void learn(Probe probe) {
			for (Integer nxtKey : neurons.keySet()) {
				neurons.get(nxtKey).learn(probe);
			}
		}

		/**
		 *
		 */
		static class Neuron {

			int cl;
			int inputLength;
			double[] weights;
			Vector input;
			double bias;
			double biasWeight;
			double learnRatio;

			/**
			 *
			 * @param cl
			 * @param inputLength
			 * @param learnRatio
			 */
			public Neuron(int cl, int inputLength, double learnRatio) {
				this.cl = cl;
				this.inputLength = inputLength;
				this.learnRatio = learnRatio;

				weights = new double[inputLength];
				Random rand = new Random();
				for (int i = 0; i < inputLength; i++) {
					weights[i] = rand.nextDouble();
				}

				bias = 1.0;
				biasWeight = 1.0;
			}

			/**
			 *
			 * @param probe
			 */
			public void learn(Probe probe) {

				setInput(probe.getVector());
				double output = getOutput();
				double expected = (probe.getExpected()==cl)?1:0;
				double modifier = calcModifier(expected, output);
				Iterator<Double> it = input.iterator();
				for (int i = 0; i < inputLength; i++) {
					if (it.hasNext()) {
						double nxtInput = it.next();
						weights[i] += nxtInput * modifier;
					}
				}
				biasWeight += modifier;
			}

			/**
			 *
			 * @param expected
			 * @param received
			 * @return
			 */
			double calcModifier(double expected, double received) {
				return learnRatio * (expected - received) * received * (1.0 - received);
			}

			/**
			 *
			 * @param vector
			 */
			public void setInput(Vector vector) {
				this.input = vector.normalized();
			}

			/**
			 *
			 * @return
			 */
			public double getOutput() {
				double tmp = 0.0;
				Iterator<Double> it = input.iterator();
				for (int i = 0; i < inputLength; i++) {
					if (it.hasNext()) {
						tmp += (it.next() * weights[i]);
					}
				}
				return activationFunction(tmp);
			}

			/**
			 *
			 * @param signal
			 * @return
			 */
			double activationFunction(double signal) {
				return 1.0 / (1.0 + Math.exp(-signal));
			}

			/**
			 *
			 */
			public static class Layer extends HashMap<Integer, Neuron> {

				static final long serialVersionUID = 3;
				int inputLength;
				double learnRatio;

				/**
				 *
				 * @param inputLength
				 * @param learnRatio
				 */
				public Layer(int inputLength, double learnRatio) {
					this.inputLength = inputLength;
					this.learnRatio = learnRatio;
				}

				/**
				 *
				 * @param cl
				 * @return
				 */
				public boolean contains(Integer cl) {
					return containsKey(cl);
				}

				/**
				 *
				 * @param expected
				 */
				public void add(int expected) {
					if (!contains(expected)) {
						this.put(expected, new Neuron(expected, inputLength, learnRatio));
					}
				}
			}
		}
	}
}
