// src/services/streaming.js
import streamingApi from '@/api/streamingApi'
// 해당 파일은 axios 호출만 담당합니다.
export default {
  // 1) 스트리밍 방 생성하기
  create({ title, latitude, longitude, forcedVideoCodec, mediaMode, recordingMode }) {
    return streamingApi.post(
      '/streamings',
      { title, latitude, longitude, forcedVideoCodec, mediaMode, recordingMode },
      { headers: { 'Content-Type': 'application/json' } }
    )
  },

  // 2) 스트리밍 참여하기
  join(streamId, { role, userName }) {
    // form-data 로 보내야 한다면:
    const form = new FormData()
    form.append('role', role)
    form.append('userName', userName)
    return streamingApi.post(
      `/streamings/${streamId}/connection`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 3) 녹화 시작 / 정지
  toggleRecording(streamId, action) {
    return streamingApi.post(`/streamings/${streamId}/recording/${action}`, new FormData())
  },

  // 4) 스트리밍 종료하기
  end(streamId) {
    return streamingApi.delete(`/streamings/${streamId}`)
  },

  // 5) 스트리밍 목록 조회
  list() {
    return streamingApi.get('/streamings')
  },

  // 2) 토큰 발급용 메서드 추가
  getToken(sessionId, userName, role = 'SUBSCRIBER') {
    return streamingApi.post(
      `/streamings/${sessionId}/connection`,
      { role, userName }
    )
  },


}
