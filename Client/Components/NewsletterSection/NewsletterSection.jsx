import './NewsletterSection.css'

export default function NewsletterSection() {
  return (
    <section className="newsWrap" id="newsletter" aria-label="Newsletter">
      <div className="newsInner">
        <div className="newsCard">
          <div className="newsTitle">Newsletter</div>
          <div className="newsText">
            Get weekly drops of new eco-inventions, plastic-free launches, and NGO campaigns.
          </div>
          <div className="newsRow">
            <input className="newsInput" placeholder="Email" aria-label="Newsletter email" />
            <button className="newsBtn" type="button">Subscribe</button>
          </div>
        </div>
      </div>
    </section>
  )
}
