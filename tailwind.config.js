/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/app/*.{html,js,ts}", // ระบุโฟลเดอร์ที่ Tailwind จะตรวจจับ HTML/JS
  ],
  theme: {
    extend: {
      fontFamily: {
        inria: ['"Inria Sans"', 'sans-serif'], // ตั้งชื่อ 'inria' ให้ฟอนต์
      },
    },
  },
  plugins: [require('tailwind-scrollbar-hide')],
};
