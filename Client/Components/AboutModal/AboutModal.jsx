import { useMemo } from 'react'
import './AboutModal.css'

import { useSettings } from '../../contexts/useSettings.js'
import { IconX } from '../icons.jsx'

const pages = {
  about: {
    title: 'About Cacun',
    content: (
      <>
        <p>Cacun is a nature-first marketplace connecting you to plastic‑free, non‑toxic, recycled, and reusable products.</p>
        <p>We partner with verified NGOs and campaigns so every purchase helps protect the planet.</p>
        <p>Our mission: make sustainable living easy, transparent, and delightful.</p>
      </>
    ),
  },
  faq: {
    title: 'FAQ',
    content: (
      <>
        <p><strong>How do I know a product is really plastic‑free?</strong><br />We verify product details with sellers and display material info and certifications.</p>
        <p><strong>Can I return a product?</strong><br />Yes, within 7 days if unused. See seller policy on product page.</p>
        <p><strong>Do you ship internationally?</strong><br />Currently within India only. We’re expanding soon.</p>
        <p><strong>How do NGOs benefit?</strong><br />A portion of eligible purchases goes to partnered NGOs; you can track impact in your profile.</p>
        <p><strong>Is my payment secure?</strong><br />We use encrypted, PCI‑compliant payment processors.</p>
      </>
    ),
  },
}

export default function AboutModal() {
  const { aboutPage, setAboutPage } = useSettings()
  const open = Boolean(aboutPage)

  const page = useMemo(() => (aboutPage ? pages[aboutPage] : null), [aboutPage])

  if (!open || !page) return null

  return (
    <div className="aboutOverlay aboutOverlayOpen" role="dialog" aria-modal="true" aria-label={page.title}>
      <button className="aboutBackdrop" type="button" aria-label="Close about page" onClick={() => setAboutPage(null)} />

      <div className="aboutCard">
        <div className="aboutHead">
          <div className="aboutTitle">{page.title}</div>
          <button className="aboutClose" type="button" onClick={() => setAboutPage(null)} aria-label="Close">
            <IconX />
          </button>
        </div>

        <div className="aboutBody">{page.content}</div>
      </div>
    </div>
  )
}
