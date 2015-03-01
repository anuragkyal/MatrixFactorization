import java.io.*;

/**
 * Created by anurag on 2/28/15.
 */
public class ReadRatings {
    int user_count = 6040;
    int item_count = 3952;

    public int[][] readRatings() throws IOException {
        int[][] ratings = new int[user_count][item_count];

        File f = new File("/raw_files/ratings.csv");

        BufferedReader b = new BufferedReader(new FileReader("/Users/anurag/study/SEM-2/WEB/Assignments/src/raw_files/ratings.csv"));

        String line = b.readLine();
        while(line != null){
            String[] parts = line.split(",");

            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            ratings[u-1][v-1] = Integer.parseInt(parts[2]);

            line = b.readLine();
        }

        return ratings;
    }

    public int[][] readRatingsItem() throws IOException {
        int[][] ratings = new int[item_count][user_count];

        File f = new File("/raw_files/ratings.csv");

        BufferedReader b = new BufferedReader(new FileReader("/Users/anurag/study/SEM-2/WEB/Assignments/src/raw_files/ratings.csv"));

        String line = b.readLine();
        while(line != null){
            String[] parts = line.split(",");

            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            ratings[v-1][u-1] = Integer.parseInt(parts[2]);

            line = b.readLine();
        }

        return ratings;
    }
}
