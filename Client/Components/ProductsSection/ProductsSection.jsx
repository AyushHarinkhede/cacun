import { useMemo, useState, useEffect } from 'react'
import axios from 'axios'
import './ProductsSection.css'

import { IconBasket, IconHeart } from '../icons.jsx'
import { useSettings } from '../../contexts/useSettings.js'

export default function ProductsSection({ showViewMore = false }) {
  const { likedIds, cartIds, toggleLiked, toggleCart, setActiveProductId } = useSettings()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await axios.get('http://localhost:5000/api/products')
        setProducts(response.data)
        setLoading(false)
      } catch (err) {
        setError(err)
        setLoading(false)
      }
    }

    fetchProducts()
  }, [])

  const displayed = useMemo(() => (showViewMore ? products.slice(0, 5) : products), [products, showViewMore])

  const goShop = () => {
    // Remove scrolling behavior - shop section will appear as overlay
    const shopSection = document.getElementById('shop')
    if (shopSection) {
      shopSection.classList.add('shopSectionVisible')
    }
  }

  if (loading) {
    return <div>Loading...</div>
  }

  if (error) {
    return <div>Error fetching products: {error.message}</div>
  }

  return (
    <section className="prodWrap" id="products" aria-label="Products">
      <div className="prodHeader">
        <div>
          <div className="prodTitle">Products</div>
          <div className="prodSub">Plastic-free • Non-toxic • Recycled • Nature-made</div>
        </div>
        <div className="prodHeaderActions">
          {showViewMore ? (
            <button className="prodMore" type="button" onClick={goShop} data-shop-trigger="true">
              View more
            </button>
          ) : null}
          <div className="prodHint">Scroll</div>
        </div>
      </div>

      <div className="prodRail" role="list">
        {displayed.map((p) => {
          const isLiked = likedIds.has(p._id)
          const inCart = cartIds.has(p._id)

          return (
            <article
              key={p._id}
              className="prodCard"
              role="listitem"
              tabIndex={0}
              onClick={() => setActiveProductId(p._id)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') setActiveProductId(p._id)
              }}
            >
              <div className="prodMedia">
                <img className="prodImg" src={p.images[0]} alt={p.title} />
                <div className="prodBadge">-{p.discountPercent}%</div>
              </div>

              <div className="prodBody">
                <div className="prodName">{p.title}</div>
                <div className="prodCat">{p.category}</div>
                <div className="prodMeta">{p.material}</div>
                <div className="prodBrand">Brand: {p.brand}</div>

                <div className="prodActions">
                  <button
                    className={isLiked ? 'prodIconBtn prodIconBtnActive' : 'prodIconBtn'}
                    type="button"
                    aria-label="Like"
                    onClick={(e) => {
                      e.stopPropagation()
                      toggleLiked(p._id)
                    }}
                  >
                    <IconHeart filled={isLiked} />
                  </button>

                  <button
                    className={inCart ? 'prodAddBtn prodAddBtnActive' : 'prodAddBtn'}
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation()
                      toggleCart(p._id)
                    }}
                  >
                    <IconBasket />
                    {inCart ? 'Added' : 'Add'}
                  </button>
                </div>
              </div>
            </article>
          )
        })}
      </div>
    </section>
  )
}
