import { useMemo } from 'react'
import './ProductModal.css'

import { products } from '../../data/products.js'
import { useSettings } from '../../contexts/useSettings.js'
import { IconBasket, IconHeart, IconX } from '../icons.jsx'

export default function ProductModal() {
  const { activeProductId, setActiveProductId, likedIds, cartIds, toggleLiked, toggleCart } = useSettings()

  const product = useMemo(() => products.find((p) => p.id === activeProductId) || null, [activeProductId])
  const open = Boolean(product)

  if (!open) return null

  const isLiked = likedIds.has(product.id)
  const inCart = cartIds.has(product.id)

  return (
    <div className="pmOverlay pmOverlayOpen" role="dialog" aria-modal="true" aria-label="Product details">
      <button className="pmBackdrop" type="button" aria-label="Close product details" onClick={() => setActiveProductId(null)} />

      <div className="pmCard">
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
              onClick={() => toggleLiked(product.id)}
              aria-label="Like"
            >
              <IconHeart filled={isLiked} />
              {isLiked ? 'Liked' : 'Like'}
            </button>

            <button
              className={inCart ? 'pmPrimary pmPrimaryActive' : 'pmPrimary'}
              type="button"
              onClick={() => toggleCart(product.id)}
            >
              <IconBasket />
              {inCart ? 'Added' : 'Add to basket'}
            </button>
          </div>

          <div className="pmFoot">
            <div className="pmHint">Reviews, NGO link, and comparisons can be added next.</div>
          </div>
        </div>
      </div>
    </div>
  )
}
