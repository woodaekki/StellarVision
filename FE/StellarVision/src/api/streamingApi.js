import axios from 'axios'

const streamingApi = axios.create({
  baseURL: 'https://i13c106.p.ssafy.io:8443',
})

export default streamingApi
