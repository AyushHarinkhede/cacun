import express from 'express';
import mongoose from 'mongoose';
import cors from 'cors';

const app = express();
app.use(cors());
app.use(express.json());

mongoose.connect('mongodb://localhost:27017/cacun', {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

app.get('/', (req, res) => {
  res.send('Cacun API Running');
});

// TODO: Add routes for users, trips, reviews
import userRoutes from './routes/user.js';
import tripRoutes from './routes/trip.js';
import reviewRoutes from './routes/review.js';

app.use('/api/users', userRoutes);
app.use('/api/trips', tripRoutes);
app.use('/api/reviews', reviewRoutes);

app.listen(5000, () => {
  console.log('Server running on port 5000');
});
