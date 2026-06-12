"use client";
/**
 * Componente de busca de programa social por código.
 * Consome GET /api/v1/social-programs/{code} (REQ-007).
 * Client Component (interatividade de formulário).
 */

import { useState } from "react";

interface CalculationTier {
  occurrenceIndex: number;
  incomeFrom: number | null;
  incomeTo: number | null;
  multiplierFactor: number | null;
  additionalValue: number | null;
  cumulativeFlag: string | null;
}

interface RegionalParam {
  occurrenceIndex: number;
  regionCode: string | null;
  regionalFactor: number | null;
  regionalComplement: number | null;
  activeFlag: string | null;
}

interface SocialProgram {
  programCode: string;
  name: string;
  acronym: string | null;
  programType: string;
  status: string;
  responsibleAgency: string | null;
  baseValueIndividual: number | null;
  maxPerCapitaIncome: number | null;
  minAge: number | null;
  maxAge: number | null;
  applicableDiscountTypes: string[] | null;
  calculationTiers: CalculationTier[] | null;
  regionalParams: RegionalParam[] | null;
}

const TYPE_LABELS: Record<string, string> = {
  A: "Assistencial",
  T: "Trabalho",
  P: "Previdenciário",
};

const STATUS_LABELS: Record<string, string> = {
  A: "Ativo",
  I: "Inativo",
  E: "Encerrado",
};

function fmtMoney(value: number | null): string {
  if (value === null || value === undefined) return "—";
  return value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export function SocialProgramSearch() {
  const [code, setCode] = useState("");
  const [result, setResult] = useState<SocialProgram | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setResult(null);
    if (!code.trim()) return;
    setLoading(true);
    try {
      const res = await fetch(`/api/v1/social-programs/${encodeURIComponent(code.trim())}`);
      if (res.status === 404) {
        setError("Programa não encontrado.");
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

  const tiers = result?.calculationTiers ?? [];
  const regionals = result?.regionalParams ?? [];
  const discounts = result?.applicableDiscountTypes ?? [];

  return (
    <div className="max-w-3xl">
      <form onSubmit={handleSearch} className="flex gap-2 mb-6 max-w-md">
        <input
          type="text"
          value={code}
          onChange={(e) => setCode(e.target.value)}
          placeholder="Digite o código do programa"
          aria-label="Código do programa"
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

      {error && <div role="alert" className="text-red-400 mb-4">{error}</div>}

      {result && (
        <div className="space-y-6">
          <div className="bg-slate-800 rounded-xl p-5 border border-slate-700
                          grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <span className="text-slate-400 text-sm">Código</span>
              <p className="font-mono">{result.programCode}</p>
            </div>
            <div>
              <span className="text-slate-400 text-sm">Sigla</span>
              <p>{result.acronym ?? "—"}</p>
            </div>
            <div className="md:col-span-2">
              <span className="text-slate-400 text-sm">Nome</span>
              <p>{result.name}</p>
            </div>
            <div>
              <span className="text-slate-400 text-sm">Tipo</span>
              <p>{TYPE_LABELS[result.programType] ?? result.programType}</p>
            </div>
            <div>
              <span className="text-slate-400 text-sm">Situação</span>
              <p>{STATUS_LABELS[result.status] ?? result.status}</p>
            </div>
            <div>
              <span className="text-slate-400 text-sm">Órgão Responsável</span>
              <p>{result.responsibleAgency ?? "—"}</p>
            </div>
            <div>
              <span className="text-slate-400 text-sm">Valor Base Individual</span>
              <p>{fmtMoney(result.baseValueIndividual)}</p>
            </div>
            <div>
              <span className="text-slate-400 text-sm">Renda Máx. per capita</span>
              <p>{fmtMoney(result.maxPerCapitaIncome)}</p>
            </div>
            <div>
              <span className="text-slate-400 text-sm">Faixa Etária</span>
              <p>{result.minAge ?? 0} a {result.maxAge ?? 0} anos</p>
            </div>
            <div className="md:col-span-2">
              <span className="text-slate-400 text-sm">Tipos de Desconto</span>
              <p>{discounts.length > 0 ? discounts.join(", ") : "—"}</p>
            </div>
          </div>

          {tiers.length > 0 && (
            <div>
              <h3 className="text-lg font-medium text-blue-400 mb-2">Faixas de Cálculo</h3>
              <div className="overflow-x-auto">
                <table className="w-full text-sm border border-slate-700 rounded-lg">
                  <thead className="bg-slate-800 text-slate-400">
                    <tr>
                      <th className="px-3 py-2 text-left">Ordem</th>
                      <th className="px-3 py-2 text-left">Renda Início</th>
                      <th className="px-3 py-2 text-left">Renda Fim</th>
                      <th className="px-3 py-2 text-left">Fator Mult.</th>
                      <th className="px-3 py-2 text-left">Vlr Adicional</th>
                      <th className="px-3 py-2 text-left">Acumula</th>
                    </tr>
                  </thead>
                  <tbody>
                    {tiers.map((t) => (
                      <tr key={t.occurrenceIndex} className="border-t border-slate-700">
                        <td className="px-3 py-2">{t.occurrenceIndex}</td>
                        <td className="px-3 py-2">{fmtMoney(t.incomeFrom)}</td>
                        <td className="px-3 py-2">{fmtMoney(t.incomeTo)}</td>
                        <td className="px-3 py-2">{t.multiplierFactor ?? "—"}</td>
                        <td className="px-3 py-2">{fmtMoney(t.additionalValue)}</td>
                        <td className="px-3 py-2">{t.cumulativeFlag === "S" ? "Sim" : "Não"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {regionals.length > 0 && (
            <div>
              <h3 className="text-lg font-medium text-blue-400 mb-2">Parâmetros Regionais</h3>
              <div className="overflow-x-auto">
                <table className="w-full text-sm border border-slate-700 rounded-lg">
                  <thead className="bg-slate-800 text-slate-400">
                    <tr>
                      <th className="px-3 py-2 text-left">Ordem</th>
                      <th className="px-3 py-2 text-left">Cód. Região</th>
                      <th className="px-3 py-2 text-left">Fator Regional</th>
                      <th className="px-3 py-2 text-left">Complemento</th>
                      <th className="px-3 py-2 text-left">Ativo</th>
                    </tr>
                  </thead>
                  <tbody>
                    {regionals.map((r) => (
                      <tr key={r.occurrenceIndex} className="border-t border-slate-700">
                        <td className="px-3 py-2">{r.occurrenceIndex}</td>
                        <td className="px-3 py-2">{r.regionCode ?? "—"}</td>
                        <td className="px-3 py-2">{r.regionalFactor ?? "—"}</td>
                        <td className="px-3 py-2">{fmtMoney(r.regionalComplement)}</td>
                        <td className="px-3 py-2">{r.activeFlag === "S" ? "Sim" : "Não"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
