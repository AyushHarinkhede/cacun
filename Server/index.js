// server/index.js

const express = require('express');
const cors = require('cors');
const app = express();

// Middleware
app.use(cors()); // React ko access dene ke liye
app.use(express.json()); // JSON data samajhne ke liye

// Ek Test Route banate hain
app.get('/', (req, res) => {
    res.send("Hello from Cacun Backend!");
});

// Server Start karna
const PORT = 5000;
app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
});
