import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import streamingApi from '@/api/streamingApi';

export const useCounterStore = defineStore('counter', () => {
  async function getToken(mySessionId) {
      const sessionId = await this.createSession(mySessionId);
      return await this.createToken(sessionId);
  }

  async function createSession(sessionId) {
      const response = await streamingApi.post('api/sessions', { customSessionId: sessionId }, {
          headers: { 'Content-Type': 'application/json', },
      });
      return response.data; // The sessionId
  }

  async function createToken(sessionId) {
      const response = await streamingApi.post('api/sessions/' + sessionId + '/connections', {}, {
          headers: { 'Content-Type': 'application/json', },
      });
      return response.data; // The token
  }
  return { getToken, createSession, createToken  }
})
