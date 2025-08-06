import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import commonApi from '@/api/commonApi'
import api from '@/api/axiosApi'

export const useAccountStore = defineStore('account', () => {
  const router = useRouter()
  const token = ref(localStorage.getItem('jwt') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || null)
  const myProfile = ref(null)

  const isLogin = computed(() => {
    return token.value ? true : false
  })

  function setToken(accessToken, newRefreshToken, memberInfo) {
    token.value = accessToken
    localStorage.setItem('jwt', accessToken)
    commonApi.defaults.headers.common.Authorization = `Bearer ${accessToken}`

    refreshToken.value = newRefreshToken
    localStorage.setItem('refreshToken', newRefreshToken)

    userInfo.value = memberInfo
    localStorage.setItem('userInfo', JSON.stringify(memberInfo))
  }

  async function sendEmailVerificationCode(email) {
    try {
      const res = await commonApi.post('/auth/email/send', { email })
      return { success: true, message: '인증코드가 전송되었습니다.' }
    } catch (err) {
      const errorMessage = err.response?.data?.message || '인증코드 전송에 실패했습니다.'
      return { success: false, message: errorMessage }
    }
  }

  async function verifyEmailCode(email, code) {
    try {
      const res = await commonApi.post('/auth/email/verification', {
        email,
        code
      })
      return { success: true, message: '이메일 인증이 완료되었습니다.' }
    } catch (err) {
      const errorMessage = err.response?.data?.message || '인증코드가 올바르지 않습니다.'
      return { success: false, message: errorMessage }
    }
  }

  async function signUp({ email, name, password, birth }) {
    try {
      const requestData = {
        email: email?.trim(),
        name: name?.trim(),
        password: password?.trim(),
        birth: birth
      }

      if (!requestData.email || !requestData.name || !requestData.password || !requestData.birth) {
        return { success: false, message: '필수 정보가 누락되었습니다.' }
      }

      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(requestData.email)) {
        return { success: false, message: '올바른 이메일 형식이 아닙니다.' }
      }

      const res = await commonApi.post('/members', requestData)

      if (res.data.status === 'success') {
        router.push({ name: 'LoginView' })
        return { success: true, message: res.data.message || '회원가입이 완료되었습니다.' }
      } else {
        return { success: false, message: res.data.message || '회원가입에 실패했습니다.' }
      }
    } catch (err) {
      let errorMessage = '회원가입에 실패했습니다.'

      if (err.response?.data?.error?.details) {
        const errorDetails = err.response.data.error.details

        if (errorDetails.includes('constraint')) {
          if (errorDetails.includes('UK_PROFILE_KEY')) {
            errorMessage = '프로필 생성 중 오류가 발생했습니다. 서버 관리자에게 문의하세요.'
          } else if (errorDetails.toLowerCase().includes('email')) {
            errorMessage = '이미 가입된 이메일 주소입니다.'
          } else if (errorDetails.toLowerCase().includes('name')) {
            errorMessage = '이미 사용 중인 닉네임입니다.'
          } else {
            errorMessage = '중복된 정보가 있습니다. 다른 정보로 시도해주세요.'
          }
        }
      } else if (err.response?.data?.message) {
        errorMessage = err.response.data.message
      } else if (err.response?.status === 500) {
        errorMessage = '서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
      } else if (err.response?.status === 400) {
        errorMessage = '입력한 정보를 다시 확인해주세요.'
      }

      return { success: false, message: errorMessage }
    }
  }

  async function logIn({ email, password }) {
    const formData = new FormData()
    formData.append('email', email)
    formData.append('password', password)

    try {
      const res = await commonApi.post(
        '/auth/login',
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } }
      )

      const { accessToken, refreshToken, memberInfo } = res.data.data
      setToken(accessToken, refreshToken, memberInfo)
      router.push({ name: 'LandingView' })

    } catch (err) {
      throw err
    }
  }

  function logOut() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    myProfile.value = null
    localStorage.removeItem('jwt')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    delete commonApi.defaults.headers.common.Authorization
    router.push({ name: 'LandingView' })
  }

  async function fetchMyProfile() {
    if (!isLogin.value) {
      return
    }

    try {
      const res = await commonApi.get('/profiles/me')
      myProfile.value = res.data.data
    } catch (err) {
      // error handling omitted for brevity
    }
  }

  async function fetchUserProfile(memberId) {
    try {
      const res = await commonApi.get(`/profiles/${memberId}`)
      return res.data.data
    } catch (err) {
      return null
    }
  }

  if (token.value) {
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
