import { defineStore } from 'pinia';

export const useStreamingStore = defineStore('streaming', {
  state: () => ({
    roomId: null,
    userName: ''
  }),
  actions: {
    setRoomInfo({ roomId, userName }) {
      this.roomId = roomId;
      this.userName = userName;
    },
    clearRoomInfo() {
      this.roomId = null;
      this.userName = '';
    }
  }
});
