import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => ({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // Credentials must be included for cookies to work through proxy
        // Note: The backend must allow the vite dev server origin in CORS
      },
    },
  },
  build: {
    outDir: 'dist',
  },
  define: {
    // Inject the API URL for production builds
    __API_URL__: JSON.stringify(mode === 'production' 
      ? process.env.VITE_API_URL 
      : ''),
  },
}))
