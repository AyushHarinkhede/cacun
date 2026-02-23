import mongoose from 'mongoose';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

const ReviewSchema = new mongoose.Schema(
  {
    reviewer: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    trip: { type: mongoose.Schema.Types.ObjectId, ref: 'Trip', required: true },
    behavior: { type: Number, min: 0, max: 5, required: true },
    problemSolving: { type: Number, min: 0, max: 5, required: true },
    vibe: { type: Number, min: 0, max: 5, required: true },
    feedback: { type: String, default: '' },
  },
  { timestamps: true }
);

const userSchema = new mongoose.Schema(
  {
    name: {
      type: String,
      required: true,
    },
    username: {
      type: String,
      unique: true,
      sparse: true,
    },
    email: {
      type: String,
      required: true,
      unique: true,
      lowercase: true,
      trim: true,
    },
    password: {
      type: String,
      required: true,
      minlength: 6,
    },
    bio: {
      type: String,
      default: '',
    },
    location: {
      type: String,
      default: '',
    },
    dob: {
      type: Date,
    },
    gender: {
      type: String,
      enum: ['male', 'female', 'other', 'prefer-not-to-say'],
      default: 'prefer-not-to-say',
    },
    profilePicture: {
      type: String,
      default: '',
    },
    // Profile settings
    settings: {
      incognito: { type: Boolean, default: false },
      textSize: { type: String, enum: ['small', 'medium', 'large'], default: 'medium' },
      darkMode: { type: Boolean, default: false },
      notifications: { type: Boolean, default: true },
      locationSharing: { type: Boolean, default: true },
      showOnlineStatus: { type: Boolean, default: true },
    },
    // OAuth providers
    oauthProviders: [{
      provider: { type: String, enum: ['google', 'facebook', 'apple', 'microsoft'] },
      providerId: String,
    }],
    // public reviews left for this user
    reviews: [ReviewSchema],
    // cached reputation aggregates (optional; can be computed from reviews)
    reputation: {
      behaviorAvg: { type: Number, default: 0 },
      problemSolvingAvg: { type: Number, default: 0 },
      vibeAvg: { type: Number, default: 0 },
      reviewsCount: { type: Number, default: 0 },
    },
    // Stats
    stats: {
      tripsCompleted: { type: Number, default: 0 },
      totalCountries: { type: Number, default: 0 },
      reliabilityScore: { type: Number, default: 95 },
    },
  },
  { timestamps: true }
);

// Hash password before saving
userSchema.pre('save', async function (next) {
  if (!this.isModified('password')) return next();
  
  try {
    const salt = await bcrypt.genSalt(10);
    this.password = await bcrypt.hash(this.password, salt);
    next();
  } catch (error) {
    next(error);
  }
});

// Method to compare password
userSchema.methods.comparePassword = async function (candidatePassword) {
  return await bcrypt.compare(candidatePassword, this.password);
};

// Method to generate JWT token
userSchema.methods.generateAuthToken = function () {
  return jwt.sign(
    { id: this._id, email: this.email },
    process.env.JWT_SECRET || 'your-secret-key',
    { expiresIn: '30d' }
  );
};

// Helper to recompute reputation from reviews
userSchema.methods.recomputeReputation = function () {
  if (!this.reviews || this.reviews.length === 0) {
    this.reputation = { behaviorAvg: 0, problemSolvingAvg: 0, vibeAvg: 0, reviewsCount: 0 };
    return this.reputation;
  }

  const count = this.reviews.length;
  const sums = this.reviews.reduce(
    (acc, r) => {
      acc.behavior += r.behavior;
      acc.problemSolving += r.problemSolving;
      acc.vibe += r.vibe;
      return acc;
    },
    { behavior: 0, problemSolving: 0, vibe: 0 }
  );

  this.reputation = {
    behaviorAvg: +(sums.behavior / count).toFixed(2),
    problemSolvingAvg: +(sums.problemSolving / count).toFixed(2),
    vibeAvg: +(sums.vibe / count).toFixed(2),
    reviewsCount: count,
  };

  return this.reputation;
};

const User = mongoose.model('User', userSchema);

export default User;
