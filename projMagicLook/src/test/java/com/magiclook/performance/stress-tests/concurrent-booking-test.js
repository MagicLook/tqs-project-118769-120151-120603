import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// Métricas customizadas
const successfulBookings = new Counter('successful_bookings');
const failedBookings = new Counter('failed_bookings');
const conflictBookings = new Counter('conflict_bookings');

export const options = {
  scenarios: {
    booking_war: {
      executor: 'per-vu-iterations',
      vus: 50,          // 50 pessoas a tentar reservar
      iterations: 10,   // Cada uma tenta 10 vezes
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<1500'],
    successful_bookings: ['count>=1'], // Pelo menos 1 deve conseguir
  },
};

export default function () {
  // TODOS tentam reservar o MESMO item nas MESMAS datas
  const targetItem = 123; // ID de um item popular
  const targetSize = 'M';
  const startDate = '2024-12-15';
  const endDate = '2024-12-17';
  
  // Headers com session fake (ou usar login real)
  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded',
    'X-Test-User': `user-${__VU}-${__ITER}`,
  };
  
  // 1. Verificar disponibilidade
  const checkRes = http.get(
    `http://localhost:8080/magiclook/api/availability?itemId=${targetItem}&size=${targetSize}`,
    { headers }
  );
  
  if (checkRes.status !== 200) {
    failedBookings.add(1);
    return;
  }
  
  // 2. Tentar reserva
  const bookingRes = http.post(
    'http://localhost:8080/magiclook/booking/create',
    {
      itemId: targetItem.toString(),
      size: targetSize,
      startUseDate: startDate,
      endUseDate: endDate,
    },
    { headers }
  );
  
  // Análise dos resultados
  if (bookingRes.status === 200) {
    successfulBookings.add(1);
    console.log(`VU ${__VU} conseguiu reservar!`);
  } else if (bookingRes.status === 409 || bookingRes.status === 400) {
    conflictBookings.add(1);
    console.log(`VU ${__VU} - Conflito (já reservado)`);
  } else {
    failedBookings.add(1);
    console.log(`VU ${__VU} - Erro: ${bookingRes.status}`);
  }
  
  sleep(0.5);
}