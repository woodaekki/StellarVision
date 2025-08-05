export async function fetchPhoto(date) {
  const apiKey = import.meta.env.VITE_NASA_SERVICE_KEY;
  const url = `https://api.nasa.gov/planetary/apod?api_key=${apiKey}&date=${date}`;

  try {
    const response = await fetch(url);

    if (!response.ok) {
      console.error(`오늘의 천체 사진 정보 호출 실패: (Status ${response.status}, ${response.statusText})`);
    }

    const json = await response.json();
    return json;

  } catch (error) {
    throw error;
  }
}
