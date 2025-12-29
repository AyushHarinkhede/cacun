import { useMemo, useState } from 'react'
import './VaniePanel.css'

import { useSettings } from '../../contexts/useSettings.js'
import { buildVanieReply } from './vaniePreset.js'

export default function VaniePanel() {
  const { vanieOpen, setVanieOpen, toggleTheme } = useSettings()
  const [input, setInput] = useState('')
  const [messages, setMessages] = useState(() => [
    { role: 'vanie', text: 'Hi, I am VANIE. Ask me about plastic-free products or type: theme dark / theme light.' },
  ])

  const panelClass = useMemo(() => {
    return vanieOpen ? 'vaniePanel vaniePanelOpen' : 'vaniePanel'
  }, [vanieOpen])

  const onSend = () => {
    const text = input.trim()
    if (!text) return

    setMessages((m) => [...m, { role: 'user', text }])
    setInput('')

    const reply = buildVanieReply(text)
    if (reply.action === 'toggleTheme') toggleTheme()

    setMessages((m) => [...m, { role: 'vanie', text: reply.text }])
  }

  return (
    <aside className={panelClass} aria-label="VANIE chatbot">
      <div className="vanieHeader">
        <div className="vanieTitle">VANIE</div>
        <button className="vanieClose" type="button" onClick={() => setVanieOpen(false)} aria-label="Close chatbot">
          Close
        </button>
      </div>

      <div className="vanieBody">
        {messages.map((m, idx) => (
          <div key={idx} className={m.role === 'user' ? 'bubble bubbleUser' : 'bubble bubbleVanie'}>
            {m.text}
          </div>
        ))}
      </div>

      <div className="vanieComposer">
        <input
          className="vanieInput"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') onSend()
          }}
          placeholder="Ask VANIE…"
          aria-label="Chat message"
        />
        <button className="vanieSend" type="button" onClick={onSend}>
          Send
        </button>
      </div>
    </aside>
  )
}
