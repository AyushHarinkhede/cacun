import { useContext } from 'react'

import { SettingsContext } from './settingsContext.js'

// Improved: add a default export and clarify error message
export function useSettings() {
  const ctx = useContext(SettingsContext)
  if (ctx === undefined || ctx === null) {
    throw new Error('useSettings must be used within a <SettingsProvider>.')
  }
  return ctx
}

export default useSettings
