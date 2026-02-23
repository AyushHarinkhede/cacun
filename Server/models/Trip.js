import mongoose from 'mongoose';

const TripSchema = new mongoose.Schema(
  {
    destination: { type: String, required: true },
    startDate: { type: Date, required: true },
    endDate: { type: Date, required: true },
    budget: { type: Number, default: 0 },
    creator: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    joinedUsers: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
  },
  { timestamps: true }
);

const Trip = mongoose.model('Trip', TripSchema);

export default Trip;
