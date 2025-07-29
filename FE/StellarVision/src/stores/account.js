import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import axios from 'axios'
import commonApi from '@/api/commonApi'


export const useAccountStore = defineStore('account', () => {
  // const ACCOUNT_URL = 'http://127.0.0.1:8080/accounts'   -> axios 생성 대체
  const router = useRouter()
  const token = ref('')

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

  return { isLogin, signUp }
})
