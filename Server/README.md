# Cacun Server

Backend API for the Cacun nature-first e-commerce platform.

## Setup

1. Install dependencies:
```bash
npm install
```

2. Create `.env` file from `.env.example`:
```bash
cp .env.example .env
```

3. Update `.env` with your configuration:
- MongoDB connection string
- JWT secret
- Cloudinary credentials (for image uploads)

4. Start the server:
```bash
npm run dev
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Users
- `GET /api/users/profile` - Get user profile (protected)
- `PUT /api/users/profile` - Update user profile (protected)
- `POST /api/users/avatar` - Upload avatar image (protected)

### Products
- `GET /api/products` - Get all products
- `GET /api/products/:id` - Get single product
- `POST /api/products` - Create product (admin)
- `PUT /api/products/:id` - Update product (admin)
- `POST /api/products/:id/reviews` - Add product review

## Database Models

### User
- name, email, password
- dob, address, mobile, personalInfo
- avatar (Cloudinary URL)

### Product
- title, category, brand, material
- price, discountPercent
- images, description, environmentalBenefit
- reviews (rating, comment, user)

## Features

- JWT authentication
- Password hashing with bcrypt
- Image uploads with Cloudinary
- MongoDB with Mongoose
- CORS enabled
- Error handling
