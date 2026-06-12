/**
 * Página de beneficiários: abas de Consulta e Cadastro.
 * Consulta: GET /api/v1/beneficiaries/{cpf} (REQ-006 — CPF mascarado).
 * Cadastro: POST /api/v1/beneficiaries (REQ-001..REQ-004).
 */

import { BeneficiarySearch } from "@/components/BeneficiarySearch";
import { BeneficiaryForm } from "@/components/BeneficiaryForm";
import { Tabs } from "@/components/Tabs";

export default function BeneficiariosPage() {
  return (
    <div>
      <h2 className="text-2xl font-semibold mb-6">Beneficiários</h2>
      <Tabs
        tabs={[
          { id: "consulta", label: "Consultar", content: <BeneficiarySearch /> },
          { id: "cadastro", label: "Cadastrar", content: <BeneficiaryForm /> },
        ]}
      />
    </div>
  );
}
