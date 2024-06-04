/** @type {import('tailwindcss').Config} */

export default {
  // mode: 'jit', // maybe that will be problematic i don't know
  content: [
    "./src/renderer/index.html",
    "./src/renderer/src/**/*.{js,ts,jsx,tsx}",
  ],

  theme: {
    extend: {},
  },
  plugins: [],
}
