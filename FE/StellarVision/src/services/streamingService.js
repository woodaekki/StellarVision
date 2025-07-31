// src/services/streaming.js
import streamingApi from '@/api/streamingApi'
// 해당 파일은 axios 호출만 담당합니다.
export default {
  // 1) 스트리밍 방 생성하기
  create({ title, latitude, longitude, forcedVideoCodec, mediaMode, recordingMode }) {
    return streamingApi.post(
      '/api/streamings',
      { title, latitude, longitude, forcedVideoCodec, mediaMode, recordingMode },
      { headers: { 'Content-Type': 'application/json' } }
    )
  },

  // 2) 스트리밍 참여하기
  join(streamId, { role /* e.g. 'publisher' | 'subscriber' */, userName }) {
    // form-data 로 보내야 한다면:
    const form = new FormData()
    form.append('role', role)
    form.append('userName', userName)
    return streamingApi.post(`/api/streamings/${streamId}/connection`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 3) 녹화 시작
  startRecording(streamId) {
    return streamingApi.post(`/api/streamings/${streamId}/recording/start`)
  },
  // 4) 녹화 정지
  stopRecording(streamId) {
    return streamingApi.post(`/api/streamings/${streamId}/recording/stop`)
  },

  // 5) 스트리밍 종료하기
  end(streamId) {
    return streamingApi.delete(`/api/streamings/${streamId}`)
  },

  // 6) 스트리밍 목록 조회
  list() {
    return streamingApi.get('/api/streamings')
  }
}
