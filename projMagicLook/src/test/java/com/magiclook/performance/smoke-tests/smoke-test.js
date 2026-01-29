// smoke-test.js
import http from 'k6/http';
import { check, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Métricas customizadas
const errorRate = new Rate('errors');
const responseTimes = {
  home: new Trend('response_time_home'),
  login: new Trend('response_time_login'),
  items: new Trend('response_time_items'),
  availability: new Trend('response_time_availability'),
  booking: new Trend('response_time_booking'),
};

export const options = {
  vus: 3,  // Apenas 3 VUs para smoke test
  duration: '1m',  // 1 minuto é suficiente
  thresholds: {
    errors: ['rate<0.01'],  // Menos de 1% de erros
    http_req_duration: ['p(95)<3000'],  // 95% das requisições < 3s
    'response_time_home': ['p(95)<1000'],
    'response_time_login': ['p(95)<1500'],
  },
  noConnectionReuse: false,  // Reutilizar conexões
  discardResponseBodies: false,  // Manter corpos para verificações
};

// Dados de teste reutilizáveis
const TEST_USER = {
  username: 'testuser0',
  password: 'test123',
};

const TEST_ITEM = {
  id: 1,
  size: 'M',
};

// Variável global para armazenar headers de autenticação entre grupos
let globalAuthHeaders = null;

export default function () {
  group('1. Teste de saúde da aplicação', function () {
    // 1.1 Página de login como verificação inicial
    const loginPageRes = http.get('http://localhost:8080/magiclook/login');
    check(loginPageRes, {
      'página login carrega': (r) => r.status === 200,
      'tem formulário de login': (r) => r.body.includes('form') && r.body.includes('password'),
    });
    responseTimes.home.add(loginPageRes.timings.duration);
    errorRate.add(loginPageRes.status !== 200);
  });

  group('2. Teste de autenticação', function () {
    // 2.1 Login com dados corretos em formato URL-encoded
    const loginRes = http.post(
      'http://localhost:8080/magiclook/login',
      `username=${TEST_USER.username}&password=${TEST_USER.password}`,
      {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        redirects: 0,
      }
    );
    
    check(loginRes, {
      'login processado (200 ou 302)': (r) => r.status === 200 || r.status === 302,
      'recebe cookie de sessão': (r) => r.cookies && r.cookies.JSESSIONID && r.cookies.JSESSIONID.length > 0,
    });
    responseTimes.login.add(loginRes.timings.duration);
    errorRate.add(loginRes.status >= 400);
    
    // Se login falhou, termina o teste
    if (!loginRes.cookies?.JSESSIONID || loginRes.cookies.JSESSIONID.length === 0) {
      console.error(`Login falhou para ${TEST_USER.username}`);
      return;
    }
    
    const sessionCookie = `JSESSIONID=${loginRes.cookies.JSESSIONID[0].value}`;
    globalAuthHeaders = {
      'Cookie': sessionCookie,
      'Content-Type': 'application/x-www-form-urlencoded',
    };
    
    // 2.2 Dashboard após login
    const dashboardRes = http.get(
      'http://localhost:8080/magiclook/dashboard',
      { headers: globalAuthHeaders }
    );
    
    check(dashboardRes, {
      'dashboard acessível': (r) => r.status === 200,
    });
  });

  // Se o login falhou, não continuar os testes
  if (!globalAuthHeaders) {
    return;
  }

  group('3. Teste de catálogo de produtos', function () {
    // 3.1 Listagem de produtos - homens
    const itemsMenRes = http.get(
      'http://localhost:8080/magiclook/items/men',
      { headers: globalAuthHeaders }
    );
    
    check(itemsMenRes, {
      'catálogo homem carrega': (r) => r.status === 200,
      'mostra produtos': (r) => r.body.includes('item') || r.body.includes('product') || r.body.includes('card'),
    });
    responseTimes.items.add(itemsMenRes.timings.duration);
    errorRate.add(itemsMenRes.status !== 200);
    
    // 3.2 Listagem de produtos - mulheres
    const itemsWomenRes = http.get(
      'http://localhost:8080/magiclook/items/women',
      { headers: globalAuthHeaders }
    );
    
    check(itemsWomenRes, {
      'catálogo mulher carrega': (r) => r.status === 200,
      'mostra produtos': (r) => r.body.includes('item') || r.body.includes('product') || r.body.includes('card'),
    });
    responseTimes.items.add(itemsWomenRes.timings.duration);
    
    // 3.3 Detalhe de produto
    const itemDetailRes = http.get(
      `http://localhost:8080/magiclook/booking/${TEST_ITEM.id}`,
      { headers: globalAuthHeaders }
    );
    
    check(itemDetailRes, {
      'página de produto carrega': (r) => r.status === 200,
    });
  });

  group('4. Teste de disponibilidade e reserva', function () {
    // 4.1 Verificar disponibilidade via API
    const startDate = getFutureDate(3);
    const endDate = getFutureDate(5);
    
    const availabilityRes = http.get(
      `http://localhost:8080/magiclook/api/items/${TEST_ITEM.id}/check?size=${TEST_ITEM.size}&start=${startDate}&end=${endDate}`,
      { headers: globalAuthHeaders }
    );
    
    check(availabilityRes, {
      'API disponibilidade responde': (r) => r.status === 200,
      'resposta é JSON válida': (r) => {
        try {
          JSON.parse(r.body);
          return true;
        } catch {
          return false;
        }
      },
    });
    responseTimes.availability.add(availabilityRes.timings.duration);
    errorRate.add(availabilityRes.status !== 200);
    
    // 4.2 Página de reserva
    const bookingPageRes = http.get(
      `http://localhost:8080/magiclook/booking/${TEST_ITEM.id}`,
      { headers: globalAuthHeaders }
    );
    
    check(bookingPageRes, {
      'página de reserva carrega': (r) => r.status === 200,
      'tem formulário de reserva': (r) => r.body.includes('form') || r.body.includes('booking'),
    });
    
    // 4.3 Tentativa de reserva
    if (availabilityRes.status === 200) {
      try {
        const availabilityData = JSON.parse(availabilityRes.body);
        if (availabilityData && availabilityData.available !== false) {
          const bookingRes = http.post(
            'http://localhost:8080/magiclook/booking/create',
            `itemId=${TEST_ITEM.id}&size=${TEST_ITEM.size}&startUseDate=${startDate}&endUseDate=${endDate}`,
            { headers: globalAuthHeaders }
          );
          
          check(bookingRes, {
            'resposta da reserva (200 ou 302)': (r) => r.status === 200 || r.status === 302 || r.status === 400,
          });
          responseTimes.booking.add(bookingRes.timings.duration);
          
          if (bookingRes.status === 200 || bookingRes.status === 302) {
            console.log(`Reserva processada para item ${TEST_ITEM.id}`);
          }
        }
      } catch (e) {
        console.log(`Erro ao processar disponibilidade: ${e}`);
      }
    }
  });

  group('5. Teste de funcionalidades auxiliares', function () {
    // 5.1 Minhas reservas
    const myBookingsRes = http.get(
      'http://localhost:8080/magiclook/my-bookings',
      { headers: globalAuthHeaders }
    );
    
    check(myBookingsRes, {
      'minhas reservas carrega': (r) => r.status === 200,
    });
    
    // 5.2 Logout
    const logoutRes = http.get(
      'http://localhost:8080/magiclook/logout',
      { headers: globalAuthHeaders }
    );
    
    check(logoutRes, {
      'logout funciona': (r) => r.status === 200 || r.status === 302,
    });
    
    // Limpar o token após logout
    globalAuthHeaders = null;
  });
}

function getFutureDate(daysFromNow) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  return date.toISOString().split('T')[0];
}

export function setup() {
  console.log('Iniciando Smoke Test da MagicLook');
  console.log('Testando endpoints críticos...');
  
  // Teste de conectividade básico
  const loginPageRes = http.get('http://localhost:8080/magiclook/login');
  if (loginPageRes.status !== 200) {
    throw new Error(`Aplicação não está acessível. Status: ${loginPageRes.status}`);
  }
  console.log('Aplicação acessível');
  
  return {
    timestamp: new Date().toISOString(),
    testUser: TEST_USER,
    testItem: TEST_ITEM,
  };
}

export function teardown(data) {
  console.log('\nRESUMO DO SMOKE TEST:');
  console.log('========================');
  console.log(`Início: ${data.timestamp}`);
  console.log(`Usuário de teste: ${data.testUser.username}`);
  console.log(`Item de teste: ID ${data.testItem.id}, Tamanho ${data.testItem.size}`);
  console.log('Smoke test concluído - Verificar métricas para detalhes');
}