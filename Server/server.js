import express from 'express'
import cors from 'cors'
import dotenv from 'dotenv'
import mongoose from 'mongoose'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'

// Import routes
import authRoutes from './routes/auth.js'
import userRoutes from './routes/user.js'
import productRoutes from './routes/product.js'

// Load environment variables
dotenv.config()

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const app = express()
const PORT = process.env.PORT || 5000

// Middleware
app.use(cors({
  origin: ['http://localhost:5173', 'http://127.0.0.1:5173'],
  credentials: true
}))
app.use(express.json({ limit: '10mb' }))
app.use(express.urlencoded({ extended: true, limit: '10mb' }))

// Serve static files
app.use('/uploads', express.static(join(__dirname, 'uploads')))

// Database connection
const connectDB = async () => {
  try {
    const conn = await mongoose.connect(process.env.MONGODB_URI || 'mongodb://localhost:27017/cacun')
    console.log('✅ Connected to MongoDB')
    console.log(`📊 Database: ${conn.connection.name}`)
  } catch (err) {
    console.error('❌ MongoDB connection error:', err)
    process.exit(1)
  }
}

// Routes
app.use('/api/auth', authRoutes)
app.use('/api/users', userRoutes)
app.use('/api/products', productRoutes)

// Health check endpoint
app.get('/api/health', (req, res) => {
  res.json({ 
    status: 'OK', 
    message: 'Cacun server is running',
    timestamp: new Date().toISOString(),
    environment: process.env.NODE_ENV || 'development'
  })
})

// API documentation endpoint
app.get('/api', (req, res) => {
  res.json({
    message: 'Cacun API',
    version: '1.0.0',
    endpoints: {
      auth: '/api/auth',
      users: '/api/users',
      products: '/api/products',
      health: '/api/health'
    },
    documentation: 'https://github.com/AyushHarinkhede/cacun'
  })
})

// Error handling middleware
app.use((err, req, res, _next) => {
  console.error('❌ Error:', {
    message: err.message,
    stack: err.stack,
    url: req.url,
    method: req.method,
    timestamp: new Date().toISOString()
  })
  
  // Mongoose validation error
  if (err.name === 'ValidationError') {
    const errors = Object.values(err.errors).map(e => e.message)
    return res.status(400).json({
      error: 'Validation Error',
      details: errors
    })
  }
  
  // Mongoose duplicate key error
  if (err.code === 11000) {
    return res.status(400).json({
      error: 'Duplicate Entry',
      message: 'This resource already exists'
    })
  }
  
  // JWT error
  if (err.name === 'JsonWebTokenError') {
    return res.status(401).json({
      error: 'Invalid Token',
      message: 'Please provide a valid authentication token'
    })
  }
  
  // Default error
  res.status(err.status || 500).json({
    error: err.message || 'Something went wrong!',
    ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
  })
})

// 404 handler
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Route not found',
    message: `Cannot ${req.method} ${req.originalUrl}`,
    availableEndpoints: ['/api/auth', '/api/users', '/api/products', '/api/health', '/api']
  })
})

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('🔄 SIGTERM received, shutting down gracefully...')
  mongoose.connection.close(() => {
    console.log('✅ MongoDB connection closed')
    process.exit(0)
  })
})

process.on('SIGINT', () => {
  console.log('🔄 SIGINT received, shutting down gracefully...')
  mongoose.connection.close(() => {
    console.log('✅ MongoDB connection closed')
    process.exit(0)
  })
})

// Start server
const startServer = async () => {
  await connectDB()
  
  app.listen(PORT, () => {
    console.log(`🚀 Cacun server running on port ${PORT}`)
    console.log(`🌐 Environment: ${process.env.NODE_ENV || 'development'}`)
    console.log(`📍 API URL: http://localhost:${PORT}/api`)
    console.log(`🏥 Health Check: http://localhost:${PORT}/api/health`)
  })
}

// Handle unhandled promise rejections
process.on('unhandledRejection', (err) => {
  console.error('❌ Unhandled Promise Rejection:', err)
  process.exit(1)
})

// Handle uncaught exceptions
process.on('uncaughtException', (err) => {
  console.error('❌ Uncaught Exception:', err)
  process.exit(1)
})

startServer().catch(console.error)
