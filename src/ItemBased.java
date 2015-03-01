import java.io.IOException;
import java.util.Random;

/**
 * Created by anurag on 2/28/15.
 */
public class ItemBased {
    int user_count = 6040;
    int item_count = 3952;

    int[][] ratings;
    int foldSize = 80000;

    public float[] validateFold(int[][] ratings, float m){
        int counter = 0;
        int[][] train = new int[item_count][user_count];
        int[][] test = new int[foldSize][3];

        copy(ratings, train);

        Random random = new Random();
        while(counter < foldSize){
            int u = Math.abs(random.nextInt()) % item_count;
            int v = Math.abs(random.nextInt()) % user_count;

            if(train[u][v] != 0){
                test[counter][0] = u;
                test[counter][1] = v;
                test[counter][2] = train[u][v];
                counter++;
            }
        }

        //have the train and test ready here
        float[][] jaccardSim = getJaccardSimilarity(train);
        float[][] pearsonSim = getPearsonSimilarity(train);
        float[][] cosineSim = getCosineSimilarity(train);

        float[] error = new float[3];

        for(int k=0; k<3; k++) {
            int norm = 0;
            float[][] sim = null;
            String type = "";

            switch (k){
                case 0:
                    sim = jaccardSim;
                    type = "J";
                    break;
                case 1:
                    sim = pearsonSim;
                    type = "P";
                    break;
                case 2:
                    sim = cosineSim;
                    type = "C";
                    break;
            }

            for (int i = 0; i < test.length; i++) {
                int u = test[i][0];
                int v = test[i][1];
                int r = test[i][2];

                int sum = 0;
                int count = 0;

                for (int j = 0; j < item_count; j++) {
                    if (sim[u][j] >= m) {
                        if (ratings[j][v] > 0) {
                            sum += ratings[j][v];
                            count++;
                        }
                    }
                }

                if (count != 0) {
                    float t = r - (float) sum / count;
                    error[k] += t * t;
                    norm++;
                }
            }

            error[k] = (float) Math.sqrt(error[k] / norm);
            System.out.println(type + error[k]);
        }

        return error;
    }

    void copy(int[][] source, int[][] target){
        for(int i=0; i<item_count; i++){
            for(int j=0; j<user_count; j++){
                target[i][j] = source[i][j];
            }
        }
    }

    public float[][] getJaccardSimilarity(int[][] ratings){
        float[][] sim = new float[item_count][item_count];

        for(int i=0; i<item_count; i++){
            for(int j=i+1; j<item_count; j++){
                int inter = 0;
                int union = 0;

                for(int k=0; k<user_count; k++){
                    if(ratings[i][k] != 0 && ratings[j][k] != 0){
                        inter++;
                        union++;
                    } else if(ratings[i][k] != 0 || ratings[j][k] != 0){
                        union++;
                    }
                }

                float t = union !=0 ? (float)inter/union : 0;
                sim[i][j] = t;
                sim[j][i] = t;
            }
        }

        return sim;
    }

    public float[][] getPearsonSimilarity(int[][] ratings){
        float[][] sim = new float[item_count][item_count];
        int[] sum = populateSum(ratings);
        int[] sumSquare = populateSumSquare(ratings);

        for(int i=0; i<item_count; i++){
            for(int j=i+1; j<item_count; j++){
                int t = 0;

                for(int k=0; k<user_count; k++){
                    t += ratings[i][k] * ratings[j][k];
                }

                int sum_i = sum[i];
                int sum_j = sum[j];

                float sim_t = (float) ((t - sum_i*sum_j/user_count) / Math.sqrt((sumSquare[i] - sum_i*sum_i/user_count) * (sumSquare[j] - sum_j*sum_j/user_count)));
                sim[i][j] = sim_t;
                sim[j][i] = sim_t;
            }
        }

        return sim;
    }

    public int[] populateSum(int[][] ratings){
        int[] sum = new int[item_count];

        for(int i=0; i<item_count; i++){
            int sum_t = 0;
            for(int j=0; j<user_count; j++){
                sum_t += ratings[i][j];
            }
            sum[i] = sum_t;
        }

        return sum;
    }

    public int[] populateSumSquare(int[][] ratings){
        int[] sum = new int[item_count];

        for(int i=0; i<item_count; i++){
            int sum_t = 0;
            for(int j=0; j<user_count; j++){
                int t = ratings[i][j];
                sum_t += t*t;
            }
            sum[i] = sum_t;
        }

        return sum;
    }

    public float[][] getCosineSimilarity(int[][] ratings){
        float[][] sim = new float[item_count][item_count];
        float[] len = populateLength(ratings);

        for(int i=0; i<item_count; i++){
            for(int j=i+1; j<item_count; j++){
                int sum = 0;

                for(int k=0; k<user_count; k++){
                    sum += ratings[i][k] * ratings[j][k];
                }

                float t = sum/(len[i]*len[j]);
                sim[i][j] = t;
                sim[j][i] = t;
            }
        }

        return sim;
    }

    public float[] populateLength(int[][] ratings){
        float[] length = new float[item_count];

        for(int i=0; i<item_count; i++){
            int sum = 0;

            for(int j=0; j<user_count; j++){
                int t = ratings[i][j];
                sum += t*t;
            }

            length[i] = (float) Math.sqrt(sum);
        }

        return length;
    }

    public void performTenFold(int[][] ratings){
        int folds = 5;

        float[] t = new float[]{.05f, .07f, .075f, .08f};

        for(int m = 0; m<t.length; m++) {
            float[] globalError = new float[3];
            float[][] errors = new float[3][folds];

            for (int i = 1; i <= folds; i++) {
                System.out.println("Fold" + i);
                float[] tempError = validateFold(ratings, t[m]);


                for (int j = 0; j < 3; j++) {
                    globalError[j] += tempError[j];
                    errors[j][i - 1] = tempError[j];
                }
            }

            for (int i = 0; i < 3; i++) {
                globalError[i] /= folds;
                System.out.println("Final:" + globalError[i]);

                System.out.print("Individual:");
                for (int j = 0; j < folds; j++) {
                    System.out.print(errors[i][j] + " ");
                }

                System.out.println();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        ItemBased itemBased = new ItemBased();
        ReadRatings readRatings = new ReadRatings();

        int[][] ratings = readRatings.readRatingsItem();
        itemBased.performTenFold(ratings);
    }
}
