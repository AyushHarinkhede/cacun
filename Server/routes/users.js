import express from 'express';
import User from '../models/User.js';
import jwt from 'jsonwebtoken';

const router = express.Router();

// Middleware to verify JWT token
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];
  
  if (!token) {
    return res.status(401).json({ error: 'Access token required' });
  }
  
  jwt.verify(token, process.env.JWT_SECRET || 'your-secret-key', (err, user) => {
    if (err) {
      return res.status(403).json({ error: 'Invalid token' });
    }
    req.user = user;
    next();
  });
};

// Register new user
router.post('/register', async (req, res) => {
  try {
    const { name, email, password, username } = req.body;
    
    // Validation
    if (!name || !email || !password) {
      return res.status(400).json({ error: 'Name, email, and password are required' });
    }
    
    if (password.length < 6) {
      return res.status(400).json({ error: 'Password must be at least 6 characters long' });
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      return res.status(400).json({ error: 'Please enter a valid email address' });
    }
    
    // Check if user already exists
    const existingUser = await User.findOne({ 
      $or: [{ email }, ...(username ? [{ username }] : [])] 
    });
    
    if (existingUser) {
      return res.status(400).json({ 
        error: existingUser.email === email ? 'Email already registered' : 'Username already taken' 
      });
    }
    
    // Create new user
    const user = new User({
      name,
      email,
      password,
      username: username || email.split('@')[0],
    });
    
    await user.save();
    
    // Generate token
    const token = user.generateAuthToken();
    
    res.status(201).json({
      message: 'User registered successfully',
      token,
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        username: user.username,
        avatar: user.name[0].toUpperCase(),
      },
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({ error: 'Failed to register user' });
  }
});

// Login user
router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    
    // Validation
    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password are required' });
    }
    
    // Find user by email
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }
    
    // Check password
    const isMatch = await user.comparePassword(password);
    if (!isMatch) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }
    
    // Generate token
    const token = user.generateAuthToken();
    
    res.json({
      message: 'Login successful',
      token,
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        username: user.username,
        avatar: user.name[0].toUpperCase(),
        bio: user.bio,
        location: user.location,
        profilePicture: user.profilePicture,
        settings: user.settings,
        stats: user.stats,
      },
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ error: 'Failed to login' });
  }
});

// Get current user profile (protected)
router.get('/profile', authenticateToken, async (req, res) => {
  try {
    const user = await User.findById(req.user.id).select('-password');
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    res.json({
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        username: user.username,
        bio: user.bio,
        location: user.location,
        dob: user.dob,
        gender: user.gender,
        profilePicture: user.profilePicture,
        settings: user.settings,
        stats: user.stats,
        reputation: user.reputation,
        createdAt: user.createdAt,
      },
    });
  } catch (error) {
    console.error('Get profile error:', error);
    res.status(500).json({ error: 'Failed to fetch profile' });
  }
});

// Update user profile (protected)
router.put('/profile', authenticateToken, async (req, res) => {
  try {
    const updates = req.body;
    const allowedUpdates = [
      'name', 'username', 'bio', 'location', 'dob', 'gender', 
      'settings', 'profilePicture'
    ];
    
    const actualUpdates = {};
    allowedUpdates.forEach(field => {
      if (updates[field] !== undefined) {
        actualUpdates[field] = updates[field];
      }
    });
    
    // Check if username is already taken (if updating)
    if (actualUpdates.username) {
      const existingUser = await User.findOne({ 
        username: actualUpdates.username,
        _id: { $ne: req.user.id }
      });
      
      if (existingUser) {
        return res.status(400).json({ error: 'Username already taken' });
      }
    }
    
    const user = await User.findByIdAndUpdate(
      req.user.id,
      actualUpdates,
      { new: true, runValidators: true }
    ).select('-password');
    
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    res.json({
      message: 'Profile updated successfully',
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        username: user.username,
        bio: user.bio,
        location: user.location,
        dob: user.dob,
        gender: user.gender,
        profilePicture: user.profilePicture,
        settings: user.settings,
        stats: user.stats,
        reputation: user.reputation,
      },
    });
  } catch (error) {
    console.error('Update profile error:', error);
    res.status(500).json({ error: 'Failed to update profile' });
  }
});

// Change password (protected)
router.put('/change-password', authenticateToken, async (req, res) => {
  try {
    const { currentPassword, newPassword } = req.body;
    
    if (!currentPassword || !newPassword) {
      return res.status(400).json({ error: 'Current and new passwords are required' });
    }
    
    if (newPassword.length < 6) {
      return res.status(400).json({ error: 'New password must be at least 6 characters long' });
    }
    
    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    }
    
    // Verify current password
    const isMatch = await user.comparePassword(currentPassword);
    if (!isMatch) {
      return res.status(401).json({ error: 'Current password is incorrect' });
    }
    
    // Update password
    user.password = newPassword;
    await user.save();
    
    res.json({ message: 'Password changed successfully' });
  } catch (error) {
    console.error('Change password error:', error);
    res.status(500).json({ error: 'Failed to change password' });
  }
});

// OAuth login/register
router.post('/oauth', async (req, res) => {
  try {
    const { provider, providerId, email, name, picture } = req.body;
    
    if (!provider || !providerId || !email || !name) {
      return res.status(400).json({ error: 'Missing OAuth information' });
    }
    
    // Find user by OAuth provider
    let user = await User.findOne({
      'oauthProviders': { $elemMatch: { provider, providerId } }
    });
    
    if (!user) {
      // Check if user exists with same email
      user = await User.findOne({ email });
      
      if (user) {
        // Link OAuth to existing account
        user.oauthProviders.push({ provider, providerId });
        await user.save();
      } else {
        // Create new user
        user = new User({
          name,
          email,
          password: Math.random().toString(36).slice(-8), // Random password
          oauthProviders: [{ provider, providerId }],
          profilePicture: picture || '',
        });
        await user.save();
      }
    }
    
    const token = user.generateAuthToken();
    
    res.json({
      message: 'OAuth login successful',
      token,
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        username: user.username,
        avatar: user.name[0].toUpperCase(),
        profilePicture: user.profilePicture,
      },
    });
  } catch (error) {
    console.error('OAuth error:', error);
    res.status(500).json({ error: 'OAuth login failed' });
  }
});
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
