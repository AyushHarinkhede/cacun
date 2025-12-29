import './App.css'

import { SettingsProvider } from './contexts/SettingsContext.jsx'
import Navbar from './Components/Navbar/Navbar.jsx'
import Home from './Components/Home/Home.jsx'
import Footer from './Components/Footer/Footer.jsx'
import FloatingControls from './Components/FloatingControls/FloatingControls.jsx'
import VaniePanel from './Components/VaniePanel/VaniePanel.jsx'
import SettingsDrawer from './Components/SettingsDrawer/SettingsDrawer.jsx'
import AuthDrawer from './Components/AuthDrawer/AuthDrawer.jsx'
import ProductModal from './Components/ProductModal/ProductModal.jsx'

export default function App() {
  return (
    <SettingsProvider>
      <div className="appShell">
        <Navbar />
        <main className="appMain">
          <Home />
        </main>
        <Footer />
        <FloatingControls />
        <VaniePanel />
        <SettingsDrawer />
        <AuthDrawer />
        <ProductModal />
      </div>
    </SettingsProvider>
  )
}
