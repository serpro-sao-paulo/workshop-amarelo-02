"use client";
/**
 * Componente de busca de beneficiário por CPF.
 * Client Component (requer interatividade de formulário).
 * REQ-006: exibe CPF mascarado conforme retornado pela API.
 */

import { useState } from "react";

interface BeneficiarySummary {
  maskedCpf: string;
  name: string;
  status: string;
}

const STATUS_LABELS: Record<string, string> = {
  A: "Ativo",
  S: "Suspenso",
  C: "Cancelado",
  I: "Inativo",
  D: "Desligado",
};

export function BeneficiarySearch() {
  const [cpf, setCpf] = useState("");
  const [result, setResult] = useState<BeneficiarySummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setResult(null);
    if (!cpf.trim()) return;
    setLoading(true);
    try {
      const res = await fetch(`/api/v1/beneficiaries/${cpf.replace(/\D/g, "")}`);
      if (res.status === 404) {
        setError("Beneficiário não encontrado.");
        return;
      }
      if (!res.ok) {
        setError("Erro ao consultar o servidor.");
        return;
      }
      setResult(await res.json());
    } catch {
      setError("Falha de conexão com o servidor.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-md">
      <form onSubmit={handleSearch} className="flex gap-2 mb-6">
        <input
          type="text"
          value={cpf}
          onChange={(e) => setCpf(e.target.value)}
          placeholder="Digite o CPF"
          aria-label="CPF do beneficiário"
          className="flex-1 px-4 py-2 rounded-lg bg-slate-800 border border-slate-600
                     focus:outline-none focus:border-blue-500"
        />
        <button
          type="submit"
          disabled={loading}
          className="px-5 py-2 bg-blue-600 rounded-lg hover:bg-blue-500
                     disabled:opacity-50 transition-colors"
        >
          {loading ? "Buscando..." : "Buscar"}
        </button>
      </form>

      {error && (
        <div role="alert" className="text-red-400 mb-4">{error}</div>
      )}

      {result && (
        <div className="bg-slate-800 rounded-xl p-5 border border-slate-700 space-y-2">
          <div>
            <span className="text-slate-400 text-sm">CPF</span>
            <p className="font-mono">{result.maskedCpf}</p>
          </div>
          <div>
            <span className="text-slate-400 text-sm">Nome</span>
            <p>{result.name}</p>
          </div>
          <div>
            <span className="text-slate-400 text-sm">Situação</span>
            <p>{STATUS_LABELS[result.status] ?? result.status}</p>
          </div>
        </div>
      )}
    </div>
  );
}
