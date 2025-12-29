import mongoose from 'mongoose'

const productSchema = new mongoose.Schema({
  title: { type: String, required: true },
  category: { type: String, required: true },
  brand: { type: String, required: true },
  material: { type: String, required: true },
  price: { type: Number, required: true },
  discountPercent: { type: Number },
  images: [{ type: String }],
  description: { type: String },
  environmentalBenefit: { type: String },
  reviews: [{
    rating: { type: Number, min: 1, max: 5 },
    comment: { type: String },
    user: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    createdAt: { type: Date, default: Date.now }
  }],
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
})

export default mongoose.model('Product', productSchema)
