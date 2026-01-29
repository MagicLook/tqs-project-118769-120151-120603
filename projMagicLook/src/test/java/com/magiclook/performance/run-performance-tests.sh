#!/bin/bash
# run-performance-tests.sh - Executar todos os testes de performance sequencialmente

echo "╔════════════════════════════════════════════════╗"
echo "║ MAGICLOOK - EXECUÇÃO DE TESTES DE PERFORMANCE  ║"
echo "╚════════════════════════════════════════════════╝"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Arrays para rastrear resultados
PASSED_TESTS=()
FAILED_TESTS=()

TESTS=(
    "smoke-tests/smoke-test.js:Smoke Test"
    "stress-tests/concurrent-booking-test.js:Concurrent Booking Test"
    "api-tests/availability-api-test.js:Availability API Test"
    "load-tests/database-load-test.js:Database Load Test"
    "load-tests/filter-and-search-test.js:Filter and Search Test"
    "load-tests/booking-cancellation-test.js:Booking Cancellation Test"
    "load-tests/full-journey-test.js:Full Journey Test"
    "load-tests/general-load-test.js:General Load Test"
    "stress-tests/spike-test.js:Spike Test"
)

TOTAL=${#TESTS[@]}

echo "Verificando disponibilidade da aplicação..."
if ! curl -s http://localhost:8080/magiclook/ > /dev/null; then
    echo -e "${YELLOW}⚠ Aplicação não está acessível em http://localhost:8080/magiclook/${NC}"
    echo "  Os testes continuarão mesmo assim (para GitHub Actions)."
    echo ""
else
    echo -e "${GREEN}✓ Aplicação acessível${NC}"
    echo ""
fi

echo "Executando $TOTAL teste(s) de performance (sequencialmente)..."
echo "════════════════════════════════════════════════"
echo ""

START_TIME=$(date +%s)
COUNT=1

for test_entry in "${TESTS[@]}"; do
    IFS=':' read -r test_path test_name <<< "$test_entry"
    
    echo -e "${YELLOW}[$COUNT/$TOTAL] ► $test_name${NC}"
    
    if [[ ! -f "$test_path" ]]; then
        echo -e "  ${RED}✗ Ficheiro não encontrado: $test_path${NC}"
        FAILED_TESTS+=("$test_name")
        echo ""
        ((COUNT++))
        continue
    fi
    
    if k6 run "$test_path" --quiet > /tmp/k6_${test_name// /_}.log 2>&1; then
        echo -e "  ${GREEN}✓ PASSOU${NC}"
        PASSED_TESTS+=("$test_name")
    else
        echo -e "  ${RED}✗ FALHOU${NC}"
        FAILED_TESTS+=("$test_name")
    fi
    echo ""
    ((COUNT++))
done

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))
MINUTES=$((DURATION / 60))
SECONDS=$((DURATION % 60))

echo "════════════════════════════════════════════════"
echo ""
echo "RESULTADOS FINAIS:"
echo -e "  ${GREEN}✓ Passaram: ${#PASSED_TESTS[@]}/${TOTAL}${NC}"
if [[ ${#PASSED_TESTS[@]} -gt 0 ]]; then
    for test in "${PASSED_TESTS[@]}"; do
        echo -e "      ${GREEN}✓${NC} $test"
    done
fi

echo ""
echo -e "  ${RED}✗ Falharam: ${#FAILED_TESTS[@]}/${TOTAL}${NC}"
if [[ ${#FAILED_TESTS[@]} -gt 0 ]]; then
    for test in "${FAILED_TESTS[@]}"; do
        echo -e "      ${RED}✗${NC} $test"
    done
fi

echo ""
echo "Duração total: ${MINUTES}m ${SECONDS}s"
echo ""

if [[ ${#FAILED_TESTS[@]} -eq 0 ]]; then
    echo -e "${GREEN}✓ Todos os testes executados com sucesso!${NC}"
    exit 0
else
    echo -e "${RED}✗ ${#FAILED_TESTS[@]} teste(s) falharam:${NC}"
    for test in "${FAILED_TESTS[@]}"; do
        echo -e "  - $test"
    done
    exit 1
fi
