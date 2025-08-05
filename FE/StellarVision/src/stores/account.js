import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import commonApi from '@/api/commonApi'

export const useAccountStore = defineStore('account', () => {
  const router = useRouter()
  //로컬 저장소에서 토큰을 가져온다. 없으면 빈 문자열 반환
  const token = ref(localStorage.getItem('jwt') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || null)
  const myProfile = ref(null)

  //token 소유 여부에 따라 로그인 상태를 나타 낼 isLogIn 변수 저장
  const isLogin = computed(()=>{
    return token.value ? true:false
  })

  //토큰 셋
  function setToken(accessToken, newRefreshToken, memberInfo) {
    // 액세스 토큰 저장
    token.value = accessToken
    localStorage.setItem('jwt', accessToken)
    commonApi.defaults.headers.common.Authorization = `Bearer ${accessToken}`    //  토큰이 있다면 모든 요청에 인증 헤더를 자동으로 붙이도록 한다.

    // 리프레시 토큰 저장
    refreshToken.value = newRefreshToken
    localStorage.setItem('refreshToken', newRefreshToken)

    // 로그인 응답으로 온 사용자 기본 정보 저장
    userInfo.value = memberInfo
    localStorage.setItem('userInfo', JSON.stringify(memberInfo))
    console.log('📦 setToken에서 userInfo 저장됨:', userInfo.value)
  }


  // 회원가입 로직
  const signUp = function({email, name, password, birth}){
    commonApi.post('/members', {email, name, password, birth})
    .then(res => {
      console.log('회원가입 성공', res.data)
      router.push({name:'LandingView'})
    })
    .catch(err => {
      console.log(err)
    })
  }


  // 로그인 로직
  async function logIn({email, password}) {

    const formData = new FormData()
    formData.append('email', email)
    formData.append('password', password)

    try {
      const res = await commonApi.post(
        '/auth/login',
        formData,
      { headers: { 'Content-Type' : 'multipart/form-data' }}
    )

     console.log('로그인 전체 응답:', res)
     console.log('res.data:', res.data)
     console.log('res.data.data:', res.data.data)

      const {accessToken, refreshToken, memberInfo} = res.data.data
      setToken(accessToken, refreshToken, memberInfo)                // 토큰 및 정보 저장
      console.log('로그인 성공')
      console.log('accessToken:', accessToken)
      console.log(memberInfo)
      router.push({name: 'LandingView'})
    } catch (err) {
      console.error('로그인 실패', err)
      throw err
    }
  }


    // 로그아웃 및 상태 초기화
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

  // 내 프로필 정보 조회
  async function fetchMyProfile() {
    // 로그인 유무 확인
    if (!isLogin.value) return

    try{
      const res = await commonApi.get('/profiles/me')
      myProfile.value = res.data.data
      console.log('내 프로필 정보', myProfile.value)
    } catch (err) {
      console.error('조회 실패', err)
    }
  }


  // 다른 사용자 프로필 정보 조회
  async function fetchUserProfile(memberId) {
    try {
      const res = await commonApi.get(`/profiles/${memberId}`)    // api 명세서 참조 경로
      return res.data.data
    } catch(err){
      console.error(`${memberId} 프로필 정보 조회 실패`, err)
      return null
    }
  }

  // 새로고침 시 로그인 상태 유지
  if(token.value){
    commonApi.defaults.headers.common.Authorization = `Bearer ${token.value}`
  }


  return { isLogin, signUp, logIn, logOut, token, userInfo, myProfile, fetchMyProfile, fetchUserProfile }
})

