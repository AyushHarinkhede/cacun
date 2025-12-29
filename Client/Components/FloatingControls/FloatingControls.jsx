import { useState } from 'react'
import './FloatingControls.css'

import { useSettings } from '../../contexts/SettingsContext.jsx'
import {
  IconBack,
  IconChevronUp,
  IconChat,
  IconSettings,
} from '../icons.jsx'

export default function FloatingControls() {
  const { setSettingsOpen, setVanieOpen } = useSettings()
  const [open, setOpen] = useState(false)

  const scrollTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <div className="fabWrap" aria-label="Quick controls">
      <button className="fabMain" type="button" onClick={() => setOpen((o) => !o)} aria-label="Open quick controls">
        <IconSettings />
      </button>

      <div className={open ? 'fabActions fabActionsOpen' : 'fabActions'}>
        <button className="fabAction" type="button" onClick={() => setSettingsOpen(true)} aria-label="Settings">
          <IconSettings />
        </button>
        <button className="fabAction" type="button" onClick={scrollTop} aria-label="Scroll to top">
          <IconChevronUp />
        </button>
        <button className="fabAction" type="button" onClick={() => window.history.back()} aria-label="Back">
          <IconBack />
        </button>
      </div>

      <button className="fabChat" type="button" onClick={() => setVanieOpen(true)} aria-label="Open VANIE chatbot">
        <IconChat />
      </button>
    </div>
  )
}
