"use client";
/**
 * Componente de busca de beneficiário por CPF.
 * Client Component (requer interatividade de formulário).
 * REQ-006: exibe CPF mascarado conforme retornado pela API.
 */

import { useState } from "react";

interface Dependent {
  maskedCpf: string | null;
  name: string;
  birthDate: string | null;
  kinship: string | null;
  status: string | null;
  disabilityFlag: string | null;
}

interface BeneficiaryDetail {
  // Identificação
  maskedCpf: string;
  registrationNumber: string | null;
  fullName: string;
  motherName: string | null;
  fatherName: string | null;
  birthDate: string | null;
  gender: string | null;
  maritalStatus: string | null;
  // Documento RG
  rgNumber: string | null;
  rgIssuer: string | null;
  rgState: string | null;
  rgIssueDate: string | null;
  // Endereço
  street: string | null;
  streetNumber: string | null;
  complement: string | null;
  neighborhood: string | null;
  city: string | null;
  uf: string | null;
  zipCode: number | null;
  ibgeCode: number | null;
  regionCode: string | null;
  // Benefício
  programCode: string | null;
  registrationDate: string | null;
  benefitStartDate: string | null;
  benefitEndDate: string | null;
  status: string;
  statusReason: string | null;
  statusDate: string | null;
  // Renda familiar
  familyIncome: number | null;
  familyMembers: number | null;
  perCapitaIncome: number | null;
  // Contato
  phoneLandline: string | null;
  phoneMobile: string | null;
  email: string | null;
  // Biometria
  biometricStatus: string | null;
  biometricCollectionDate: string | null;
  biometricPostCode: string | null;
  // Dependentes
  dependents: Dependent[];
  // Controle
  createdAt: string | null;
  updatedAt: string | null;
}

const STATUS_LABELS: Record<string, string> = {
  A: "Ativo",
  S: "Suspenso",
  C: "Cancelado",
  I: "Inativo",
  D: "Desligado",
};

const GENDER_LABELS: Record<string, string> = {
  M: "Masculino",
  F: "Feminino",
  I: "Indefinido",
};

const MARITAL_STATUS_LABELS: Record<string, string> = {
  S: "Solteiro(a)",
  C: "Casado(a)",
  D: "Divorciado(a)",
  V: "Viúvo(a)",
  U: "União estável",
};

const KINSHIP_LABELS: Record<string, string> = {
  FI: "Filho(a)",
  CJ: "Cônjuge",
  NT: "Neto(a)",
  TU: "Tutelado(a)",
};

const DEPENDENT_STATUS_LABELS: Record<string, string> = {
  A: "Ativo",
  I: "Inativo",
  D: "Desligado",
};

const BIOMETRIC_STATUS_LABELS: Record<string, string> = {
  S: "Coletada",
  N: "Não coletada",
  P: "Pendente",
};

function labelFor(map: Record<string, string>, value: string | null) {
  if (!value) return "—";
  return map[value] ?? value;
}

function text(value: string | null) {
  return value && value.trim() !== "" ? value : "—";
}

function formatDate(value: string | null) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString("pt-BR");
}

function formatBRL(value: number | null) {
  if (value === null || value === undefined) return "—";
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

function formatZip(value: number | null) {
  if (value === null || value === undefined) return "—";
  const s = String(value).padStart(8, "0");
  return `${s.slice(0, 5)}-${s.slice(5)}`;
}

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <span className="text-slate-400 text-sm">{label}</span>
      <p className="break-words">{value}</p>
    </div>
  );
}

function Section({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <section className="bg-slate-800 rounded-xl p-5 border border-slate-700">
      <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-300 mb-4">
        {title}
      </h3>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-3">
        {children}
      </div>
    </section>
  );
}

export function BeneficiarySearch() {
  const [cpf, setCpf] = useState("");
  const [result, setResult] = useState<BeneficiaryDetail | null>(null);
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
    <div className="max-w-4xl">
      <form onSubmit={handleSearch} className="flex gap-2 mb-6 max-w-md">
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
        <div className="space-y-5">
          <Section title="Identificação">
            <Field label="CPF" value={<span className="font-mono">{result.maskedCpf}</span>} />
            <Field label="Nome completo" value={text(result.fullName)} />
            <Field label="Nº de inscrição" value={text(result.registrationNumber)} />
            <Field label="Data de nascimento" value={formatDate(result.birthDate)} />
            <Field label="Nome da mãe" value={text(result.motherName)} />
            <Field label="Nome do pai" value={text(result.fatherName)} />
            <Field label="Sexo" value={labelFor(GENDER_LABELS, result.gender)} />
            <Field
              label="Estado civil"
              value={labelFor(MARITAL_STATUS_LABELS, result.maritalStatus)}
            />
          </Section>

          <Section title="Documento (RG)">
            <Field label="Número" value={text(result.rgNumber)} />
            <Field label="Órgão emissor" value={text(result.rgIssuer)} />
            <Field label="UF" value={text(result.rgState)} />
            <Field label="Data de expedição" value={formatDate(result.rgIssueDate)} />
          </Section>

          <Section title="Endereço">
            <Field label="Logradouro" value={text(result.street)} />
            <Field label="Número" value={text(result.streetNumber)} />
            <Field label="Complemento" value={text(result.complement)} />
            <Field label="Bairro" value={text(result.neighborhood)} />
            <Field label="Cidade" value={text(result.city)} />
            <Field label="UF" value={text(result.uf)} />
            <Field label="CEP" value={formatZip(result.zipCode)} />
            <Field label="Código IBGE" value={result.ibgeCode ?? "—"} />
            <Field label="Código de região" value={text(result.regionCode)} />
          </Section>

          <Section title="Benefício">
            <Field label="Código do programa" value={text(result.programCode)} />
            <Field
              label="Situação"
              value={labelFor(STATUS_LABELS, result.status)}
            />
            <Field label="Motivo da situação" value={text(result.statusReason)} />
            <Field label="Data da situação" value={formatDate(result.statusDate)} />
            <Field label="Data de cadastro" value={formatDate(result.registrationDate)} />
            <Field label="Início do benefício" value={formatDate(result.benefitStartDate)} />
            <Field label="Fim do benefício" value={formatDate(result.benefitEndDate)} />
          </Section>

          <Section title="Renda familiar">
            <Field label="Renda familiar" value={formatBRL(result.familyIncome)} />
            <Field label="Membros da família" value={result.familyMembers ?? "—"} />
            <Field label="Renda per capita" value={formatBRL(result.perCapitaIncome)} />
          </Section>

          <Section title="Contato">
            <Field label="Telefone fixo" value={text(result.phoneLandline)} />
            <Field label="Celular" value={text(result.phoneMobile)} />
            <Field label="E-mail" value={text(result.email)} />
          </Section>

          <Section title="Biometria">
            <Field
              label="Situação"
              value={labelFor(BIOMETRIC_STATUS_LABELS, result.biometricStatus)}
            />
            <Field label="Data de coleta" value={formatDate(result.biometricCollectionDate)} />
            <Field label="Posto de coleta" value={text(result.biometricPostCode)} />
          </Section>

          <section className="bg-slate-800 rounded-xl p-5 border border-slate-700">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-slate-300 mb-4">
              Dependentes ({result.dependents.length})
            </h3>
            {result.dependents.length === 0 ? (
              <p className="text-slate-400 text-sm">Nenhum dependente cadastrado.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-slate-400 border-b border-slate-700">
                      <th className="py-2 pr-4 font-medium">Nome</th>
                      <th className="py-2 pr-4 font-medium">CPF</th>
                      <th className="py-2 pr-4 font-medium">Nascimento</th>
                      <th className="py-2 pr-4 font-medium">Parentesco</th>
                      <th className="py-2 pr-4 font-medium">Situação</th>
                      <th className="py-2 pr-4 font-medium">Deficiência</th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.dependents.map((dep, i) => (
                      <tr key={i} className="border-b border-slate-700/50">
                        <td className="py-2 pr-4">{text(dep.name)}</td>
                        <td className="py-2 pr-4 font-mono">{text(dep.maskedCpf)}</td>
                        <td className="py-2 pr-4">{formatDate(dep.birthDate)}</td>
                        <td className="py-2 pr-4">{labelFor(KINSHIP_LABELS, dep.kinship)}</td>
                        <td className="py-2 pr-4">
                          {labelFor(DEPENDENT_STATUS_LABELS, dep.status)}
                        </td>
                        <td className="py-2 pr-4">{dep.disabilityFlag === "S" ? "Sim" : "Não"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <Section title="Controle">
            <Field label="Criado em" value={formatDate(result.createdAt)} />
            <Field label="Atualizado em" value={formatDate(result.updatedAt)} />
          </Section>
        </div>
      )}
    </div>
  );
}
