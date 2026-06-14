/*
 * PATCH INSTRUCTIONS for frontend/src/services/api.js
 *
 * Change line 4 from:
 *   baseURL: '/api',
 *
 * To:
 *   baseURL: import.meta.env.VITE_API_URL || '/api',
 *
 * This makes the frontend use the Nginx-proxied /api in development
 * and the full Oracle Cloud URL (https://your-domain.duckdns.org/api)
 * in production builds.
 *
 * No other changes needed in api.js.
 */
