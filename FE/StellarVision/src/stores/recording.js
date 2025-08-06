import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import commonApi from '@/api/commonApi'

export const useRecordingStore = defineStore('record', () => {

  const recordingInfo = ref('')

  function setRecordingInfo({info}){
    recordingInfo.value = info
    console.log('저장합니다', recordingInfo)
  }

  return { recordingInfo, setRecordingInfo }
})

