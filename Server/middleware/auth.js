// Authentication middleware placeholder
export const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ error: 'Access token required' });
  }

  try {
    // Verify token here
    next();
  } catch (error) {
    return res.status(403).json({ error: 'Invalid token' });
  }
};

export default authenticateToken;
