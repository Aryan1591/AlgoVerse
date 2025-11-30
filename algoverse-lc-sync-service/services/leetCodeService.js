const { LeetCode, Credential } = require('leetcode-query');

class LeetCodeService {
  
  async fetchAllAcceptedSubmissions(username, cookie) {
    try {
      console.log(`Starting to fetch LeetCode submissions for user: ${username}`);
      
      // Initialize LeetCode API with user's cookie
      const credential = new Credential();
      await credential.init(cookie);
      const leetcode = new LeetCode(credential);
      
      // Map to store unique solved problems (using problemSlug as key)
      const solvedProblems = new Map();
      
      let offset = 0;
      const limit = 100; // Fetch 100 submissions per request
      let totalSubmissions = 0;
      
      // Keep fetching until no more submissions
      while (true) {
        const submissions = await leetcode.submissions({ limit, offset });
        
        // If no submissions returned, we've reached the end
        if (!submissions || submissions.length === 0) {
          break;
        }
        
        totalSubmissions += submissions.length;
        
        // Process each submission
        submissions.forEach(sub => {
          // Filter Accepted submissions
          if (sub.statusDisplay === "Accepted") {
            const slug = sub.titleSlug;
            
            // Only store the first (most recent) accepted submission per problem
            if (!solvedProblems.has(slug)) {
              solvedProblems.set(slug, {
                problemSlug: slug,
                problemName: sub.title,
                solvedAt: new Date(sub.timestamp),
                language: sub.lang
              });
            }
          }
        });
        
        console.log(`Finished fetching for offset and limit: ${offset}, ${limit}. Total submissions so far: ${totalSubmissions}`);
        
        // Move to next page
        offset += limit;
        
        // Small delay to avoid rate limiting
        await new Promise(resolve => setTimeout(resolve, 1000));
      }
      
      console.log(`Completed fetching submissions for user: ${username}. Total accepted problems: ${solvedProblems.size}`);
      
      // Return as array instead of Map
      return {
        solvedProblems: Array.from(solvedProblems.values()),
        totalSubmissions: totalSubmissions
      };
      
    } catch (error) {
      console.error(`Error fetching LeetCode data for user ${username}:`, error);
      throw new Error(`Failed to fetch LeetCode data: ${error.message}`);
    }
  }
}

// Export a single instance (singleton pattern)
module.exports = new LeetCodeService();