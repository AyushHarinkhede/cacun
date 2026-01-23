import express from 'express';
import Trip from '../models/Trip.js';
import User from '../models/User.js';

const router = express.Router();

// Create a trip
router.post('/', async (req, res) => {
  try {
    const { destination, startDate, endDate, budget, creator } = req.body;
    const trip = await Trip.create({ destination, startDate, endDate, budget, creator, joinedUsers: [] });
    res.status(201).json(trip);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to create trip' });
  }
});

// Join a trip
router.post('/:id/join', async (req, res) => {
  try {
    const trip = await Trip.findById(req.params.id);
    if (!trip) return res.status(404).json({ error: 'Trip not found' });

    const { userId } = req.body;
    if (!userId) return res.status(400).json({ error: 'userId required' });

    if (trip.joinedUsers.includes(userId)) return res.status(400).json({ error: 'Already joined' });

    trip.joinedUsers.push(userId);
    await trip.save();
    res.json(trip);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to join trip' });
  }
});

// Submit a review for a participant after trip end
router.post('/:id/review', async (req, res) => {
  try {
    const trip = await Trip.findById(req.params.id).populate('creator joinedUsers');
    if (!trip) return res.status(404).json({ error: 'Trip not found' });

    const { reviewerId, revieweeId, behavior, problemSolving, vibe, feedback } = req.body;

    if (!reviewerId || !revieweeId) return res.status(400).json({ error: 'reviewerId and revieweeId required' });

    // ensure trip has ended
    const now = new Date();
    if (new Date(trip.endDate) > now) return res.status(400).json({ error: 'Cannot review before trip ends' });

    // ensure both reviewer and reviewee participated in the trip (creator or joinedUsers)
    const participated = (userId) => {
      if (!userId) return false;
      if (trip.creator && trip.creator._id.toString() === userId) return true;
      return trip.joinedUsers.some((u) => u._id.toString() === userId || u.toString() === userId);
    };

    if (!participated(reviewerId) || !participated(revieweeId)) {
      return res.status(403).json({ error: 'Both reviewer and reviewee must have participated in the trip' });
    }

    if (reviewerId === revieweeId) return res.status(400).json({ error: 'Cannot review yourself' });

    // append review into reviewee's profile
    const reviewee = await User.findById(revieweeId);
    if (!reviewee) return res.status(404).json({ error: 'Reviewee not found' });

    reviewee.reviews.push({ reviewer: reviewerId, trip: trip._id, behavior, problemSolving, vibe, feedback });
    reviewee.recomputeReputation();
    await reviewee.save();

    res.status(201).json({ message: 'Review added', review: reviewee.reviews[reviewee.reviews.length - 1] });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to submit review' });
  }
});

// Get trip details
router.get('/:id', async (req, res) => {
  try {
    const trip = await Trip.findById(req.params.id).populate('creator joinedUsers');
    if (!trip) return res.status(404).json({ error: 'Trip not found' });
    res.json(trip);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to fetch trip' });
  }
});

export default router;
