import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import globals from 'globals'

/**
 * ESLint 10 只支持 flat config（eslintrc 已彻底移除）。
 *
 * 规则策略：先接"能抓到真 bug"的那一层，不追求风格统一。
 * - js.configs.recommended：no-undef / no-unused-vars / no-dupe-keys 等
 * - pluginVue flat/essential：Vue 3 的必需规则（重复 key、v-for 缺 key 等）
 * 刻意不启用 flat/recommended 与 stylistic，那两套是风格约束，
 * 对已有代码会产生大量纯格式告警，反而淹没真问题。
 */
export default [
  {
    name: 'wenji/ignores',
    ignores: [
      'dist/**',
      'node_modules/**',
      '.vite/**',
      'public/**',
      '**/*.min.js',
      // json 不是 JS，交给 IDE / JSON Schema 校验，ESLint 只做 JS/Vue
      '**/*.json',
    ],
  },

  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],

  {
    name: 'wenji/browser',
    files: ['**/*.{js,mjs,cjs,vue}'],
    languageOptions: {
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        // Vue SFC 编译器宏，无需 import
        defineProps: 'readonly',
        defineEmits: 'readonly',
        defineExpose: 'readonly',
        defineOptions: 'readonly',
        defineSlots: 'readonly',
        defineModel: 'readonly',
        withDefaults: 'readonly',
      },
    },
    rules: {
      // 现有代码里存在大量"声明了没用"的变量，先降级为警告：
      // 仍会打印，但不阻断 CI，便于逐步清理
      'no-unused-vars': ['warn', { args: 'none', caughtErrors: 'none' }],
      // Vue 模板里大量使用无前缀组件名与多单词以外命名，保持 warning
      'vue/multi-word-component-names': 'off',
    },
  },

  {
    name: 'wenji/node-configs',
    files: [
      '*.config.js',
      'vite.config.js',
      'tailwind.config.js',
      'postcss.config.js',
    ],
    languageOptions: {
      globals: { ...globals.node },
    },
  },
]
