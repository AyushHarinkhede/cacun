import './Footer.css'

import logo from '../cacun.png'

export default function Footer() {
  return (
    <footer className="footerWrap">
      <div className="footerInner">
        <div className="footerBrand">
          <img className="footerLogo" src={logo} alt="cacun" />
          <div>
            <div className="footerName">cacun</div>
            <div className="footerTag">Nature-first • Plastic-free • Non-toxic</div>
          </div>
        </div>

        <div className="footerCols">
          <div className="footerCol">
            <div className="footerTitle">Explore</div>
            <a className="footerLink" href="#home">Home</a>
            <a className="footerLink" href="#">Campaigns</a>
            <a className="footerLink" href="#">NGOs</a>
            <a className="footerLink" href="#">Products</a>
          </div>
          <div className="footerCol">
            <div className="footerTitle">Help</div>
            <a className="footerLink" href="#">Contact</a>
            <a className="footerLink" href="#">Shipping</a>
            <a className="footerLink" href="#">Returns</a>
          </div>
          <div className="footerCol">
            <div className="footerTitle">Newsletter</div>
            <div className="footerTag">Weekly drops of new eco-inventions.</div>
            <div className="footerNews">
              <input className="footerInput" placeholder="Email" aria-label="Newsletter email" />
              <button className="footerBtn" type="button">Subscribe</button>
            </div>
          </div>
        </div>
      </div>
    </footer>
  )
}
