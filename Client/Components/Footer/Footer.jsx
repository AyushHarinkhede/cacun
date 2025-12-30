import './Footer.css'

import logo from '../cacun.png'
import { useSettings } from '../../contexts/useSettings.js'
import { IconInstagram, IconTwitter, IconLinkedin, IconMail, IconPhone, IconMapPin, IconMessageCircle } from '../icons.jsx'

export default function Footer() {
  const { setAboutPage, setLegalPage } = useSettings()
  return (
    <footer className="footerWrap">
      <svg className="footerWave" viewBox="0 0 1440 90" preserveAspectRatio="none" aria-hidden="true">
        <path
          fill="var(--bg)"
          d="M0,64L60,58.7C120,53,240,43,360,37.3C480,32,600,32,720,37.3C840,43,960,53,1080,53.3C1200,53,1320,43,1380,37.3L1440,32L1440,0L1380,0C1320,0,1200,0,1080,0C960,0,840,0,720,0C600,0,480,0,360,0C240,0,120,0,60,0L0,0Z"
        />
      </svg>

      <div className="footerInner">
        <div className="footerCorner">
          <img className="footerLogo" src={logo} alt="cacun" />
          <div className="footerTag">Nature-first • Plastic-free • Non-toxic</div>
          <div className="footerSocial">
            <a className="socialIcon" href="#" target="_blank" rel="noopener noreferrer" aria-label="Instagram">
              <IconInstagram />
            </a>
            <a className="socialIcon" href="#" target="_blank" rel="noopener noreferrer" aria-label="X (Twitter)">
              <IconTwitter />
            </a>
            <a className="socialIcon" href="#" target="_blank" rel="noopener noreferrer" aria-label="LinkedIn">
              <IconLinkedin />
            </a>
            <a className="socialIcon" href="#" target="_blank" rel="noopener noreferrer" aria-label="Email">
              <IconMail />
            </a>
          </div>
        </div>

        <div className="footerContent">
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
              <button className="footerLink" type="button" onClick={() => setLegalPage('privacy')}>Privacy policy</button>
              <button className="footerLink" type="button" onClick={() => setLegalPage('terms')}>Terms</button>
              <button className="footerLink" type="button" onClick={() => setLegalPage('rights')}>User rights</button>
            </div>

            <div className="footerCol">
              <div className="footerTitle">Company</div>
              <button className="footerLink" type="button" onClick={() => setAboutPage('about')}>About</button>
              <button className="footerLink" type="button" onClick={() => setAboutPage('faq')}>FAQ</button>
              <a className="footerLink" href="#">Careers</a>
              <a className="footerLink" href="#">Brand promises</a>
            </div>
          </div>
          
          <div className="footerContact">
            <div className="footerContactItem">
              <IconPhone />
              <span>+91 98765 43210</span>
            </div>
            <div className="footerContactItem">
              <IconMail />
              <span>support@cacun.com</span>
            </div>
            <div className="footerContactItem">
              <IconMapPin />
              <span>Mumbai, India</span>
            </div>
            <div className="footerContactItem">
              <IconMessageCircle />
              <span>Live Chat Available</span>
            </div>
          </div>
        </div>
      </div>
      
      <div className="footerBottom">
        <div className="footerCopyright">
          <p>© 2025 Cacun. All rights reserved.</p>
          <p>Made with ❤️ for nature lovers</p>
        </div>
      </div>
    </footer>
  )
}
