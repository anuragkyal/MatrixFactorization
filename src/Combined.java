import java.io.IOException;
import java.util.Random;

/**
 * Created by anurag on 3/6/15.
 */
public class Combined {
    ReadRatings readRatings = new ReadRatings();
    UserBased userBased;
    ItemBased itemBased;

    int foldSize = 80000;
    int user_count = 6040;
    int item_count = 3952;
    int foldCount = 10;

    public Combined() throws IOException {
        userBased = new UserBased(null);
        itemBased = new ItemBased(null);
    }

    public void testBench() throws IOException {
        int[][] ratings = readRatings.readRatingsUser();
        int[][] test = new int[foldSize][3];

        Random random = new Random();
        for(int i=0; i<foldCount; i++){
            //Taking backup of ratings
            int[][] temp = new int[user_count][item_count];
            copy(ratings, temp);

            //Preparing the test bench
            for(int j=0; j<foldSize; j++) {
                int rating = 0;
                int userId = 0;
                int itemId = 0;

                while(rating == 0){
                    userId = Math.abs(random.nextInt()) % user_count;
                    itemId = Math.abs(random.nextInt()) % item_count;

                    rating = ratings[userId][itemId];
                }

                test[j][0] = userId;
                test[j][1] = itemId;
                test[j][2] = rating;

                ratings[userId][itemId] = 0;
            }

            int[][] itemRatings = transform(ratings);
            float[][] userSim = userBased.getSimilarity(ratings, 3);
            float[][] itemSim = itemBased.getSimilarity(itemRatings, 3);

            float rmse = 0;
            int errorCount = 0;

            for(int j=0; j<foldSize; j++){
                int userIndex = test[j][0];
                int itemIndex = test[j][1];
                int correctRating = test[j][2];

                float score = 0;
                float norm = 0;

                for(int k=0; k<user_count; k++){
                    float currUserRating = ratings[k][itemIndex];
                    float currUserSim = userSim[userIndex][k];
                    if(currUserRating > 0){
                        score += currUserSim * currUserRating;
                        norm += currUserSim;
                    }
                }

                for(int k=0; k<item_count; k++){
                    float currItemSim = itemSim[itemIndex][k];
                    float currItemRating = itemRatings[k][userIndex];
                    if(currItemRating > 0){
                        score += currItemSim * currItemRating;
                        norm += currItemSim;
                    }
                }

                if(norm > 0){
                    rmse += (correctRating - score/norm) * (correctRating - score/norm) ;
                    errorCount++;
                }
            }

            if(errorCount > 0){
                rmse = (float) Math.sqrt(rmse/errorCount);
            }

            System.out.println("RMSE = " + rmse);

            //Setting back ratings to original
            ratings = temp;
        }
    }

    void copy(int[][] source, int[][] target){
        for(int i=0; i<user_count; i++){
            for(int j=0; j<item_count; j++){
                target[i][j] = source[i][j];
            }
        }
    }

    int[][] transform(int[][] ratings){
        int[][] itemRatings = new int[item_count][user_count];

        for(int i=0; i<user_count; i++){
            for(int j=0; j<item_count; j++){
                itemRatings[j][i] = ratings[i][j];
            }
        }

        return itemRatings;
    }

    public void performTest(int[][] ratings, int[][] test, int type){
        int[][] itemRatings = transform(ratings);
        float[][] userSim = userBased.getSimilarity(ratings, type);
        float[][] itemSim = itemBased.getSimilarity(itemRatings, type);

        for(int j=0; j<test.length; j++){
            int userIndex = test[j][0];
            int itemIndex = test[j][1];

            float score = 0;
            float norm = 0;

            for(int k=0; k<user_count; k++){
                float currUserRating = ratings[k][itemIndex];
                float currUserSim = userSim[userIndex][k];
                if(currUserRating > 0){
                    score += currUserSim * currUserRating;
                    norm += currUserSim;
                }
            }

            for(int k=0; k<item_count; k++){
                float currItemSim = itemSim[itemIndex][k];
                float currItemRating = itemRatings[k][userIndex];
                if(currItemRating > 0){
                    score += currItemSim * currItemRating;
                    norm += currItemSim;
                }
            }

            if(norm > 0){
                System.out.println(score/norm);
            } else {
                System.out.println(0);
            }
        }
    }

    public static void main(String args[]) throws IOException {
        Combined combined = new Combined();
        combined.testBench();
    }
}
