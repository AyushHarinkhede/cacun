import mongoose from 'mongoose';

const ReviewSchema = new mongoose.Schema({
  behavior: { type: Number, min: 1, max: 5 },
  problemSolving: { type: Number, min: 1, max: 5 },
  vibeCheck: { type: Number, min: 1, max: 5 },
  feedback: { type: String },
  reviewer: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
  trip: { type: mongoose.Schema.Types.ObjectId, ref: 'Trip' },
  date: { type: Date, default: Date.now }
});

const UserSchema = new mongoose.Schema({
  username: { type: String, required: true, unique: true },
  reputation: [ReviewSchema],
  // ...other fields
});

export default mongoose.model('User', UserSchema);