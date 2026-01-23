import express from 'express';
import Trip from '../models/Trip.js';

const router = express.Router();

// Get all trips
router.get('/', async (req, res) => {
  const trips = await Trip.find().populate('creator joinedUsers');
  res.json(trips);
});

// Create a trip
router.post('/', async (req, res) => {
  const { destination, dates, budget, creator } = req.body;
  const trip = new Trip({ destination, dates, budget, creator });
  await trip.save();
  res.json(trip);
});

export default router;
