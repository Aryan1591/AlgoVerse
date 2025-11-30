const { ObjectId } = require('mongodb');
const { getDb } = require('../config/database');
const leetcodeService = require('./leetCodeService');

// Define category as an enum to match Spring Boot Problem entity
const CATEGORY = {
  EASY: 'EASY',
  MEDIUM: 'MEDIUM',
  HARD: 'HARD'
};

class SyncService {
  
  async syncLeetCodeData(userId, username, cookie) {
    const db = getDb();
    const usersCollection = db.collection('users');
    const userProblemsCollection = db.collection('user_problems');
    const problemsCollection = db.collection('problems');

    try {
      console.log(`Starting sync for user: ${username}`);
      
      // Step 1: Fetch data from LeetCode
      const { solvedProblems } = 
        await leetcodeService.fetchAllAcceptedSubmissions(username, cookie);
     
      // Step 2: Fetch platform problems and process solved problems
      const platformProblems = await problemsCollection.find({}).toArray();
      const platformProblemsMap = new Map(
        platformProblems.map(problem => [problem.title, problem.category])
      );

      const difficultyCounts = { [CATEGORY.EASY]: 0, [CATEGORY.MEDIUM]: 0, [CATEGORY.HARD]: 0 };
      const filteredSolvedProblems = solvedProblems.filter(problem => {
        const category = platformProblemsMap.get(problem.problemName);
        if (category) {
          difficultyCounts[category]++;
          return true;
        }
        return false;
      });

      console.log(`Matched ${filteredSolvedProblems.length} solved problems with platform database`);
      console.log(`Difficulty counts: Easy=${difficultyCounts.EASY}, Medium=${difficultyCounts.MEDIUM}, Hard=${difficultyCounts.HARD}`);
      
      // Step 3: Store solved problems in database
      if (filteredSolvedProblems.length > 0) {
        await this.storeSolvedProblems(userId, username, filteredSolvedProblems);
      } else {
        console.log(`No solved problems to store for user: ${username}`);
      }
      
      // Step 4: Update user stats
      await usersCollection.updateOne(
        { _id: new ObjectId(userId) },
        {
          $set: {
            syncStatus: 'COMPLETED',
            lastSyncedAt: new Date(),
            'stats.totalSolved': filteredSolvedProblems.length,
            'stats.easySolved': difficultyCounts[EASY],
            'stats.mediumSolved': difficultyCounts[MEDIUM],
            'stats.hardSolved': difficultyCounts[HARD],
            updatedAt: new Date()
          }
        }
      );
      
      console.log(`Sync completed for user: ${username}`);
      
    } catch (error) {
      console.error(`Sync failed for user: ${username}:`, error.message);
      
      // Update status to FAILED
      await usersCollection.updateOne(
        { _id: new ObjectId(userId) },
        {
          $set: {
            syncStatus: 'FAILED',
            updatedAt: new Date()
          }
        }
      ).catch(dbError => {
        console.error(`Failed to update sync status for user: ${username}:`, dbError.message);
      });
      
      throw error;
    }
  }
  
  async storeSolvedProblems(userId, username, solvedProblems) {
    const db = getDb();
    const userProblemsCollection = db.collection('user_problems');
    
    console.log(`About to store/update ${solvedProblems.length} solved problems for user: ${username}`);
    
    // Bulk upsert operation for performance
    // (upsert = update if exists, insert if doesn't exist)
    const bulkOps = solvedProblems.map(problem => ({
      updateOne: {
        filter: { 
          userId: new ObjectId(userId), 
          problemName: problem.problemName // Using unique index on userId + problemName
        },
        update: {
          $set: {
            userId: new ObjectId(userId),
            problemSlug: problem.problemSlug,
            problemName: problem.problemName,
            solvedAt: problem.solvedAt,
            language: problem.lang,
            updatedAt: new Date()
          },
          $setOnInsert: {
            createdAt: new Date()
          }
        },
        upsert: true
      }
    }));
    
    if (bulkOps.length > 0) {
      const result = await userProblemsCollection.bulkWrite(bulkOps);
      console.log(`Successfully updated/inserted solved problems for user: ${username}`);
    }
  }
}

// Export a single instance (singleton pattern)
module.exports = new SyncService();