import { useState } from 'react'
import './ProductCard.css'

import { IconHeart, IconShoppingBag, IconStar, IconLeaf, IconRecycle } from '../icons.jsx'

export default function ProductCard({ product, onViewDetails, onAddToCart, onToggleLike }) {
  const [isLiked, setIsLiked] = useState(product.liked || false)
  const [imageLoaded, setImageLoaded] = useState(false)
  const [isAdding, setIsAdding] = useState(false)

  const handleLike = (e) => {
    e.stopPropagation()
    setIsLiked(!isLiked)
    onToggleLike?.(product.id, !isLiked)
  }

  const handleAddToCart = async (e) => {
    e.stopPropagation()
    setIsAdding(true)
    await onAddToCart?.(product)
    setTimeout(() => setIsAdding(false), 1000)
  }

  const handleViewDetails = () => {
    onViewDetails?.(product)
  }

  const getEcoIcon = (category) => {
    switch (category) {
      case 'plastic-free':
        return <IconLeaf />
      case 'recycled':
        return <IconRecycle />
      case 'non-toxic':
        return <IconHeart />
      default:
        return <IconLeaf />
    }
  }

  const getEcoBadge = (category) => {
    const badges = {
      'plastic-free': { color: 'green', label: 'Plastic Free' },
      'recycled': { color: 'blue', label: 'Recycled' },
      'non-toxic': { color: 'purple', label: 'Non-Toxic' },
      'nature': { color: 'green', label: 'Nature Product' },
      'reuse': { color: 'teal', label: 'Reusable' }
    }
    return badges[category] || { color: 'gray', label: 'Eco-Friendly' }
  }

  const ecoBadge = getEcoBadge(product.category)

  return (
    <div className="productCard" onClick={handleViewDetails}>
      <div className="productImageContainer">
        <img
          src={product.image}
          alt={product.name}
          className={`productImage ${imageLoaded ? 'loaded' : ''}`}
          onLoad={() => setImageLoaded(true)}
        />
        {!imageLoaded && (
          <div className="imageSkeleton">
            <div className="skeletonShimmer" />
          </div>
        )}
        
        <div className="productOverlay">
          <button
            className={`likeBtn ${isLiked ? 'liked' : ''}`}
            onClick={handleLike}
            aria-label={isLiked ? 'Remove from favorites' : 'Add to favorites'}
          >
            <IconHeart />
          </button>
          
          <div className="ecoBadge">
            {getEcoIcon(product.category)}
            <span>{ecoBadge.label}</span>
          </div>
        </div>
      </div>

      <div className="productContent">
        <div className="productHeader">
          <h3 className="productName">{product.name}</h3>
          <div className="productRating">
            <IconStar />
            <span>{product.rating || '4.5'}</span>
            <span className="ratingCount">({product.reviews || '23'})</span>
          </div>
        </div>

        <p className="productDescription">{product.description}</p>
        
        <div className="productMeta">
          <div className="productPrice">
            <span className="currentPrice">${product.price}</span>
            {product.originalPrice && (
              <span className="originalPrice">${product.originalPrice}</span>
            )}
          </div>
          
          {product.ecoScore && (
            <div className="ecoScore">
              <span className="scoreLabel">Eco Score</span>
              <span className={`scoreValue ${product.ecoScore.toLowerCase()}`}>
                {product.ecoScore}
              </span>
            </div>
          )}
        </div>

        <div className="productActions">
          <button
            className={`addToCartBtn ${isAdding ? 'adding' : ''}`}
            onClick={handleAddToCart}
            disabled={isAdding}
          >
            {isAdding ? (
              <>
                <div className="spinner" />
                Adding...
              </>
            ) : (
              <>
                <IconShoppingBag />
                Add to Cart
              </>
            )}
          </button>
          
          <button
            className="quickViewBtn"
            onClick={handleViewDetails}
            aria-label="Quick view"
          >
            Quick View
          </button>
        </div>
      </div>
    </div>
  )
}
