import { useState, useEffect } from 'react'
import axios from 'axios'
import './ProductModal.css'

import { useSettings } from '../../contexts/useSettings.js'
import { IconBasket, IconHeart, IconX } from '../icons.jsx'

export default function ProductModal() {
  const { activeProductId, setActiveProductId, likedIds, cartIds, toggleLiked, toggleCart } = useSettings()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!activeProductId) {
      setProduct(null)
      return
    }

    const fetchProduct = async () => {
      setLoading(true)
      try {
        const response = await axios.get(`http://localhost:5000/api/products/${activeProductId}`)
        setProduct(response.data)
        setLoading(false)
      } catch (err) {
        setError(err)
        setLoading(false)
      }
    }

    fetchProduct()
  }, [activeProductId])

  const open = Boolean(product)

  if (!open && !loading) return null

  const isLiked = product && likedIds.has(product._id)
  const inCart = product && cartIds.has(product._id)

  return (
    <div className="pmOverlay pmOverlayOpen" role="dialog" aria-modal="true" aria-label="Product details">
      <button className="pmBackdrop" type="button" aria-label="Close product details" onClick={() => setActiveProductId(null)} />

      <div className="pmCard">
        {loading && <div className="pmLoading">Loading...</div>}
        {error && <div className="pmError">Error: {error.message}</div>}
        {product && (
          <>
            <div className="pmHead">
              <div>
                <div className="pmTitle">{product.title}</div>
                <div className="pmMeta">{product.category} • {product.material}</div>
              </div>
              <button className="pmClose" type="button" onClick={() => setActiveProductId(null)} aria-label="Close">
                <IconX />
              </button>
            </div>

            <div className="pmBody">
              <div className="pmMedia">
                <img className="pmImg" src={product.images[0]} alt={product.title} />
                <div className="pmDiscount">-{product.discountPercent}%</div>
              </div>

              <div className="pmInfo">
                <div className="pmRow">
                  <div className="pmLabel">Brand</div>
                  <div className="pmValue">{product.brand}</div>
                </div>
                <div className="pmRow">
                  <div className="pmLabel">Why it matters</div>
                  <div className="pmValue">
                    This product is curated for nature-first living. It helps reduce plastic use, avoids harmful chemicals, and supports cleaner consumption.
                  </div>
                </div>
                <div className="pmRow">
                  <div className="pmLabel">Nature impact</div>
                  <div className="pmValue">
                    Compared to regular products, this reduces waste and toxicity. Pair it with campaigns & NGO partners to increase real impact.
                  </div>
                </div>
              </div>

              <div className="pmActions">
                <button
                  className={isLiked ? 'pmIcon pmIconActive' : 'pmIcon'}
                  type="button"
                  onClick={() => toggleLiked(product._id)}
                  aria-label="Like"
                >
                  <IconHeart filled={isLiked} />
                  {isLiked ? 'Liked' : 'Like'}
                </button>

                <button
                  className={inCart ? 'pmPrimary pmPrimaryActive' : 'pmPrimary'}
                  type="button"
                  onClick={() => toggleCart(product._id)}
                >
                  <IconBasket />
                  {inCart ? 'Added' : 'Add to basket'}
                </button>
              </div>

              <div className="pmFoot">
                <div className="pmHint">Reviews, NGO link, and comparisons can be added next.</div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
