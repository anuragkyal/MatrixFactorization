import java.io.IOException;
import java.util.Random;

/**
 * Created by anurag on 2/28/15.
 */
public class UserBased extends Thread{
    Thread thread;
    int m;
    String threadName;

    int user_count = 6040;
    int item_count = 3952;

    int[][] ratings;
    int foldSize = 80000;

    UserBased(int[][] ratings) throws IOException {
        this.ratings = ratings;
    }

    public float[] validateFold(int[][] ratings, float th){
        int counter = 0;
        int[][] train = new int[user_count][item_count];
        int[][] test = new int[foldSize][3];

        copy(ratings, train);

        Random random = new Random();
        while(counter < foldSize){
            int u = Math.abs(random.nextInt()) % user_count;
            int v = Math.abs(random.nextInt()) % item_count;

            if(train[u][v] != 0){
                test[counter][0] = u;
                test[counter][1] = v;
                test[counter][2] = train[u][v];
                train[u][v] = 0;
                counter++;
            }
        }

        //have the train and test ready here
        float[][] jaccardSim = null;//getJaccardSimilarity(train);
        float[][] pearsonSim = getPearsonSimilarity(train);
        float[][] cosineSim = null;//getCosineSimilarity(train);

        float[] error = new float[3];
        float[] avg = getAvgRating(train);

        for(int k=1; k<2; k++) {
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

                for (int j = 0; j < user_count; j++) {
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
        float[] avg = new float[user_count];

        for(int i=0; i<user_count; i++){
            int ratingSum = 0;
            int count = 0;

            for(int j=0; j<item_count; j++){
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
        for(int i=0; i<user_count; i++){
            for(int j=0; j<item_count; j++){
                target[i][j] = source[i][j];
            }
        }
    }

    public float[][] getJaccardSimilarity(int[][] ratings){
        float[][] sim = new float[user_count][user_count];

        for(int i=0; i<user_count; i++){
            for(int j=i+1; j<user_count; j++){
                int inter = 0;
                int union = 0;

                for(int k=0; k<item_count; k++){
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
        float[][] sim = new float[user_count][user_count];
        int[] sum = populateSum(ratings);
        int[] sumSquare = populateSumSquare(ratings);

        for(int i=0; i<user_count; i++){
            for(int j=i+1; j<user_count; j++){
                int t = 0;

                for(int k=0; k<item_count; k++){
                    t += ratings[i][k] * ratings[j][k];
                }

                int sum_i = sum[i];
                int sum_j = sum[j];

                float sim_t = (float) ((t - sum_i*sum_j/item_count) / Math.sqrt((sumSquare[i] - sum_i*sum_i/item_count) * (sumSquare[j] - sum_j*sum_j/item_count)));
                sim[i][j] = sim_t;
                sim[j][i] = sim_t;
            }
        }

        return sim;
    }

    public int[] populateSum(int[][] ratings){
        int[] sum = new int[user_count];

        for(int i=0; i<user_count; i++){
            int sum_t = 0;
            for(int j=0; j<item_count; j++){
                sum_t += ratings[i][j];
            }
            sum[i] = sum_t;
        }

        return sum;
    }

    public int[] populateSumSquare(int[][] ratings){
        int[] sum = new int[user_count];

        for(int i=0; i<user_count; i++){
            int sum_t = 0;
            for(int j=0; j<item_count; j++){
                int t = ratings[i][j];
                sum_t += t*t;
            }
            sum[i] = sum_t;
        }

        return sum;
    }

    public float[][] getCosineSimilarity(int[][] ratings){
        float[][] sim = new float[user_count][user_count];
        float[] len = populateLength(ratings);

        for(int i=0; i<user_count; i++){
            for(int j=i+1; j<user_count; j++){
                int sum = 0;

                for(int k=0; k<item_count; k++){
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
        float[] length = new float[user_count];

        for(int i=0; i<user_count; i++){
            int sum = 0;

            for(int j=0; j<item_count; j++){
                int t = ratings[i][j];
                sum += t*t;
            }

            length[i] = (float) Math.sqrt(sum);
        }

        return length;
    }

    public void performTenFold(int[][] ratings){
        int folds = 10;

        float[] t = new float[]{.09f, 1f, 1.1f};

        for(int m = 0; m<1; m++) {
            float[] globalError = new float[3];
            float[][] errors = new float[3][folds];

            System.out.println("M=" + m);
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

    public float getRating(float[][] sim, float[] avg, int u, int v){
        float sum = 0;
        float normal = 0;
        int count = 0;

        for (int j = 0; j < user_count; j++) {
            float t = ratings[j][v];

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
            return t;
        }

        return 0;
    }

    public void performTest(int simType, int[][] test){
        float[][] sim = null;
        float[] avg = getAvgRating(ratings);

        switch (simType){
            case 1:
                sim = getJaccardSimilarity(ratings);
                break;
            case 2:
                sim = getPearsonSimilarity(ratings);
                break;
            case 3:
                sim = getCosineSimilarity(ratings);
                break;
        }

        for(int i=0; i<200000; i++){
            int u = test[i][0];
            int v = test[i][1];

            System.out.println(getRating(sim, avg, u, v));
        }
    }

    public float[][] getSimilarity(int[][] ratings, int type){
        System.out.print("Calculating user sim: ");
        switch (type){
            case 1:
                System.out.println("jaccard");
                return getJaccardSimilarity(ratings);
            case 2:
                System.out.println("pearson");
                return getPearsonSimilarity(ratings);
            case 3:
                System.out.println("cosine");
                return getCosineSimilarity(ratings);
            default:return null;
        }
    }

    public static void main(String args[]) throws IOException {
        ReadRatings readRatings = new ReadRatings();
        UserBased userBased = new UserBased(readRatings.readRatingsUser());
        userBased.performTenFold(userBased.ratings);
    }
}
