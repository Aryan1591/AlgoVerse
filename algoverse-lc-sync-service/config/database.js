const { MongoClient } = require('mongodb');

let db;
let mongoClient;

async function connectToDatabase() {
  try {
    // Create MongoDB client
    mongoClient = new MongoClient(process.env.MONGODB_URI);
    
    // Connect to MongoDB
    await mongoClient.connect();
    
    // Get database
    db = mongoClient.db(process.env.DB_NAME || 'leetcode_platform');
    
    console.log('Connected to MongoDB Atlas');
    return db;
    
  } catch (error) {
    console.error('MongoDB connection error:', error);
    throw error;
  }
}

function getDb() {
  if (!db) {
    throw new Error('Database not initialized. Call connectToDatabase first.');
  }
  return db;
}

async function closeDatabase() {
  if (mongoClient) {
    await mongoClient.close();
    console.log('Database connection closed');
  }
}

// Export functions so other files can use them
module.exports = {
  connectToDatabase,
  getDb,
  closeDatabase
};