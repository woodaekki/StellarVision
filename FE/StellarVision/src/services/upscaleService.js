// src/services/upscaleService.js
import axios from 'axios'

// 필요에 맞게 교체!
const UPSCALE_URL = 'https://susang-fastapi.my/api/upscale/photo' 

export function createUpscaleService() {
  const client = axios.create({
    baseURL: UPSCALE_URL,
    timeout: 120000,
  })

  return {
    /**
     * 원본 이미지(Blob/File)를 업로드하면 업스케일된 "이미지 Blob"을 준다고 가정
     */
    async upscaleImage(imageBlob, filename = 'capture.jpg') {
      const fd = new FormData()
      fd.append('file', imageBlob, filename)

      const res = await client.post('', fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
        responseType: 'blob', // 🔑 blob로 받아야 이미지 데이터 확보 가능
      })
      return res.data // Blob
    },
  }
}
