import { useMemo } from 'react'
import './LegalModal.css'

import { useSettings } from '../../contexts/useSettings.js'
import { IconX } from '../icons.jsx'

const legalPages = {
  privacy: {
    title: 'Privacy Policy',
    content: (
      <>
        <p>We respect your privacy. Your data is used only to provide and improve cacun services.</p>
        <ul>
          <li>We collect minimal information needed for transactions and support.</li>
          <li>We never sell your personal data to third parties.</li>
          <li>You can request data deletion at any time.</li>
          <li>We use secure storage and encryption.</li>
        </ul>
        <p>Last updated: Dec 2025.</p>
      </>
    ),
  },
  terms: {
    title: 'Terms & Conditions',
    content: (
      <>
        <p>By using cacun, you agree to these terms.</p>
        <ul>
          <li>All product information is provided by sellers; cacun is a marketplace platform.</li>
          <li>Payments are processed securely via third-party providers.</li>
          <li>You must not misuse the platform or provide false information.</li>
          <li>We reserve the right to suspend accounts for violations.</li>
        </ul>
        <p>Last updated: Dec 2025.</p>
      </>
    ),
  },
  rights: {
    title: 'User Rights',
    content: (
      <>
        <p>You have the following rights regarding your account and data:</p>
        <ul>
          <li>Access your data and download a copy.</li>
          <li>Correct inaccurate personal information.</li>
          <li>Request deletion of your account and data.</li>
          <li>Opt out of marketing communications.</li>
          <li>Lodge complaints with data protection authorities.</li>
        </ul>
        <p>Contact support for any rights requests.</p>
      </>
    ),
  },
}

export default function LegalModal() {
  const { legalPage, setLegalPage } = useSettings()
  const open = Boolean(legalPage)

  const page = useMemo(() => (legalPage ? legalPages[legalPage] : null), [legalPage])

  if (!open || !page) return null

  return (
    <div className="legalOverlay legalOverlayOpen" role="dialog" aria-modal="true" aria-label={page.title}>
      <button className="legalBackdrop" type="button" aria-label="Close legal page" onClick={() => setLegalPage(null)} />

      <div className="legalCard">
        <div className="legalHead">
          <div className="legalTitle">{page.title}</div>
          <button className="legalClose" type="button" onClick={() => setLegalPage(null)} aria-label="Close">
            <IconX />
          </button>
        </div>

        <div className="legalBody">{page.content}</div>
      </div>
    </div>
  )
}
