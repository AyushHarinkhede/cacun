import express from 'express';
import User from '../models/User.js';

const router = express.Router();

// Get user profile with reviews
router.get('/:id', async (req, res) => {
  const user = await User.findById(req.params.id).populate('reputation.reviewer reputation.trip');
  res.json(user);
});

// Create user
router.post('/', async (req, res) => {
  const { username } = req.body;
  const user = new User({ username });
  await user.save();
  res.json(user);
});

export default router;
