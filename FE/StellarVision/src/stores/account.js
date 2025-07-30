import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import commonApi from '@/api/commonApi'
<<<<<<< HEAD
=======
import { jwtDecode } from 'jwt-decode'
>>>>>>> origin

export const useAccountStore = defineStore('account', () => {
  // const ACCOUNT_URL = 'http://127.0.0.1:8080/accounts'   -> axios 생성 대체
  const router = useRouter()
<<<<<<< HEAD
  // 1. 로컬 저장소에서 토큰을 가져온다. 없으면 빈 문자열 반환
  const token = ref(localStorage.getItem('jwt') || '')

  // 2.
=======
  // 1. _tokens를 선언하고 getter 제공
  const _tokens = ref({})     //실제 토큰에 대한 정보
  const tokens = computed(()=> _tokens.value) //외부에 오픈할 정보

  // 2. 
>>>>>>> origin

  //token 소유 여부에 따라 로그인 상태를 나타 낼 isLogIn 변수 저장
  const isLogin = computed(()=>{
    return token.value ? true:false
  })

  // 회원가입 로직
  const signUp = function({userEmail, nickname, password, birthday}){
    commonApi.post('/api/signup', {userEmail, nickname, password, birthday})
    .then(res => {
      console.log('회원가입 성공', res.data)
      router.push({name:'LandingPageView'})
    })
    .catch(err => {
      console.log(err)
    })
  }

<<<<<<< HEAD

  //토큰 셋
  function setToken(t) {
    token.value = t
    localStorage.setItem('jwt', t)
    commonApi.defaults.headers.common.Authorization = `Bearer ${t}`    //  토큰이 있다면 모든 요청에 인증 헤더를 자동으로 붙이도록 한다.
  }

  // 로그인 로직
  async function logIn({email, password}) {
    try {
      const res = await commonApi.post('/api/login', {email, password})
      setToken(res.data.token)                // 토큰 저장
      router.push({name: 'StreamingListView'})
    } catch (err) {
      console.error('로그인 실패', err)
      throw err
    }
  }

  return { isLogin, signUp, logIn, isLogin, token }
=======
  const logIn = function({email, password}){
    commonApi.post('/api/login', {email, password})
    .then(res =>{
      console.log('로그인 성공', res.data)
      router.push({name:'LandingPageView'})
    })
    .catch(err =>{
      console.log(err)
    })
  }

  return { isLogin, signUp, logIn, tokens }
>>>>>>> origin
})
