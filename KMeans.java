import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class Point implements Comparable<Point>
{
    double xPos;
    double yPos;
    int centroid;

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

    public double getxPos() {
        return xPos;
    }

    public double getyPos() {
        return yPos;
    }

    public int getCentroid() {
        return centroid;
    }

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

    @Override
    public int compareTo(Point o) {
        return (centroid == o.centroid) ? 0 : 1;
    }
}

public class KMeans {

    public double distance(Point p1, Point p2)
    {
        return distance(p1.getxPos(), p1.getyPos(), p2.getxPos(), p2.getyPos());
    }
    public double distance(double x1, double y1, double x2, double y2)
    {
        return distance(new double[]{x1, y1}, new double[]{x2, y2});
    }
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

    public static void main(String[] args)
    {
        String blobsPath = "blobs.csv";
        String outputPath = "output.csv";

        ArrayList<Point> points = new ArrayList<>();
        KMeans kMeans = new KMeans();

        int numCentroids = 3;
        try
        {
            // read the blobs data from the notebook generated CSVs
            double[][] blobs = readCSVdouble(blobsPath);

            // fill out list of points with the points, no centroid assigned (-1) to start
            for (int i = 0; i < blobs.length; i++)
            {
                double xPos = blobs[i][0];
                double yPos = blobs[i][1];
                Point p = new Point(xPos, yPos, -1);
                points.add(p);
            }

            ArrayList<Point> centroids = new ArrayList<>();
            // Randomly select n number of existing points as centroids
            int randomState = 42;

            Random r = new Random(randomState);
            for (int i = 0; i < numCentroids; i++)
            {
                int pos = (int)(r.nextDouble() * points.size());
                Point c = new Point(points.get(pos).xPos, points.get(pos).yPos, i);
                centroids.add(c);
            }

            for (Point c : centroids)
            {
                //System.out.println("centroid: " + c.toString());
            }
            System.out.println();

            ArrayList<Point> newPoints = new ArrayList<>();
            for (Point p : points)
            {
                newPoints.add(new Point(p.xPos, p.yPos, p.centroid));
            }

            int iterations = 0;
            int maxIterations = 100;
            do
            {
                if (iterations == maxIterations)
                {
                    break;
                }
                iterations++;
                // create copy of points list
                points = new ArrayList<>();
                for (Point p : newPoints)
                {
                    points.add(new Point(p.xPos, p.yPos, p.centroid));
                }

                // assign points to centroid
                for (Point p : newPoints)
                {
                    int closestCentroidIndex = -1;
                    double closestDistance = Double.MAX_VALUE;
                    for (int i = 0; i < centroids.size(); i++)
                    {
                        double distance = kMeans.distance(p, centroids.get(i));
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
                    newXPos/=numPointsAtCentroid;
                    newYPos/=numPointsAtCentroid;
                    centroids.get(i).xPos = newXPos;
                    centroids.get(i).yPos = newYPos;
                }
                for (Point c : centroids)
                {
                    //System.out.println("centroid: " + c.toString());
                }
            }
            while(!points.equals(newPoints));

            System.out.println("iterations: " + iterations);

            String[] labels = new String[newPoints.size()];
            for(int i = 0; i < labels.length; i++)
            {
                labels[i] = String.valueOf((newPoints.get(i).centroid));
            }
            Path predictionCSV = Paths.get("output.csv");
            try
            {
                Files.write(predictionCSV, Arrays.asList(labels), StandardCharsets.UTF_8);
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
}
