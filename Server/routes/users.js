import express from 'express';

const router = express.Router();

// Placeholder routes for users
router.get('/', (req, res) => {
  res.json({ message: 'Get all users' });
});

router.post('/', (req, res) => {
  res.json({ message: 'Create user' });
});

export default router;
