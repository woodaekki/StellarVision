// api/commonApi.js
import axios from "axios";

const commonApi = axios.create({
  baseURL: 'https://i13c106.p.ssafy.io/api',
  timeout: 10000, // 타임아웃 증가
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
});

// 요청 인터셉터
commonApi.interceptors.request.use(
  function (config) {
    console.log('🚀 CommonAPI 요청 인터셉터 실행')
    console.log('📤 요청 설정:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      baseURL: config.baseURL,
      fullURL: `${config.baseURL}${config.url}`,
      headers: config.headers,
      data: config.data
    })

    const token = localStorage.getItem('jwt');
    if (token) {
      config.headers.Authorization = 'Bearer ' + token;
      console.log('🔑 토큰이 헤더에 추가됨')
    } else {
      console.log('⚠️ 토큰이 없음')
    }

    return config;
  },
  function (error) {
    console.error('❌ CommonAPI 요청 인터셉터 에러:', error)
    return Promise.reject(error);
  }
);

// 응답 인터셉터
commonApi.interceptors.response.use(
  function (response) {
    console.log('✅ CommonAPI 응답 인터셉터 - 성공')
    console.log('📥 응답 데이터:', {
      status: response.status,
      statusText: response.statusText,
      headers: response.headers,
      data: response.data
    })
    return response;
  },
  function (error) {
    console.error('❌ CommonAPI 응답 인터셉터 - 에러')
    console.error('💥 에러 상세:', {
      message: error.message,
      code: error.code,
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      headers: error.response?.headers
    })

    // 401 에러 처리 (인증 실패)
    if (error.response?.status === 401) {
      console.log('🔐 401 인증 실패 - 로그아웃 처리 필요')
      localStorage.removeItem('jwt')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
      // 필요시 로그인 페이지로 리다이렉트
    }

    return Promise.reject(error);
  }
);

export default commonApi
