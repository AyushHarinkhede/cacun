import mongoose from 'mongoose';

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
    email: {
      type: String,
      required: true,
      unique: true,
    },
    password: {
      type: String,
      required: true,
    },
    // public reviews left for this user
    reviews: [ReviewSchema],
    // cached reputation aggregates (optional; can be computed from reviews)
    reputation: {
      behaviorAvg: { type: Number, default: 0 },
      problemSolvingAvg: { type: Number, default: 0 },
      vibeAvg: { type: Number, default: 0 },
      reviewsCount: { type: Number, default: 0 },
    },
  },
  { timestamps: true }
);

// helper to recompute reputation from reviews
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
