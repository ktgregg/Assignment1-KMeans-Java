import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class Centroid extends Point{
    Centroid(double x, double y, int centroid) {
        super(x, y, centroid);
    }
    @Override
    public String toString() {
        return "centroid: " + centroid + " at coordinates: " + xPos + ", " + yPos;
    }
}
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
        return (centroid == o.centroid && xPos == o.xPos && yPos == o.yPos) ? 0 : 1;
    }
}

public class KMeans {

    public static double distance(Point p1, Point p2)
    {
        return distance(p1.getxPos(), p1.getyPos(), p2.getxPos(), p2.getyPos());
    }
    public static double distance(double x1, double y1, double x2, double y2)
    {
        return distance(new double[]{x1, y1}, new double[]{x2, y2});
    }
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
        String inputPath = args[0];
        String labelsOutputPath = args[1];
        String centersOutputPath = args[2];
        int randomState = Integer.parseInt(args[3]);
        int numCentroids = Integer.parseInt(args[4]);
        int maxIterations = Integer.parseInt(args[5]);

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

            ArrayList<Centroid> centroids = new ArrayList<>();

            Random r = new Random(randomState);
            for (int i = 0; i < numCentroids; i++)
            {
                int pos = (int)(r.nextDouble() * points.size());
                Centroid c = new Centroid(points.get(pos).xPos, points.get(pos).yPos, i);
                centroids.add(c);
            }

            ArrayList<Point> newPoints = new ArrayList<>();
            for (Point p : points)
            {
                newPoints.add(new Point(p.xPos, p.yPos, p.centroid));
            }

            int iterations = 0;
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
                    newXPos/=numPointsAtCentroid;
                    newYPos/=numPointsAtCentroid;
                    centroids.get(i).xPos = newXPos;
                    centroids.get(i).yPos = newYPos;
                }
                for (Centroid c : centroids)
                {
                    System.out.println(c.toString());
                }
                System.out.println();
            }
            while(!points.equals(newPoints));

            System.out.println("iterations: " + iterations);

            String[] labels = new String[newPoints.size()];
            for(int i = 0; i < labels.length; i++)
            {
                labels[i] = String.valueOf((newPoints.get(i).centroid));
            }

            String[] centers = new String[centroids.size()];
            for(int i = 0; i < centers.length; i++)
            {
                centers[i] = String.valueOf((centroids.get(i).xPos + "," + centroids.get(i).yPos));
            }
            Path outputLabelsCSV = Paths.get(labelsOutputPath);
            Path outputCentersCSV = Paths.get(centersOutputPath);
            try
            {
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
