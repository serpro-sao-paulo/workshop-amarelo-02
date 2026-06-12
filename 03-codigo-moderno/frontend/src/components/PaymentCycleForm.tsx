"use client";
/**
 * Formulário de geração de pagamento do ciclo mensal.
 * Consome POST /api/v1/payment-cycles?cpf={cpf}&competence={comp}
 * REQ-009 (só ativos), REQ-010 (idempotência), REQ-021 (status G).
 */

import { useState } from "react";

interface GeneratedPayment {
  paymentNumber: number;
  competence: string;
  grossAmount: number;
  netAmount: number;
  totalDiscount: number;
  status: string;
  paymentType: string;
}

function formatBRL(value: number) {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(value);
}

export function PaymentCycleForm() {
  const [cpf, setCpf] = useState("");
  const [competence, setCompetence] = useState("");
  const [result, setResult] = useState<GeneratedPayment | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleGenerate(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setResult(null);
    if (!cpf.trim() || !competence.trim()) return;
    setLoading(true);
    try {
      const params = new URLSearchParams({
        cpf: cpf.replace(/\D/g, ""),
        competence: competence.replace(/\D/g, ""),
      });
      const res = await fetch(`/api/v1/payment-cycles?${params}`, { method: "POST" });
      if (res.status === 409) {
        setError("Pagamento já existe para esta competência (idempotência).");
        return;
      }
      if (!res.ok) {
        const body = await res.text();
        setError(body || "Não foi possível gerar o pagamento.");
        return;
      }
      setResult(await res.json());
    } catch {
      setError("Falha de conexão com o servidor.");
    } finally {
      setLoading(false);
    }
  }

  const inputClass =
    "px-4 py-2 rounded-lg bg-slate-800 border border-slate-600 " +
    "focus:outline-none focus:border-blue-500";

  return (
    <div className="max-w-lg">
      <form onSubmit={handleGenerate} className="space-y-4 mb-6">
        <div>
          <label className="block text-slate-400 text-xs mb-1" htmlFor="cpf-cycle">
            CPF do beneficiário
          </label>
          <input id="cpf-cycle" value={cpf} onChange={(e) => setCpf(e.target.value)}
            aria-label="CPF" placeholder="Somente números"
            className={`${inputClass} w-full`} />
        </div>
        <div>
          <label className="block text-slate-400 text-xs mb-1" htmlFor="comp-cycle">
            Competência (AAAAMM)
          </label>
          <input id="comp-cycle" value={competence} onChange={(e) => setCompetence(e.target.value)}
            aria-label="Competência" placeholder="Ex: 202506"
            className={`${inputClass} w-full`} />
        </div>
        <button type="submit" disabled={loading}
          className="px-6 py-2 bg-blue-600 rounded-lg hover:bg-blue-500
                     disabled:opacity-50 transition-colors">
          {loading ? "Gerando..." : "Gerar Pagamento"}
        </button>
      </form>

      {error && <div role="alert" className="text-red-400 mb-4">{error}</div>}

      {result && (
        <div className="bg-slate-800 rounded-xl p-5 border border-green-700/50 space-y-2">
          <p className="text-green-400 font-medium mb-2">Pagamento gerado com sucesso</p>
          <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
            <span className="text-slate-400">Nº Pagamento</span>
            <span className="font-mono">{result.paymentNumber}</span>
            <span className="text-slate-400">Competência</span>
            <span className="font-mono">{result.competence}</span>
            <span className="text-slate-400">Bruto</span>
            <span>{formatBRL(result.grossAmount)}</span>
            <span className="text-slate-400">Desconto</span>
            <span className="text-red-400">{formatBRL(result.totalDiscount)}</span>
            <span className="text-slate-400">Líquido</span>
            <span className="text-green-400">{formatBRL(result.netAmount)}</span>
            <span className="text-slate-400">Situação</span>
            <span>{result.status}</span>
          </div>
        </div>
      )}
    </div>
  );
}
