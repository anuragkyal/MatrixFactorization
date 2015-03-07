import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by anurag on 3/1/15.
 */
public class Recommender {
    public static void main(String args[]) throws IOException {
        Recommender recommender = new Recommender();
        ReadRatings readRatings = new ReadRatings();

        BufferedReader b = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("1 for user based, 2 for item based: ");
        String s = b.readLine();
        int type = Integer.parseInt(s);

        System.out.print("1 for Jaccard, 2 for Pearson, 3 for Cosine: ");
        s = b.readLine();
        int sim = Integer.parseInt(s);

        System.out.print("Enter path of the train file: ");
        String trainPath = b.readLine();

        System.out.print("Enter path of the test file: ");
        String testPath = b.readLine();

        int[][] test = recommender.prepareTest(testPath);
        switch (type){
            case 1:
                UserBased userBased = new UserBased(readRatings.readRatingsUser(trainPath));
                userBased.performTest(sim, test);
                break;

            case 2:
                ItemBased itemBased = new ItemBased(readRatings.readRatingsItem(trainPath));
                itemBased.performTest(sim, test);
                break;
        }
    }

    public int[][] prepareTest(String path) throws IOException {
        BufferedReader b = new BufferedReader(new FileReader(path));

        int[][] test = new int[200000][2];
        String line = b.readLine();
        int i = 0;

        while(line != null){
            String[] parts = line.split(",");
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);

            test[i][0] = u-1;
            test[i][1] = v-1;
            i++;

            line = b.readLine();
        }

        return test;
    }
}
