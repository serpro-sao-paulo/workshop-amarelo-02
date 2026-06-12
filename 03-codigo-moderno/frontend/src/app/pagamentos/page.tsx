/**
 * Página de pagamentos: abas de Histórico e Geração de Ciclo.
 * Histórico: GET /api/v1/payments?cpf={cpf} (REQ-011, REQ-012).
 * Geração: POST /api/v1/payment-cycles (REQ-009, REQ-010, REQ-021).
 */

import { PaymentList } from "@/components/PaymentList";
import { PaymentCycleForm } from "@/components/PaymentCycleForm";
import { Tabs } from "@/components/Tabs";

export default function PagamentosPage() {
  return (
    <div>
      <h2 className="text-2xl font-semibold mb-6">Pagamentos</h2>
      <Tabs
        tabs={[
          { id: "historico", label: "Histórico", content: <PaymentList /> },
          { id: "gerar", label: "Gerar Ciclo", content: <PaymentCycleForm /> },
        ]}
      />
    </div>
  );
}
