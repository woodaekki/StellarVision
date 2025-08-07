import { latLngToPosition } from "./latLngToPosition";

export function adjustPinPosition(pins, radius, center) {
  const threshold = 6; // lat/lng 총합 차이 기준
  const adjusted = [];

  pins.forEach((pin, idx) => {
   // latitude/longitude 우선, 이미 조정된 lat/lng(p.lat, p.lng)이 있으면 그걸 사용
    const lat0 = pin.lat  ?? pin.latitude;
   const lng0 = pin.lng  ?? pin.longitude;
   const similar = adjusted.filter(p =>
     Math.abs(p.lat - lat0) + Math.abs(p.lng - lng0) < threshold
   );
   const offset = similar.length * 0.5;
   const angle = idx * 36 * (Math.PI / 180);
   const newLat = lat0 + Math.cos(angle) * offset;
   const newLng = lng0 + Math.sin(angle) * offset;
   adjusted.push({
     ...pin,
     lat: newLat,
     lng: newLng,
     _adjustedPosition: latLngToPosition(newLat, newLng, radius, center)
   });
 });

  return adjusted;
}
