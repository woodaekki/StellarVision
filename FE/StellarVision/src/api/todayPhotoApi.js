// 로컬 기준으로 시간 계산
function getLocalDateString(date = new Date()) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

// NASA로부터 사진을 가져오는 요청
export async function fetchPhoto(date) {
  const apiKey = import.meta.env.VITE_NASA_SERVICE_KEY;
  const url = `https://api.nasa.gov/planetary/apod?api_key=${apiKey}&date=${date}`;

  try {
    const response = await fetch(url);

    if (!response.ok) {
      console.error(`APOD fetch failed: (Status ${response.status}, ${response.statusText})`);
      return null;
    }

    return await response.json();
  } catch (error) {
    console.error('APOD fetch error:', error);
    return null;
  }
}

// 오늘 혹은 어제의 사진을 불러옴
export async function getTodayOrYesterdayAPOD() {
  const now = new Date();
  const todayDate = getLocalDateString(now); // 캐시 및 표시용 로컬 날짜

  // NASA 서버 시간 기준으로 날짜가 바뀌었는지 확인
  const useYesterday = now.getUTCHours() < 5;
  const targetDateUTC = useYesterday
    ? new Date(now.getTime() - 86400000).toISOString().split('T')[0]
    : new Date().toISOString().split('T')[0];

  // null값을 캐싱해왔을 경우 제거
  const getValidCache = (key) => {
    const cached = localStorage.getItem(key);
    if (!cached) return null;
    const parsed = JSON.parse(cached);
    return parsed?.url && parsed?.media_type ? parsed : null;
  };

  // 캐싱된 정보가 오늘 날짜인지 확인 (로컬 날짜 기준)
  const cacheKeyToday = `apod-${todayDate}`;
  let apodData = getValidCache(cacheKeyToday);

  // 캐싱된 정보가 없으면 API 요청 시도
  if (!apodData) {
    apodData = await fetchPhoto(targetDateUTC);

    // 오늘 날짜로 받은 값이 없을 경우 어제 날짜 정보를 호출
    if (!apodData && !useYesterday) {
      const yesterdayDate = getLocalDateString(new Date(now.getTime() - 86400000));

      const cacheKeyYesterday = `apod-${yesterdayDate}`;
      apodData = getValidCache(cacheKeyYesterday) || (await fetchPhoto(yesterdayDate));

      if (apodData) {
        localStorage.setItem(cacheKeyYesterday, JSON.stringify(apodData));
        return { data: apodData, isYesterday: true };
      }

      return { data: null, isYesterday: true };
    }

    // 유효한 정보일 때만 캐싱 (로컬 날짜 기준)
    if (apodData) {
      localStorage.setItem(cacheKeyToday, JSON.stringify(apodData));
    }
  }

  return { data: apodData, isYesterday: apodData?.date !== todayDate};
}

