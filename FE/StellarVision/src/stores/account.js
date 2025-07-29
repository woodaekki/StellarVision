import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import axios from 'axios'


export const useAccountStore = defineStore('account', () => {
  const ACCOUNT_URL = 'http://127.0.0.1:8080/accounts'
  const router = useRouter()
  const token = ref('')

  //token 소유 여부에 따라 로그인 상태를 나타 낼 isLogIn 변수 저장
  const isLogin = computed(()=>{
    return token.value ? true:false
  })

  // 회원가입 로직
  const signUp = function({userEmail, password, birthday}){
    axios({
      method:'POST',
      url: `${ACCOUNT_URL}/api/signup`,
      data:{userEmail, password, birthday}
    })
    .then(res => {
      console.log('회원가입 성공')
      router.push({name:'LandingPageView'})
    })
    .catch(err => {
      console.log(err)
    })
  }

  return { isLogin, signUp }
})
