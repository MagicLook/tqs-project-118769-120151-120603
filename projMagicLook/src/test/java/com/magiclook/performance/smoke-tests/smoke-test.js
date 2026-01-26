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
  username: 'smoketest',
  password: 'test123',
};

const TEST_ITEM = {
  id: 1,
  size: 'M',
};

export default function () {
  group('1. Teste de saúde da aplicação', function () {
    // 1.1 Página inicial
    const homeRes = http.get('http://localhost:8080/magiclook/');
    check(homeRes, {
      'página inicial responde com 200': (r) => r.status === 200,
      'contém título MagicLook': (r) => r.body.includes('MagicLook'),
      'tem navbar': (r) => r.body.includes('navbar') || r.body.includes('menu'),
    });
    responseTimes.home.add(homeRes.timings.duration);
    errorRate.add(homeRes.status !== 200);
    
    // 1.2 Página de login
    const loginPageRes = http.get('http://localhost:8080/magiclook/login');
    check(loginPageRes, {
      'página login carrega': (r) => r.status === 200,
      'tem formulário de login': (r) => r.body.includes('form') && r.body.includes('password'),
    });
  });

  group('2. Teste de autenticação', function () {
    // 2.1 Login
    const loginRes = http.post(
      'http://localhost:8080/magiclook/login',
      {
        username: TEST_USER.username,
        password: TEST_USER.password,
      },
      {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        redirects: 0,  // Não seguir redirects para verificar
      }
    );
    
    check(loginRes, {
      'login processado (200 ou 302)': (r) => r.status === 200 || r.status === 302,
      'recebe cookie de sessão': (r) => r.cookies && r.cookies.JSESSIONID,
    });
    responseTimes.login.add(loginRes.timings.duration);
    errorRate.add(loginRes.status >= 400);
    
    // Se login falhou, termina o teste
    if (!loginRes.cookies || !loginRes.cookies.JSESSIONID) {
      console.error(`Login falhou para ${TEST_USER.username}`);
      return;
    }
    
    const sessionCookie = `JSESSIONID=${loginRes.cookies.JSESSIONID[0].value}`;
    const authHeaders = {
      'Cookie': sessionCookie,
      'Content-Type': 'application/x-www-form-urlencoded',
    };
    
    // 2.2 Dashboard após login
    const dashboardRes = http.get(
      'http://localhost:8080/magiclook/dashboard',
      { headers: authHeaders }
    );
    
    check(dashboardRes, {
      'dashboard acessível': (r) => r.status === 200,
      'mostra nome do usuário': (r) => r.body.includes('Olá') || r.body.includes('Bem-vindo'),
    });
    
    return { authHeaders };
  });

  group('3. Teste de catálogo de produtos', function () {
    const authHeaders = __ENV.authHeaders || {};
    
    // 3.1 Listagem de produtos
    const categories = ['men', 'women', 'dresses', 'suits'];
    const category = categories[Math.floor(Math.random() * categories.length)];
    
    const itemsRes = http.get(
      `http://localhost:8080/magiclook/items/${category}`,
      { headers: authHeaders }
    );
    
    check(itemsRes, {
      'catálogo carrega': (r) => r.status === 200,
      'mostra produtos': (r) => r.body.includes('item') || r.body.includes('product') || r.body.includes('card'),
    });
    responseTimes.items.add(itemsRes.timings.duration);
    errorRate.add(itemsRes.status !== 200);
    
    // 3.2 Detalhe de produto
    const itemDetailRes = http.get(
      `http://localhost:8080/magiclook/items/${TEST_ITEM.id}`,
      { headers: authHeaders }
    );
    
    check(itemDetailRes, {
      'detalhe do produto carrega': (r) => r.status === 200,
      'mostra preço': (r) => r.body.includes('€') || r.body.includes('price'),
      'mostra tamanhos disponíveis': (r) => r.body.includes('size') || r.body.includes('tamanho'),
    });
    
    return { authHeaders };
  });

  group('4. Teste de disponibilidade e reserva', function () {
    const authHeaders = __ENV.authHeaders || {};
    
    // 4.1 Verificar disponibilidade
    const startDate = getFutureDate(3);
    const endDate = getFutureDate(5);
    
    const availabilityRes = http.get(
      `http://localhost:8080/magiclook/api/availability?` +
      `itemId=${TEST_ITEM.id}&size=${TEST_ITEM.size}&start=${startDate}&end=${endDate}`,
      { headers: authHeaders }
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
      `http://localhost:8080/magiclook/booking/form/${TEST_ITEM.id}`,
      { headers: authHeaders }
    );
    
    check(bookingPageRes, {
      'página de reserva carrega': (r) => r.status === 200,
      'tem calendário': (r) => r.body.includes('calendar') || r.body.includes('calendário'),
      'tem formulário de reserva': (r) => r.body.includes('form') && r.body.includes('booking'),
    });
    
    // 4.3 Tentativa de reserva (se disponível)
    const availabilityData = JSON.parse(availabilityRes.body);
    if (availabilityData.available) {
      const bookingRes = http.post(
        'http://localhost:8080/magiclook/booking/create',
        {
          itemId: TEST_ITEM.id.toString(),
          size: TEST_ITEM.size,
          startUseDate: startDate,
          endUseDate: endDate,
        },
        { headers: authHeaders }
      );
      
      check(bookingRes, {
        'resposta da reserva (qualquer status)': (r) => r.status > 0,
      });
      responseTimes.booking.add(bookingRes.timings.duration);
      
      if (bookingRes.status === 200 || bookingRes.status === 302) {
        console.log(`Reserva criada com sucesso para item ${TEST_ITEM.id}`);
      }
    } else {
      console.log(`Item ${TEST_ITEM.id} não disponível nas datas selecionadas`);
    }
  });

  group('5. Teste de funcionalidades auxiliares', function () {
    const authHeaders = __ENV.authHeaders || {};
    
    // 5.1 Minhas reservas
    const myBookingsRes = http.get(
      'http://localhost:8080/magiclook/my-bookings',
      { headers: authHeaders }
    );
    
    check(myBookingsRes, {
      'minhas reservas carrega': (r) => r.status === 200,
      'mostra lista de reservas': (r) => r.body.includes('booking') || r.body.includes('reserva'),
    });
    
    // 5.2 Perfil do usuário
    const profileRes = http.get(
      'http://localhost:8080/magiclook/profile',
      { headers: authHeaders }
    );
    
    check(profileRes, {
      'perfil carrega': (r) => r.status === 200,
      'mostra informações do usuário': (r) => r.body.includes('profile') || r.body.includes('perfil'),
    });
    
    // 5.3 Logout
    const logoutRes = http.get(
      'http://localhost:8080/magiclook/logout',
      { headers: authHeaders }
    );
    
    check(logoutRes, {
      'logout funciona': (r) => r.status === 200 || r.status === 302,
    });
  });

  group('6. Teste de APIs públicas', function () {
    // APIs que não precisam de autenticação
    
    // 6.1 API de categorias
    const categoriesRes = http.get(
      'http://localhost:8080/magiclook/api/categories'
    );
    
    check(categoriesRes, {
      'API categorias responde': (r) => r.status === 200,
      'categorias em JSON': (r) => {
        try {
          const data = JSON.parse(r.body);
          return Array.isArray(data);
        } catch {
          return false;
        }
      },
    });
    
    // 6.2 API de itens populares
    const popularRes = http.get(
      'http://localhost:8080/magiclook/api/items/popular?limit=5'
    );
    
    check(popularRes, {
      'API popular responde': (r) => r.status === 200,
      'retorna array de itens': (r) => {
        try {
          const data = JSON.parse(r.body);
          return Array.isArray(data);
        } catch {
          return false;
        }
      },
    });
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
  const pingRes = http.get('http://localhost:8080/magiclook/health');
  if (pingRes.status === 200) {
    console.log('Aplicação acessível');
  } else {
    // Tenta a página inicial
    const homeRes = http.get('http://localhost:8080/magiclook/');
    if (homeRes.status !== 200) {
      throw new Error(`Aplicação não está acessível. Status: ${homeRes.status}`);
    }
    console.log('Endpoint /health não disponível, mas aplicação responde');
  }
  
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