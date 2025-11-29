const express = require('express');
const syncController = require('./routes/syncController');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware to parse JSON requests
app.use(express.json());

// Routes
app.use('/api', syncController);

// Start the server
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});