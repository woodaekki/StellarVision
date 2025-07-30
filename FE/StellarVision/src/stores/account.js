import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import commonApi from '@/api/commonApi'

export const useAccountStore = defineStore('account', () => {
  // const ACCOUNT_URL = 'http://127.0.0.1:8080/accounts'   -> axios 생성 대체
  const router = useRouter()
  // 1. 로컬 저장소에서 토큰을 가져온다. 없으면 빈 문자열 반환
  const token = ref(localStorage.getItem('jwt') || '')

  // 2.

  //token 소유 여부에 따라 로그인 상태를 나타 낼 isLogIn 변수 저장
  const isLogin = computed(()=>{
    return token.value ? true:false
  })

  // 회원가입 로직
  const signUp = function({userEmail, nickname, password, birthday}){
    commonApi.post('/api/account/signup', {userEmail, nickname, password, birthday})
    .then(res => {
      console.log('회원가입 성공', res.data)
      router.push({name:'LandingPageView'})
    })
    .catch(err => {
      console.log(err)
    })
  }


  //토큰 셋
  function setToken(accessToken) {
    token.value = accessToken
    localStorage.setItem('jwt', accessToken)
    commonApi.defaults.headers.common.Authorization = `Bearer ${t}`    //  토큰이 있다면 모든 요청에 인증 헤더를 자동으로 붙이도록 한다.
  }

  // 로그인 로직
  async function logIn({email, password}) {

    const formData = FormData()
    formData.append('email', email)
    formData.append('password', password)

    try {
      const res = await commonApi.post(
        '/api/auth/login',
        formData,
      { headers: { 'Content-Type' : 'multipart/form-data' }}
    )
      const {accessToken, refreshToken} = res.data.data
      setToken(res.data.token)                // 토큰 저장
      router.push({name: 'StreamingListView'})
    } catch (err) {
      console.error('로그인 실패', err)
      throw err
    }
  }

  return { isLogin, signUp, logIn, isLogin, token }
})
