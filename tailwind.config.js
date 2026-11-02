/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        aero: {
          bg: '#080D18',
          card: '#111B2B',
          elevated: '#16243A',
          border: '#223550',
          cyan: '#00D1FF',
          blue: '#5271FF',
          success: '#22C98B',
          warning: '#FFB547',
          critical: '#FF5263',
          text: '#F2F6FC',
          muted: '#8795A8'
        }
      }
    },
  },
  plugins: [],
}
