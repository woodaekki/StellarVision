const SERVICE_KEY = import.meta.env.VITE_SERVICE_KEY;

export function getAstroEventInfoUrl(solYear, solMonth) {
  return `https://apis.data.go.kr/B090041/openapi/service/AstroEventInfoService/getAstroEventInfo?solYear=${solYear}&solMonth=${solMonth}&ServiceKey=${SERVICE_KEY}`;
}
