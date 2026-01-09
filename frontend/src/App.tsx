import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { HomePage } from './pages/HomePage';
import { ListingsPage } from './pages/ListingsPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<div>Login Page (placeholder)</div>} />
        <Route path="/register" element={<div>Register Page (placeholder)</div>} />
        <Route path="/listings" element={<ListingsPage />} />
        <Route path="/seller/dashboard" element={<div>Seller Dashboard (placeholder)</div>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
