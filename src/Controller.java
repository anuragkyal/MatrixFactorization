import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by anurag on 3/1/15.
 */
public class Controller {
    public static void main(String args[]) throws IOException {
        Controller controller = new Controller();

        BufferedReader b = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("1 for user based, 2 for item based");
        String s = b.readLine();

        int type = Integer.parseInt(s);

        System.out.print("1 for Jaccard, 2 for Pearson, 3 for Cosine");
        s = b.readLine();

        int sim = Integer.parseInt(s);

        System.out.print("Enter path of the test file:");
        String path = b.readLine();

        int[][] test = controller.prepareTest(path);
        switch (type){
            case 1:
                UserBased userBased = new UserBased();
                break;

            case 2:
                ItemBased itemBased = new ItemBased();
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

            test[i][0] = u;
            test[i][1] = v;
            i++;

            line = b.readLine();
        }

        return test;
    }
}
