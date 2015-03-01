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
                train[u][v] = 0;
                counter++;
            }
        }

        //have the train and test ready here
        float[][] jaccardSim = getJaccardSimilarity(train);
        float[][] pearsonSim = getPearsonSimilarity(train);
        float[][] cosineSim = getCosineSimilarity(train);

        float[] error = new float[3];
        float[] avg = getAvgRating(train);

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

                float sum = 0;
                float normal = 0;
                int count = 0;

                for (int j = 0; j < item_count; j++) {
                    float t = train[j][v];

                    if (t > 0) {
                        float sim_t = sim[u][j];
                        sum += sim_t * (t - avg[j]);
                        normal += sim_t;
                        count++;
                    }
                }

                if (count > 0 && normal > 0) {
                    float t = avg[u] + sum/normal;
                    t = t > 5 ? 5 : t;
                    t = t - r;
                    error[k] += t * t;
                    norm++;
                }
            }

            error[k] = (float) Math.sqrt(error[k] / norm);
            System.out.println(type + error[k]);
        }

        return error;
    }

    float[] getAvgRating(int[][] ratings){
        float[] avg = new float[item_count];

        for(int i=0; i<item_count; i++){
            int ratingSum = 0;
            int count = 0;

            for(int j=0; j<user_count; j++){
                if(ratings[i][j] > 0){
                    ratingSum += ratings[i][j];
                    count++;
                }
            }

            avg[i] = count > 0 ? (float)ratingSum/count : 0;
        }

        return avg;
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
        float[] avg = getAvgRating(ratings);

        for(int i=0; i<item_count; i++){
            for(int j=i+1; j<item_count; j++){
                float num = 0;
                float diffSq1 = 0;
                float diffSq2 = 0;

                for(int k=0; k<user_count; k++){
                    if(ratings[i][k] > 0 && ratings[j][k] > 0){
                        float diff1 = ratings[i][k] - avg[i];
                        float diff2 = ratings[j][k] - avg[j];

                        num += diff1*diff2;
                        diffSq1 += diff1*diff1;
                        diffSq2 += diff2*diff2;
                    }
                }

                //float sim_t = (float) ((t - sum_i*sum_j/user_count) / Math.sqrt((sumSquare[i] - sum_i*sum_i/user_count) * (sumSquare[j] - sum_j*sum_j/user_count)));
                float sim_t = (float) (num/(Math.sqrt(diffSq1)*Math.sqrt(diffSq2)));
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

        for(int m = 0; m<1; m++) {
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
