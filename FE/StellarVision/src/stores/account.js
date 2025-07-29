import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import commonApi from '@/api/commonApi'
import { jwtDecode } from 'jwt-decode'

export const useAccountStore = defineStore('account', () => {
  // const ACCOUNT_URL = 'http://127.0.0.1:8080/accounts'   -> axios 생성 대체
  const router = useRouter()
  // 1. _tokens를 선언하고 getter 제공
  const _tokens = ref({})     //실제 토큰에 대한 정보
  const tokens = computed(()=> _tokens.value) //외부에 오픈할 정보

  // 2. 

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
})
