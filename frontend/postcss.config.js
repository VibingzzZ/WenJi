// Tailwind CSS v4 起，PostCSS 插件已从 tailwindcss 主包拆到 @tailwindcss/postcss。
// 仍写 tailwindcss: {} 会直接报错：
//   "It looks like you're trying to use `tailwindcss` directly as a PostCSS plugin."
export default {
  plugins: {
    '@tailwindcss/postcss': {},
    autoprefixer: {},
  },
}
