import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class to store info about the centroids
 */
class Centroid extends Point{
    /**
     * Constructor
     * @param x x position
     * @param y y position
     * @param centroid assigned/nearest centroid
     */
    Centroid(double x, double y, int centroid) {
        super(x, y, centroid);
    }
    @Override
    public String toString() {
        return "centroid: " + centroid + " at coordinates: " + xPos + ", " + yPos;
    }
}

/**
 * Class to store info about the points
 */
class Point implements Comparable<Point>
{
    /**
     * x position of the point
     */
    double xPos;
    /**
     * y position of the point
     */
    double yPos;
    /**
     * index of the centroid the Point is closest to
     */
    int centroid;

    /**
     * constructor
     * @param x x position
     * @param y y position
     * @param centroid assigned/nearest centroid
     */
    Point(double x, double y, int centroid)
    {
        this.xPos = x;
        this.yPos = y;
        this.centroid = centroid;
    }

    @Override
    public String toString() {
        return xPos + ", " + yPos + "   in centroid: " + centroid;
    }

    /**
     * get X position
     * @return x position
     */
    public double getxPos() {
        return xPos;
    }

    /**
     * get Y position
     * @return y position
     */
    public double getyPos() {
        return yPos;
    }

    /**
     * get assigned centroid
     * @return centroid
     */
    public int getCentroid() {
        return centroid;
    }

    /**
     * compares Point objects
     * @param o other Point
     * @return true if Points have matching x and y positions, and are assigned to the same centroid, else false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.xPos, xPos) == 0 && Double.compare(point.yPos, yPos) == 0 && centroid == point.centroid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xPos, yPos, centroid);
    }

    /**
     * compares Point objects
     * @param o other Point
     * @return 0 if Points have matching x and y positions, and are assigned to the same centroid, else 1
     */
    @Override
    public int compareTo(Point o) {
        return (centroid == o.centroid && xPos == o.xPos && yPos == o.yPos) ? 0 : 1;
    }
}

/**
 * My Kmeans Class
 */
public class KMeans {

    /**
     * Calculate euclidean distance between two Points
     * @param p1 Point 1
     * @param p2 Point 2
     * @return distance between the two points
     */
    public static double distance(Point p1, Point p2)
    {
        return distance(p1.getxPos(), p1.getyPos(), p2.getxPos(), p2.getyPos());
    }

    /**
     * Calculate euclidean distance between two pairs of coordinates
     * @param x1 x position of first point
     * @param y1 y position of first point
     * @param x2 x position of second point
     * @param y2 y position of second point
     * @return distance between the two points
     */
    public static double distance(double x1, double y1, double x2, double y2)
    {
        return distance(new double[]{x1, y1}, new double[]{x2, y2});
    }

    /**
     * Calculate euclidean distance between two multi-dimensional arrays of coordinates
     * @param a array of coordintates for first point
     * @param b array of coordintates for second point
     * @return
     */
    public static double distance(double[] a, double[] b)
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

    public static void main(String[] args)
    {
        System.out.println("args " + Arrays.toString(args) + "\n");
        // input/output paths and additional parameters are sent in through args
        String inputPath = args[0];
        String labelsOutputPath = args[1];
        String centersOutputPath = args[2];
        int randomState = Integer.parseInt(args[3]);
        int numCentroids = Integer.parseInt(args[4]);
        int maxIterations = Integer.parseInt(args[5]);

        // create an ArrayList of Points (empty to start)
        ArrayList<Point> points = new ArrayList<>();
        try
        {
            // read the data from the notebook generated CSVs
            double[][] data = readCSVdouble(inputPath);

            // fill out list of points with the points, no centroid assigned (-1) to start
            for (int i = 0; i < data.length; i++)
            {
                double xPos = data[i][0];
                double yPos = data[i][1];
                Point p = new Point(xPos, yPos, -1);
                points.add(p);
            }

            // create and ArrayList of Centroids (empty to start)
            ArrayList<Centroid> centroids = new ArrayList<>();

            // assign and create starting centroids with positions of existing points, randomly
            Random r = new Random(randomState);
            for (int i = 0; i < numCentroids; i++)
            {
                int pos = (int)(r.nextDouble() * points.size());
                Centroid c = new Centroid(points.get(pos).xPos, points.get(pos).yPos, i);
                centroids.add(c);
            }

            // create a copy of the points, so we can compare after each iteration to the "previous" state
            ArrayList<Point> newPoints = new ArrayList<>();
            for (Point p : points)
            {
                newPoints.add(new Point(p.xPos, p.yPos, p.centroid));
            }

            int iterations = 0;
            do
            {
                // if we've hit our limit of iterations, break
                if (iterations == maxIterations)
                {
                    System.out.println("max iterations reached");
                    break;
                }
                System.out.println("current iteration: " + iterations);
                // Print out the information for each centroid, once per iteration to see how the centroids move between iterations
                for (Centroid c : centroids)
                {
                    System.out.println(c.toString());
                }
                System.out.println();
                iterations++;

                // set "previous" to "current" upon start of this new iteration
                points = new ArrayList<>();
                for (Point p : newPoints)
                {
                    points.add(new Point(p.xPos, p.yPos, p.centroid));
                }

                // assign points to centroid based on distance to the nearest centroid
                for (Point p : newPoints)
                {
                    int closestCentroidIndex = -1;
                    double closestDistance = Double.MAX_VALUE;
                    for (int i = 0; i < centroids.size(); i++)
                    {
                        double distance = distance(p, centroids.get(i));
                        if (distance < closestDistance)
                        {
                            closestCentroidIndex = centroids.get(i).centroid;
                            closestDistance = distance;
                        }
                    }
                    p.centroid = closestCentroidIndex;
                }

                // update centroid locations
                for (int i = 0; i < centroids.size(); i++)
                {
                    double newXPos = 0;
                    double newYPos = 0;
                    int numPointsAtCentroid = 0;
                    for (Point p : newPoints)
                    {
                        if (p.centroid == i)
                        {
                            numPointsAtCentroid++;
                            newXPos+=p.xPos;
                            newYPos+=p.yPos;
                        }
                    }
                    // calculate and set new x and y positions of the centroid based on the average x and y position of the associated points
                    newXPos/=numPointsAtCentroid;
                    newYPos/=numPointsAtCentroid;
                    centroids.get(i).xPos = newXPos;
                    centroids.get(i).yPos = newYPos;
                }
            }
            // loop ends if centroids have not moved since last iteration and points/centroid assignments have not changed since the last iteration
            while(!points.equals(newPoints));

            System.out.println("total iterations run: " + iterations);

            // prep labels for output to csv
            String[] labels = new String[newPoints.size()];
            for(int i = 0; i < labels.length; i++)
            {
                labels[i] = String.valueOf((newPoints.get(i).centroid));
            }

            // prep centers for output to csv
            String[] centers = new String[centroids.size()];
            for(int i = 0; i < centers.length; i++)
            {
                centers[i] = String.valueOf((centroids.get(i).xPos + "," + centroids.get(i).yPos));
            }

            Path outputLabelsCSV = Paths.get(labelsOutputPath);
            Path outputCentersCSV = Paths.get(centersOutputPath);
            try
            {
                // write output csv's to later be read back in by the python notebook
                Files.write(outputLabelsCSV, Arrays.asList(labels), StandardCharsets.UTF_8);
                Files.write(outputCentersCSV, Arrays.asList(centers), StandardCharsets.UTF_8);
            }
            catch (IOException e)
            {
                System.out.println("error writing file");
            }
        } catch (IOException e)
        {
            System.out.println("unable to read input file");
        }
    }

    /**
     * modified from https://stackoverflow.com/questions/33034833/converting-csv-file-into-2d-array
     * @param path inputPath
     * @return 2d array
     * @throws FileNotFoundException if file is not found
     * @throws IOException if unable to open file
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
}
