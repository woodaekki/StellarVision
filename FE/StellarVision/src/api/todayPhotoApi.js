export async function fetchPhoto(date) {
  // NASA로부터 api키 발급 받은 후 env에 집어넣어야 함, 추후 수정 필요
  const apiKey = 'DEMO_KEY'
  const url = `https://api.nasa.gov/planetary/apod?api_key=${apiKey}&date=${date}`

  try {
    const response = await fetch(url)
    if (!response.ok) throw new Error('Failed to fetch Today\'s photo data')
    return await response.json()
  } catch (error) {
    console.error('[Today\'s Photo Fetch Error]', error)
    throw error
  }
}
