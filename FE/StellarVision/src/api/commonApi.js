import axios from "axios"

const commonApi = axios.create({
  baseURL: 'https://i13c106.p.ssafy.io',   //개발 단계에서 사용할 도메인
  timeout: 1000,
  headers: {'Content-Type' : 'application/json'}    //HTTP에 JSON 전달임을 명시
})

export default commonApi
