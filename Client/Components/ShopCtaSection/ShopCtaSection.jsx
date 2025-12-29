import './ShopCtaSection.css'

export default function ShopCtaSection() {
  const goShop = () => {
    const el = document.getElementById('shop')
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <section className="shopCtaWrap" aria-label="Start shopping">
      <div className="shopCtaCard">
        <div className="shopCtaTitle">Start Shopping</div>
        <div className="shopCtaText">
          Explore plastic-free, non-toxic, recycled and nature-made essentials — curated to keep you close to nature.
        </div>
        <div className="shopCtaRow">
          <button className="shopCtaBtn" type="button" onClick={goShop}>
            Go to Shop
          </button>
          <div className="shopCtaHint">Tap any product to view details</div>
        </div>
      </div>
    </section>
  )
}
