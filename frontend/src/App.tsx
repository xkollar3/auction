import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import { Elements } from '@stripe/react-stripe-js';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { HomePage } from './pages/HomePage';
import { ListingsPage } from './pages/ListingsPage';
import { AuctionItemPage } from './pages/AuctionItemPage';
import { ProfilePage } from './pages/ProfilePage';
import { PaymentSetupPage } from './pages/PaymentSetupPage';
import { PaymentMethodsPage } from './pages/PaymentMethodsPage';
import { PostAuctionItemPage } from './pages/PostAuctionItemPage';
import { SellerDashboardPage } from './pages/SellerDashboardPage';
import { ProtectedRoute } from './shared/ProtectedRoute';
import { keycloak, keycloakInitConfig } from './lib/keycloak';
import { getStripe, stripeElementsOptions } from './lib/stripe';

// Create a QueryClient instance
const queryClient = new QueryClient();

function App() {
  const stripePromise = getStripe();

  return (
    <QueryClientProvider client={queryClient}>
      <ReactKeycloakProvider authClient={keycloak} initOptions={keycloakInitConfig}>
        <Elements stripe={stripePromise} options={stripeElementsOptions}>
          <BrowserRouter>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<div>Login Page (placeholder)</div>} />
            <Route path="/register" element={<div>Register Page (placeholder)</div>} />
            <Route path="/listings" element={<ListingsPage />} />
            <Route path="/auction/:id" element={<AuctionItemPage />} />
            <Route
              path="/seller/dashboard"
              element={
                <ProtectedRoute>
                  <SellerDashboardPage />
                </ProtectedRoute>
              }
            />

            {/* Protected seller routes */}
            <Route
              path="/seller/post-item"
              element={
                <ProtectedRoute>
                  <PostAuctionItemPage />
                </ProtectedRoute>
              }
            />

            {/* Protected user management routes */}
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile/payments"
              element={
                <ProtectedRoute>
                  <PaymentMethodsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile/payments/setup"
              element={
                <ProtectedRoute>
                  <PaymentSetupPage />
                </ProtectedRoute>
              }
            />
          </Routes>
          </BrowserRouter>
        </Elements>
      </ReactKeycloakProvider>
    </QueryClientProvider>
  );
}

export default App;
