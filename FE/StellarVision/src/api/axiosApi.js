import axios from "axios";

const api = axios.create({
  baseURL: 'https://i13c106.p.ssafy.io/api',
  maxContentLength: Infinity,
  maxBodyLength: Infinity,
  timeout: 100000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
});

// 요청 인터셉터
api.interceptors.request.use(
  function (config) {
    console.log('🚀 AxiosAPI 요청 인터셉터 실행')
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
    console.error('❌ AxiosAPI 요청 인터셉터 에러:', error)
    return Promise.reject(error);
  }
);

// 응답 인터셉터
api.interceptors.response.use(
  function (response) {
    console.log('✅ AxiosAPI 응답 인터셉터 - 성공')
    console.log('📥 응답 데이터:', {
      status: response.status,
      statusText: response.statusText,
      headers: response.headers,
      data: response.data
    })
    return response;
  },
  function (error) {
    console.error('❌ AxiosAPI 응답 인터셉터 - 에러')
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
    }

    return Promise.reject(error);
  }
);

export default api;
