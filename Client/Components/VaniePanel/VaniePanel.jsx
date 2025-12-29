import { useMemo, useState, useRef } from 'react'
import './VaniePanel.css'

import { useSettings } from '../../contexts/useSettings.js'
import { buildVanieReply } from './vaniePreset.js'
import { IconMic, IconUpload, IconCopy, IconHeart, IconX } from '../icons.jsx'

export default function VaniePanel() {
  const { vanieOpen, setVanieOpen, toggleTheme } = useSettings()
  const [input, setInput] = useState('')
  const [messages, setMessages] = useState(() => [
    { role: 'vanie', text: 'Hi, I am VANIE. Ask me about plastic-free products or type: theme dark / theme light.' },
  ])
  const [isRecording, setIsRecording] = useState(false)
  const [isThinking, setIsThinking] = useState(false)
  const [showSettings, setShowSettings] = useState(false)
  const fileInputRef = useRef(null)

  const panelClass = useMemo(() => {
    return vanieOpen ? 'vaniePanel vaniePanelOpen' : 'vaniePanel'
  }, [vanieOpen])

  const onSend = async () => {
    const text = input.trim()
    if (!text) return

    setMessages((m) => [...m, { role: 'user', text }])
    setInput('')
    setIsThinking(true)

    // Simulate AI delay
    await new Promise((resolve) => setTimeout(resolve, 1000))

    const reply = buildVanieReply(text)
    if (reply.action === 'toggleTheme') toggleTheme()

    setMessages((m) => [...m, { role: 'vanie', text: reply.text }])
    setIsThinking(false)
  }

  const onVoiceToggle = () => {
    setIsRecording(!isRecording)
    // TODO: Implement voice recording
  }

  const onFileUpload = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      setMessages((m) => [...m, { role: 'user', text: `📎 Uploaded: ${file.name}` }])
      // TODO: Process file upload
    }
  }

  const onClearChat = () => {
    setMessages([{ role: 'vanie', text: 'Chat cleared. How can I help you?' }])
  }

  const onExportChat = () => {
    const chatText = messages.map((m) => `${m.role.toUpperCase()}: ${m.text}`).join('\n\n')
    const blob = new Blob([chatText], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'vanie-chat.txt'
    a.click()
    URL.revokeObjectURL(url)
  }

  const onCopyMessage = (text) => {
    navigator.clipboard.writeText(text)
  }

  const onReactToMessage = (idx, reaction) => {
    // TODO: Implement message reactions
    console.log('React to message', idx, reaction)
  }

  return (
    <aside className={panelClass} aria-label="VANIE chatbot">
      <div className="vanieHeader">
        <div className="vanieTitle">VANIE</div>
        <div className="vanieHeaderActions">
          <button
            className="vanieHeaderBtn"
            type="button"
            onClick={() => setShowSettings(!showSettings)}
            aria-label="Chat settings"
          >
            ⚙️
          </button>
          <button className="vanieClose" type="button" onClick={() => setVanieOpen(false)} aria-label="Close chatbot">
            <IconX />
          </button>
        </div>
      </div>

      {showSettings && (
        <div className="vanieSettings">
          <div className="vanieSettingsRow">
            <span>Chat History</span>
            <button className="vanieSettingsBtn" onClick={onClearChat}>Clear</button>
          </div>
          <div className="vanieSettingsRow">
            <span>Export</span>
            <button className="vanieSettingsBtn" onClick={onExportChat}>Download</button>
          </div>
        </div>
      )}

      <div className="vanieBody">
        {messages.map((m, idx) => (
          <div key={idx} className={`bubble ${m.role === 'user' ? 'bubbleUser' : 'bubbleVanie'}`}>
            <div className="bubbleContent">{m.text}</div>
            <div className="bubbleActions">
              <button
                className="bubbleAction"
                type="button"
                onClick={() => onCopyMessage(m.text)}
                aria-label="Copy message"
              >
                <IconCopy />
              </button>
              <button
                className="bubbleAction"
                type="button"
                onClick={() => onReactToMessage(idx, 'like')}
                aria-label="Like message"
              >
                <IconHeart />
              </button>
            </div>
          </div>
        ))}
        {isThinking && (
          <div className="bubble bubbleVanie">
            <div className="thinking">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        )}
      </div>

      <div className="vanieComposer">
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*,.pdf,.txt"
          onChange={onFileUpload}
          style={{ display: 'none' }}
        />
        <button
          className={`vanieVoiceBtn ${isRecording ? 'recording' : ''}`}
          type="button"
          onClick={onVoiceToggle}
          aria-label={isRecording ? 'Stop recording' : 'Start voice input'}
        >
          <IconMic />
        </button>
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
        <button
          className="vanieUploadBtn"
          type="button"
          onClick={() => fileInputRef.current?.click()}
          aria-label="Upload file"
        >
          <IconUpload />
        </button>
        <button className="vanieSend" type="button" onClick={onSend}>
          Send
        </button>
      </div>
    </aside>
  )
}
