import axios from 'axios'

const streamingApi = axios.create({
  baseURL: 'https://i13c106.p.ssafy.io',
})

export default streamingApi
