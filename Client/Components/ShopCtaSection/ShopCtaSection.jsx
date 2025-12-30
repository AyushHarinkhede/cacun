import './ShopCtaSection.css'

export default function ShopCtaSection() {
  const goShop = () => {
    // Show shop overlay instead of scrolling
    const shopSection = document.getElementById('shop')
    if (shopSection) {
      shopSection.classList.add('shopSectionVisible')
    }
  }

  return (
    <section className="shopCtaWrap" aria-label="Start shopping">
      <div className="shopCtaCard">
        <div className="shopCtaTitle">Start Shopping</div>
        <div className="shopCtaText">
          Explore plastic-free, non-toxic, recycled and nature-made essentials — curated to keep you close to nature.
        </div>
        <div className="shopCtaRow">
          <button className="shopCtaBtn" type="button" onClick={goShop} data-shop-trigger="true">
            Go to Shop
          </button>
          <div className="shopCtaHint">Tap any product to view details</div>
        </div>
      </div>
    </section>
  )
}
