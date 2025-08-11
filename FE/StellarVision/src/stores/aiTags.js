// src/stores/aiTags.js
import { defineStore } from 'pinia'

export const useAITagStore = defineStore('aiTags', {
  state: () => ({
    // sessionId -> { tagName: true, ... }
    tagsBySession: {}
  }),
  getters: {
    // 세션별 태그 배열로 반환
    list: (s) => (sessionId) => Object.keys(s.tagsBySession[sessionId] || {})
  },
  actions: {
    // AI 응답에서 class만 추출해 중복 없이 누적
    addFromPredictions(sessionId, res) {
      if (!res || !Array.isArray(res.predictions)) return
      if (!this.tagsBySession[sessionId]) this.tagsBySession[sessionId] = {}
      const dict = this.tagsBySession[sessionId]
      for (const p of res.predictions) {
        const name = normalize(p.class)
        if (name) dict[name] = true
      }
    },
    clear(sessionId) {
      delete this.tagsBySession[sessionId]
    }
  }
})

// 표기 표준화(필요시 확장)
function normalize(raw) {
  if (!raw) return ''
  const s = String(raw).trim()
  const map = {
    cyg: 'Cyg', vul: 'Vul', aql: 'Aql', lyr: 'Lyr', sge: 'Sge', del: 'Del',
    scorpius: 'Sco', sco: 'Sco'
  }
  const key = s.toLowerCase()
  return map[key] || s
}
