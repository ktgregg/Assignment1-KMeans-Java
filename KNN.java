import java.io.IOException;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.io.*;
import java.util.Random;

/**
 * My absolutely amazing java implementation of KNN.
 *
 * @author kierangregg
 */
public class KNN {

    /**
     * k number of neighbors to check
     */
	private int k;
    /**
     * random seed
     */
	private long random_state;
    /**
     * train features - "train_x"
     */
    private double [][] features;
    /**
     * train labels - "train_y"
     */
    private int[] labels;

    /**
     * class to store information about a data point/instance
     */
    class DataInstance implements Comparable<DataInstance>
    {
        double[] features;
        double[] test_features;
        int label;

        /**
         * Constructor
         * @param features
         * @param label
         */
        public DataInstance(double[] features, int label) {
            this.features = features;
            this.label = label;
        }

        /**
         * sets the test features for this datapoint to check against the test set for sorting
         * @param test_features
         */
        public void setTestFeatures(double[] test_features)
        {
            this.test_features = test_features;
        }

        /**
         * custom comparator, comparing 6 dimension euclidean distance, then randomly if theres a tie
         * @param other
         * @return
         */
        @Override
        public int compareTo(DataInstance other)
        {
            if (distance(features, test_features) < distance(other.features, other.test_features))
            {
                return -1;
            }
            else if (distance(features, test_features) > distance(other.features, other.test_features))
            {
                return 1;
            }
            else
            {
                Random r = new Random(random_state);
                double randD = r.nextDouble();
                return ((randD < 0.5) == true) ? - 1 : 1;
            }
        }

        @Override
        public String toString()
        {
            return ("features: " + Arrays.toString(features) + ", label: " + label);
        }

    }

	public static void main(String[] args)
    {
        // Command Line arguments passed in to program
        // example usage
        //  javac KNN.java
        //  java KNN train_x.csv train_y.csv features.csv 3 42 prediction.csv
        // this will compile and run the program using the csv's generated from the notebook
        // and output to prediction csv using k=3 and using 42 as the random seed

        String train_x_csv_path = args[0];
        String train_y_csv_path = args[1];
        String features_csv_path = args[2];
        int k = Integer.parseInt(args[3]);
        long random_state = Long.parseLong(args[4]);
        String prediction_csv_path = args[5];

        KNN knn = new KNN(k, random_state);
        try
        {
            // read the training data from the notebook generated CSVs
            double[][] train_x = readCSVdouble(train_x_csv_path);
            int[][] train_y_2d = readCSVint(train_y_csv_path);
            int[] train_y = new int[train_y_2d.length];
            for (int i = 0; i < train_y_2d.length; i++)
            {
                train_y[i] = train_y_2d[i][0];
            }

            // read the test data from the notebook generated CSVs
            double[][] features = readCSVdouble(features_csv_path);

            // store the train data
            knn.fit(train_x, train_y);
            // get hypotheses for the given features, from the predict method
            int[] hypotheses = knn.predict(features);

            // Stringify the ints to prep for saving as a CSV to validate accuracy score in the notebook
            String[] hypothesesString = new String[hypotheses.length];
            for (int i = 0; i < hypotheses.length; i++)
            {
                hypothesesString[i] = String.valueOf(hypotheses[i]);
            }
            Path predictionCSV = Paths.get(prediction_csv_path);
            try
            {
                // write CSV of predictions
                Files.write(predictionCSV, Arrays.asList(hypothesesString), StandardCharsets.UTF_8);
            }
            catch (IOException e)
            {
                System.out.println("error writing file");
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("unable to read input file");
        }
        catch (IOException e)
        {
            System.out.println("unable to read input file");
        }

    }

    /**
     * Constructor
     * @param k k value
     * @param random_state random seed
     */
	public KNN(int k, long random_state) {
		this.k = k;
		this.random_state = random_state;
	}

    /**
     * modified https://stackoverflow.com/questions/33034833/converting-csv-file-into-2d-array
     * @param path inputPath
     * @return 2d array
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static int[][] readCSVint(String path) throws FileNotFoundException, IOException {
        try (FileReader fr = new FileReader(path);
             BufferedReader br = new BufferedReader(fr)) {
            Collection<int[]> lines = new ArrayList<>();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] elements = line.split(";");
                int[] nums = new int[elements.length];
                for (int i = 0; i < elements.length; i++)
                {
                    nums[i] = Integer.parseInt(elements[i]);
                }
                lines.add(nums);
            }
            return lines.toArray(new int[lines.size()][]);
        }
    }

    /**
     * modified https://stackoverflow.com/questions/33034833/converting-csv-file-into-2d-array
     * @param path inputPath
     * @return 2d array
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static double[][] readCSVdouble(String path) throws FileNotFoundException, IOException {
        try (FileReader fr = new FileReader(path);
             BufferedReader br = new BufferedReader(fr)) {
            Collection<double[]> lines = new ArrayList<>();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String[] elements = line.split(",");
                double[] nums = new double[elements.length];
                for (int i = 0; i < elements.length; i++)
                {
                    nums[i] = Double.parseDouble(elements[i]);
                }
                lines.add(nums);
            }
            return lines.toArray(new double[lines.size()][]);
        }
    }

    /**
     * store the training data
     * @param features train_x
     * @param labels train_y
     */
	public void fit(double [][] features, int [] labels) {
        this.features = features;
        this.labels = labels;
	}

    /**
     * get K nearest points from the sorted List
     * @param k number of points
     * @param points list of points
     * @return K nearest points
     */
	public List<DataInstance> getKNearest(int k, List<DataInstance> points)
    {
        return points.subList(0,k);
    }

    /**
     * @param points list of points to check
     * @return 1 if majority of points are 1, 0 if majority of points are 0,
     * if theres a tie, randomly select
     */
    public int majority(List<DataInstance> points)
    {
        int numZero = 0;
        int numOne = 0;
        for (DataInstance point : points)
        {
            if (point.label == 1)
            {
                numOne++;
            }
            else
            {
                numZero++;
            }
        }
        // if most of the neighbors are "0"
        if (numZero > numOne)
        {
            return 0;
        }
        // if most of the neighbors are "1"
        else if (numZero < numOne)
        {
            return 1;
        }
        // if theres a tie in zeros and one's return 1 or 0 randomly
        else
        {
            Random r = new Random(random_state);
            double randD = r.nextDouble();
            return ((randD < 0.5) == true) ? - 1 : 1;
        }
    }

    /**
     * @param features_test test features to predict
     * @return list of hypotheses
     */
	public int[] predict(double [][] features_test) {
        int numFeatures = features_test.length;
        int[] hypotheses = new int[numFeatures];
        ArrayList<DataInstance> data = new ArrayList<>();

        // create a data instance for each of the test features, with an empty reference point, put in a list
        for (int i = 0; i < features.length; i++)
        {
            data.add(new DataInstance(features[i], labels[i]));
        }

        for (int i = 0; i < numFeatures; i++)
        {
            // making a copy of the data instaces
            ArrayList<DataInstance> points = new ArrayList<>(data);
            // setting the test references value for each of the data instances to prep for sorting
            for (DataInstance dataInstance : points)
            {
                dataInstance.setTestFeatures(features_test[i]);
            }
            // sort the points (uses the custom distance to the test feature points for comparator)
            Collections.sort(points);
            // get the list of the K nearest points
            List<DataInstance> kPoints = getKNearest(k, points);
            // determine whether the test point should be 1 or 0 based on the majority value of K nearest neighbors
            int hypothesis = majority(kPoints);
            // store the hypothesis in the list
            hypotheses[i] = hypothesis;
        }
        return hypotheses;
	}

    /**
     * calculates euclidean distance between two 6-dimension coordinates
     * square root of the sum of squared differences between n number of points
     * @return euclidean distance between two 6-dimension coordinates
     */
	public double distance(double[] a, double[] b)
    {
        double sum = 0;
        for (int i = 0; i < a.length; i++)
        {
            double diff = a[i] - b[i];
            double diffSquared = Math.pow(diff, 2);
            sum += diffSquared;
        }
        return Math.pow(sum, 0.5);
    }
}
