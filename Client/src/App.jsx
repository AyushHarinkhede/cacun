import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './pages/Home';
import Explore from './pages/Explore';
import CreateTrip from './pages/CreateTrip';
import Messages from './pages/Messages';
import Profile from './pages/Profile';
import Layout from './components/Layout';
import { ThemeProvider } from './contexts/ThemeContext';

function App() {
  return (
    <ThemeProvider>
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/explore" element={<Explore />} />
            <Route path="/create" element={<CreateTrip />} />
            <Route path="/messages" element={<Messages />} />
            <Route path="/profile" element={<Profile />} />
          </Routes>
        </Layout>
      </Router>
    </ThemeProvider>
  );
}

export default App;
