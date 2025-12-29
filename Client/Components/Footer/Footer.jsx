import './Footer.css'

import logo from '../cacun.png'

export default function Footer() {
  return (
    <footer className="footerWrap">
      <svg className="footerWave" viewBox="0 0 1440 90" preserveAspectRatio="none" aria-hidden="true">
        <path
          fill="var(--bg)"
          d="M0,64L60,58.7C120,53,240,43,360,37.3C480,32,600,32,720,37.3C840,43,960,53,1080,53.3C1200,53,1320,43,1380,37.3L1440,32L1440,0L1380,0C1320,0,1200,0,1080,0C960,0,840,0,720,0C600,0,480,0,360,0C240,0,120,0,60,0L0,0Z"
        />
      </svg>

      <div className="footerInner">
        <div className="footerBrand">
          <img className="footerLogo" src={logo} alt="cacun" />
          <div className="footerTag">Nature-first • Plastic-free • Non-toxic</div>
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
            <div className="footerTitle">Legal</div>
            <a className="footerLink" href="#">Privacy policy</a>
            <a className="footerLink" href="#">Terms</a>
            <a className="footerLink" href="#">Rules & regulations</a>
          </div>

          <div className="footerCol">
            <div className="footerTitle">Connect</div>
            <a className="footerLink" href="#">Instagram</a>
            <a className="footerLink" href="#">X (Twitter)</a>
            <a className="footerLink" href="#">LinkedIn</a>
            <a className="footerLink" href="#">Email</a>
          </div>

          <div className="footerCol">
            <div className="footerTitle">Company</div>
            <a className="footerLink" href="#">About</a>
            <a className="footerLink" href="#">Careers</a>
            <a className="footerLink" href="#">Brand promises</a>
          </div>
        </div>
      </div>
    </footer>
  )
}
