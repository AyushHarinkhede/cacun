import mongoose from 'mongoose';

const TripSchema = new mongoose.Schema({
  destination: { type: String, required: true },
  dates: { type: String, required: true },
  budget: { type: Number },
  creator: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
  joinedUsers: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
});

export default mongoose.model('Trip', TripSchema);