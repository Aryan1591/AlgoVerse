const express = require('express');
const SyncService = require('./services/syncService');

const router = express.Router();

// POST endpoint to trigger LeetCode sync
router.post('/sync', async (req, res) => {
  try {
    const { userId, username, cookie } = req.body;

    // Validate required parameters
    if (!userId || !username || !cookie) {
      const missingParams = [];
      if (!userId) missingParams.push('userId');
      if (!username) missingParams.push('username');
      if (!cookie) missingParams.push('cookie');
      return res.status(400).json({ message: `Missing required parameters: ${missingParams.join(', ')}` });
    }

    console.log(`Received sync request for user: ${username}`);

    // Respond immediately with 202 Accepted
    res.status(202).json({ message: 'Sync request accepted', userId, username });

    // Perform async processing
    setImmediate(async () => {
      try {
        await SyncService.syncLeetCodeData(userId, username, cookie);
        console.log(`Async sync completed for user: ${username}`);
      } catch (error) {
        console.error(`Async sync failed for user: ${username}:`, error.message);
      }
    });
  } catch (error) {
    console.error('Error handling sync request:', error);
    res.status(500).json({ message: 'Internal server error' });
  }
});

module.exports = router;