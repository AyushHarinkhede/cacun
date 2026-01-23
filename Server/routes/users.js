import express from 'express';
import User from '../models/User.js';

const router = express.Router();

// Get public profile including reviews and computed reputation
router.get('/:id', async (req, res) => {
  try {
    const user = await User.findById(req.params.id).populate({
      path: 'reviews.reviewer',
      select: 'name',
    });
    if (!user) return res.status(404).json({ error: 'User not found' });

    // recompute reputation on the fly
    user.recomputeReputation();

    await user.save();

    res.json({
      id: user._id,
      name: user.name,
      reputation: user.reputation,
      reviews: user.reviews.map((r) => ({
        id: r._id,
        reviewer: r.reviewer ? { id: r.reviewer._id, name: r.reviewer.name } : { id: r.reviewer },
        trip: r.trip,
        behavior: r.behavior,
        problemSolving: r.problemSolving,
        vibe: r.vibe,
        feedback: r.feedback,
        createdAt: r.createdAt,
      })),
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Failed to fetch user profile' });
  }
});

export default router;
