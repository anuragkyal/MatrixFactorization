Compile: javac Recommender.java
Run: java Recommender

Inputs: 
1. Type of method: User Based/Item Based/Combined
2. Type of similarity: Jaccard/Pearson/Cosine
3. Path for ratings - Please provide the complete path
4. Path for test - Please provide the complete path

Output:
Several lines, each line containting the rating for each pair of user/item in the test file.

Sample Execution:

lawn-128-61-118-245:src anurag$ java Recommender
1 for user based, 2 for item based, 3 for combined: 2
1 for Jaccard, 2 for Pearson, 3 for Cosine: 2
Enter path of the train file: /Users/anurag/study/SEM-2/WEB/Assignments/assignment3/src/raw_files/ratings.csv
Enter path of the test file: /Users/anurag/study/SEM-2/WEB/Assignments/assignment3/src/raw_files/toBeRated.csv

