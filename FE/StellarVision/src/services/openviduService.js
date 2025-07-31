import { OpenVidu } from 'openvidu-browser'
import streamingApi from '@/api/streamingApi'

// OpenVidu 브라우저 클라이언트 인스턴스 생성
const OV = new OpenVidu()
let session = null

/**
 * 스트리밍 방 생성
 * @param {Object} options - { title, latitude, longitude, forcedVideoCodec, mediaMode, recordingMode }
 * @returns {Promise<string>} sessionId
 */
export async function createStreamingSession(options) {
  const response = await streamingApi.post('/api/streamings', options)
  // API 응답의 data 필드에 sessionId가 담겨있다고 가정
  return response.data.data
}

/**
 * 스트리밍 방에 참여 (스트리머/시청자 구분)
 * @param {string} sessionId - 방 ID
 * @param {'PUBLISHER'|'SUBSCRIBER'} role - 'PUBLISHER'는 스트리머, 'SUBSCRIBER'는 시청자
 * @returns {Promise<Session>} connected OpenVidu Session 객체
 */
export async function joinStreamingSession(sessionId, role) {
  // 백엔드에서 토큰 발급 (form-data 전송)
  const form = new FormData()
  form.append('role', role)
  const response = await streamingApi.post(
    `/api/streamings/${sessionId}/connections`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  )
  const token = response.data.data

  // OpenVidu 세션 초기화 및 연결
  session = OV.initSession()
  await session.connect(token)
  return session
}

/**
 * 로컬 OpenVidu 세션 반환
 */
export function getCurrentSession() {
  return session
}
