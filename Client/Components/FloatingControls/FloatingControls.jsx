import { useState } from 'react'
import './FloatingControls.css'

import { useSettings } from '../../contexts/useSettings.js'
import {
  IconBack,
  IconChevronUp,
  IconPlus,
  IconSettings,
  IconVanie,
  IconX,
} from '../icons.jsx'

export default function FloatingControls() {
  const { setSettingsOpen, setVanieOpen } = useSettings()
  const [open, setOpen] = useState(false)

  const scrollTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <div className="floatDock" aria-label="Quick controls">
      <div className={open ? 'floatStack floatStackOpen' : 'floatStack'}>
        <button className="floatBtn" type="button" onClick={() => setSettingsOpen(true)} aria-label="Settings">
          <IconSettings />
        </button>
        <button className="floatBtn" type="button" onClick={scrollTop} aria-label="Scroll to top">
          <IconChevronUp />
        </button>
        <button className="floatBtn" type="button" onClick={() => window.history.back()} aria-label="Back">
          <IconBack />
        </button>
      </div>

      <button
        className={open ? 'floatToggle floatToggleOpen' : 'floatToggle'}
        type="button"
        onClick={() => setOpen((o) => !o)}
        aria-label={open ? 'Hide quick controls' : 'Show quick controls'}
      >
        {open ? <IconX /> : <IconPlus />}
      </button>

      <button className="vanieBtn" type="button" onClick={() => setVanieOpen(true)} aria-label="Open VANIE">
        <IconVanie />
      </button>
    </div>
  )
}
