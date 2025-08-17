// // .env 파일을 안 만들어서 데이터 정보 띄우는 것만 보고, 키는 삭제할 예정
// export const SERVICE_KEY = '';

// export function getAstroEventInfoUrl(solYear, solMonth) {
//   return `http://apis.data.go.kr/B090041/openapi/service/AstroEventInfoService/getAstroEventInfo?solYear=${solYear}&solMonth=${solMonth}&ServiceKey=${SERVICE_KEY}`;
// }

const SERVICE_KEY = import.meta.env.VITE_SERVICE_KEY;

export function getAstroEventInfoUrl(solYear, solMonth) {
  return `https://apis.data.go.kr/B090041/openapi/service/AstroEventInfoService/getAstroEventInfo?solYear=${solYear}&solMonth=${solMonth}&ServiceKey=${SERVICE_KEY}`;
}
