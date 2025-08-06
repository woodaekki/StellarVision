import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import commonApi from '@/api/commonApi'
import api from '@/api/axiosApi'

export const useAccountStore = defineStore('account', () => {
  const router = useRouter()
  //로컬 저장소에서 토큰을 가져온다. 없으면 빈 문자열 반환
  const token = ref(localStorage.getItem('jwt') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || null)
  const myProfile = ref(null)

  //token 소유 여부에 따라 로그인 상태를 나타낼 isLogIn 변수 저장
  const isLogin = computed(()=>{
    return token.value ? true:false
  })

  //토큰 셋
  function setToken(accessToken, newRefreshToken, memberInfo) {
    console.log('🔧 setToken 호출됨')
    console.log('📥 받은 데이터:', { accessToken, newRefreshToken, memberInfo })

    // 액세스 토큰 저장
    token.value = accessToken
    localStorage.setItem('jwt', accessToken)
    commonApi.defaults.headers.common.Authorization = `Bearer ${accessToken}`

    // 리프레시 토큰 저장
    refreshToken.value = newRefreshToken
    localStorage.setItem('refreshToken', newRefreshToken)

    // 로그인 응답으로 온 사용자 기본 정보 저장
    userInfo.value = memberInfo
    localStorage.setItem('userInfo', JSON.stringify(memberInfo))
    console.log('📦 setToken에서 userInfo 저장됨:', userInfo.value)
  }

  // 이메일 인증코드 전송
  async function sendEmailVerificationCode(email) {
    console.log('📧 이메일 인증코드 전송 시작:', email)
    try {
      const res = await commonApi.post('/auth/email/send', { email })
      console.log('✅ 인증코드 전송 성공:', res.data)
      return { success: true, message: '인증코드가 전송되었습니다.' }
    } catch (err) {
      console.error('❌ 인증코드 전송 실패:', err)
      console.error('📄 에러 응답:', err.response?.data)
      const errorMessage = err.response?.data?.message || '인증코드 전송에 실패했습니다.'
      return { success: false, message: errorMessage }
    }
  }

  // 이메일 인증코드 검증
  async function verifyEmailCode(email, code) {
    console.log('🔍 이메일 인증코드 검증 시작:', { email, code })
    try {
      const res = await commonApi.post('/auth/email/verification', {
        email,
        code
      })
      console.log('✅ 이메일 인증 성공:', res.data)
      return { success: true, message: '이메일 인증이 완료되었습니다.' }
    } catch (err) {
      console.error('❌ 이메일 인증 실패:', err)
      console.error('📄 에러 응답:', err.response?.data)
      const errorMessage = err.response?.data?.message || '인증코드가 올바르지 않습니다.'
      return { success: false, message: errorMessage }
    }
  }

  // 회원가입 로직 수정
  async function signUp({email, name, password, birth}){
    console.log('🚀 회원가입 프로세스 시작')
    console.log('📤 요청할 원본 데이터:', {email, name, password, birth})

    try {
      // 요청 데이터 정리 및 검증
      const requestData = {
        email: email?.trim(),
        name: name?.trim(),
        password: password?.trim(),
        birth: birth
      }

      console.log('🔧 정리된 요청 데이터:', requestData)
      console.log('📊 데이터 타입 체크:', {
        email: typeof requestData.email,
        name: typeof requestData.name,
        password: typeof requestData.password,
        birth: typeof requestData.birth
      })

      // 필수 데이터 검증
      if (!requestData.email || !requestData.name || !requestData.password || !requestData.birth) {
        console.error('❌ 필수 데이터 누락:', requestData)
        return { success: false, message: '필수 정보가 누락되었습니다.' }
      }

      // 이메일 형식 검증
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(requestData.email)) {
        console.error('❌ 이메일 형식 오류:', requestData.email)
        return { success: false, message: '올바른 이메일 형식이 아닙니다.' }
      }

      // 요청 직전 상태 로깅
      console.log('🌐 API 요청 직전 상태:')
      console.log('  - URL:', `${commonApi.defaults.baseURL}/members`)
      console.log('  - Headers:', commonApi.defaults.headers)
      console.log('  - Data:', requestData)

      // API 요청
      console.log('📡 API 요청 전송 중...')
      const res = await commonApi.post('/members', requestData)

      console.log('🎉 API 응답 수신됨')
      console.log('📥 전체 응답 객체:', res)
      console.log('📄 응답 데이터:', res.data)
      console.log('📊 응답 상태:', res.status)
      console.log('📋 응답 헤더:', res.headers)

      // 응답 형식에 따른 성공 처리
      if (res.data.status === 'success') {
        console.log('✅ 회원가입 성공 - 랜딩 페이지로 이동')
        router.push({name:'LandingView'})
        return { success: true, message: res.data.message || '회원가입이 완료되었습니다.' }
      } else {
        console.log('⚠️ 회원가입 실패 응답:', res.data)
        return { success: false, message: res.data.message || '회원가입에 실패했습니다.' }
      }

    } catch (err) {
      console.error('💥 회원가입 에러 발생')
      console.error('🔍 에러 객체 전체:', err)
      console.error('📄 에러 응답 데이터:', err.response?.data)
      console.error('📊 에러 상태 코드:', err.response?.status)
      console.error('📋 에러 헤더:', err.response?.headers)
      console.error('⚙️ 요청 설정:', err.config)
      console.error('🌐 요청 URL:', err.config?.url)
      console.error('📤 실제 전송된 데이터:', err.config?.data)

      // 에러 세부사항 로깅
      if (err.response?.data?.error) {
        console.error('🔥 서버 에러 세부사항:', err.response.data.error)
        if (err.response.data.error.details) {
          console.error('📝 에러 상세 내용:', err.response.data.error.details)
        }
        if (err.response.data.error.timestamp) {
          console.error('⏰ 에러 발생 시간:', err.response.data.error.timestamp)
        }
      }

      // 네트워크 관련 정보
      console.log('🌍 네트워크 정보:')
      console.log('  - 요청이 전송되었는가?', !!err.request)
      console.log('  - 응답을 받았는가?', !!err.response)
      console.log('  - 에러 코드:', err.code)
      console.log('  - 에러 메시지:', err.message)

      // 구체적인 에러 메시지 처리
      let errorMessage = '회원가입에 실패했습니다.'

      if (err.response?.data?.error?.details) {
        const errorDetails = err.response.data.error.details
        console.log('🔍 에러 상세 분석:', errorDetails)

        if (errorDetails.includes('constraint')) {
          console.log('🚫 제약 조건 위반 감지')
          if (errorDetails.includes('UK_PROFILE_KEY')) {
            console.log('👤 프로필 키 제약 조건 위반')
            errorMessage = '프로필 생성 중 오류가 발생했습니다. 서버 관리자에게 문의하세요.'
          } else if (errorDetails.toLowerCase().includes('email')) {
            console.log('📧 이메일 중복')
            errorMessage = '이미 가입된 이메일 주소입니다.'
          } else if (errorDetails.toLowerCase().includes('name')) {
            console.log('👤 닉네임 중복')
            errorMessage = '이미 사용 중인 닉네임입니다.'
          } else {
            console.log('🔄 기타 중복 제약 조건')
            errorMessage = '중복된 정보가 있습니다. 다른 정보로 시도해주세요.'
          }
        }
      } else if (err.response?.data?.message) {
        console.log('📝 서버 메시지 사용:', err.response.data.message)
        errorMessage = err.response.data.message
      } else if (err.response?.status === 500) {
        console.log('💥 500 서버 내부 오류')
        errorMessage = '서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
      } else if (err.response?.status === 400) {
        console.log('📝 400 잘못된 요청')
        errorMessage = '입력한 정보를 다시 확인해주세요.'
      }

      console.log('📢 최종 에러 메시지:', errorMessage)
      return { success: false, message: errorMessage }
    }
  }

  // 로그인 로직
  async function logIn({email, password}) {
    console.log('🔐 로그인 프로세스 시작:', { email })

    const formData = new FormData()
    formData.append('email', email)
    formData.append('password', password)

    console.log('📤 FormData 생성 완료')

    try {
      console.log('📡 로그인 API 요청 전송')
      const res = await commonApi.post(
        '/auth/login',
        formData,
        { headers: { 'Content-Type' : 'multipart/form-data' }}
      )

      console.log('🎉 로그인 API 응답 수신')
      console.log('📥 전체 응답:', res)
      console.log('📄 응답 데이터:', res.data)

      const {accessToken, refreshToken, memberInfo} = res.data.data
      console.log('🔧 토큰 및 사용자 정보 추출:', {
        accessToken: accessToken ? '존재함' : '없음',
        refreshToken: refreshToken ? '존재함' : '없음',
        memberInfo
      })

      setToken(accessToken, refreshToken, memberInfo)
      console.log('✅ 로그인 성공 - 랜딩 페이지로 이동')
      router.push({name: 'LandingView'})

    } catch (err) {
      console.error('❌ 로그인 실패:', err)
      console.error('📄 에러 응답:', err.response?.data)
      throw err
    }
  }

  // 로그아웃 및 상태 초기화
  function logOut() {
    console.log('👋 로그아웃 프로세스 시작')
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    myProfile.value = null
    localStorage.removeItem('jwt')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    delete commonApi.defaults.headers.common.Authorization
    console.log('✅ 로그아웃 완료 - 랜딩 페이지로 이동')
    router.push({ name: 'LandingView' })
  }

  // 내 프로필 정보 조회
  async function fetchMyProfile() {
    console.log('👤 내 프로필 조회 시작')
    // 로그인 유무 확인
    if (!isLogin.value) {
      console.log('❌ 로그인되지 않음')
      return
    }

    try{
      console.log('📡 프로필 API 요청 전송')
      const res = await commonApi.get('/profiles/me')
      myProfile.value = res.data.data
      console.log('✅ 내 프로필 정보 조회 성공:', myProfile.value)
    } catch (err) {
      console.error('❌ 프로필 조회 실패:', err)
      console.error('📄 에러 응답:', err.response?.data)
    }
  }

  // 다른 사용자 프로필 정보 조회
  async function fetchUserProfile(memberId) {
    console.log('👥 사용자 프로필 조회 시작:', memberId)
    try {
      console.log('📡 사용자 프로필 API 요청 전송')
      const res = await commonApi.get(`/profiles/${memberId}`)
      console.log('✅ 사용자 프로필 조회 성공:', res.data.data)
      return res.data.data
    } catch(err){
      console.error(`❌ ${memberId} 프로필 정보 조회 실패:`, err)
      console.error('📄 에러 응답:', err.response?.data)
      return null
    }
  }

  // 새로고침 시 로그인 상태 유지
  if(token.value){
    console.log('🔄 페이지 새로고침 - 토큰으로 로그인 상태 복원')
    commonApi.defaults.headers.common.Authorization = `Bearer ${token.value}`
  }

  return {
    isLogin,
    signUp,
    logIn,
    logOut,
    token,
    userInfo,
    myProfile,
    fetchMyProfile,
    fetchUserProfile,
    sendEmailVerificationCode,
    verifyEmailCode
  }
})
