<template>
  <div>
    <input type="file" @change="onFileChange" />
    <button @click="uploadPhoto" :disabled="!selectedFile">업로드</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      memberId: 1,           // 가상의 멤버 아이디
      selectedFile: null,
    };
  },
  methods: {
    // 파일 선택 시 호출됨
    onFileChange(event) {
      this.selectedFile = event.target.files[0];  // 선택한 파일 저장
    },

    // 업로드 프로세스 시작
    uploadPhoto() {
      if (!this.selectedFile) {
        alert('파일을 선택하세요.');
        return;
      }

      // 1. Presigned URL 발급 요청
      this.getPresignedUrl(this.memberId, this.selectedFile.name)
        .then(({ uploadUrl, s3Key }) => {
          // 2. 받은 Presigned URL에 파일 업로드 (PUT 요청)
          return this.uploadFileToS3(uploadUrl, this.selectedFile).then(() => s3Key);
          // 업로드 성공 후 s3Key를 다음 then으로 전달
        })
        .then((s3Key) => {
          // 3. 업로드 완료 API 호출 (백엔드에 업로드 완료 알림)
          return this.notifyUploadComplete(this.memberId, this.selectedFile.name, s3Key);
        })
        .then((result) => {
          alert('업로드 성공: ' + result.message);
        })
        .catch((err) => {
          alert('업로드 실패: ' + err.message);
        });
    },

    // Presigned URL 요청 함수
    getPresignedUrl(memberId, originalFilename) {
      return fetch('/api/photos/presignedUrl', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ memberId, originalFilename }),
      })
        .then((res) => {
          if (!res.ok) throw new Error('Presigned URL 요청 실패');
          return res.json();
        })
        .then((data) => data.data);  // 실제 데이터만 반환 (uploadUrl, s3Key)
    },

    // S3에 파일 업로드 함수
    uploadFileToS3(uploadUrl, file) {
      return fetch(uploadUrl, {
        method: 'PUT',
        headers: { 'Content-Type': file.type },  // 확장자에 맞는 Content-Type
        body: file,
      }).then((res) => {
        if (!res.ok) throw new Error('S3 업로드 실패');
      });
    },

    // 업로드 완료 알림 함수
    notifyUploadComplete(memberId, originalFilename, s3Key) {
      return fetch('/api/photos/complete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ memberId, originalFilename, s3Key }),
      })
        .then((res) => {
          if (!res.ok) throw new Error('업로드 완료 알림 실패');
          return res.json();
        });
    },
  },
};
</script>
