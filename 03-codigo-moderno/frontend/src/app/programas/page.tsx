/**
 * Página de programas sociais: aba de Cadastro.
 * Cadastro: POST /api/v1/social-programs (REQ-007).
 * source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN
 */

import { SocialProgramForm } from "@/components/SocialProgramForm";
import { SocialProgramSearch } from "@/components/SocialProgramSearch";
import { Tabs } from "@/components/Tabs";

export default function ProgramasPage() {
  return (
    <div>
      <h2 className="text-2xl font-semibold mb-6">Programas Sociais</h2>
      <Tabs
        tabs={[
          { id: "consulta", label: "Consultar", content: <SocialProgramSearch /> },
          { id: "cadastro", label: "Cadastrar", content: <SocialProgramForm /> },
        ]}
      />
    </div>
  );
}
