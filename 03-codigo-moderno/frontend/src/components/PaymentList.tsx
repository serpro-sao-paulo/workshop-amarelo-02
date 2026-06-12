"use client";
/**
 * Componente de listagem de pagamentos por CPF.
 * REQ-011 (teto 30%), REQ-012 (contribuição), REQ-022 (status G/P/D/E/C).
 * Clique numa linha abre o detalhe do pagamento + dados do beneficiário.
 */

import { useState } from "react";

interface PaymentDiscount {
  occurrenceIndex?: number;
  discountType?: string | null;
  description?: string | null;
  amount?: number | null;
}

interface Payment {
  id: number;
  paymentNumber: number | null;
  cpf: string;
  registrationNumber: string | null;
  programCode: string;
  competence: string;
  cycleNumber: string | null;
  grossAmount: number;
  netAmount: number;
  totalDiscount: number;
  status: string;
  paymentType: string;
  generationDate: string | null;
  emissionDate: string | null;
  confirmationDate: string | null;
  cancellationDate: string | null;
  cancellationReason: string | null;
  bankCode: string | null;
  agencyCode: string | null;
  accountNumber: string | null;
  accountType: string | null;
  operationCode: string | null;
  siafiOrderNumber: string | null;
  siafiCommitmentNote: string | null;
  siafiManagementUnit: string | null;
  siafiManagementCode: string | null;
  siafiIntegrationStatus: string | null;
  reconciliationDate: string | null;
  reconciliationStatus: string | null;
  reconciledAmount: number | null;
  bankReturnCode: string | null;
  bankReturnDescription: string | null;
  discounts: PaymentDiscount[] | null;
}

interface Beneficiary {
  maskedCpf: string;
  name: string;
  status: string;
}

const STATUS_LABELS: Record<string, string> = {
  G: "Gerado",
  P: "Pago",
  D: "Devolvido",
  E: "Estornado",
  C: "Confirmado",
  X: "Cancelado",
  R: "Reprocessado",
};

const TYPE_LABELS: Record<string, string> = {
  N: "Normal",
  D: "Dezembro/13º",
  T: "Terceiro",
};

const BENEFICIARY_STATUS_LABELS: Record<string, string> = {
  A: "Ativo",
  S: "Suspenso",
  C: "Cancelado",
  I: "Inativo",
  D: "Desligado",
};

const SIAFI_STATUS_LABELS: Record<string, string> = {
  I: "Integrado",
  P: "Pendente",
  E: "Erro",
};

const RECONCILIATION_STATUS_LABELS: Record<string, string> = {
  C: "Conciliado",
  D: "Divergente",
  P: "Pendente",
  N: "N/A",
};

function formatBRL(value: number | null | undefined) {
  if (value === null || value === undefined) return "—";
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(value);
}

function formatCompetence(competence: string) {
  if (!competence || competence.length !== 6) return competence;
  return `${competence.slice(4, 6)}/${competence.slice(0, 4)}`;
}

function formatDate(value: string | null | undefined) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString("pt-BR");
}

export function PaymentList() {
  const [cpf, setCpf] = useState("");
  const [payments, setPayments] = useState<Payment[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState<Payment | null>(null);
  const [beneficiary, setBeneficiary] = useState<Beneficiary | null>(null);
  const [beneficiaryLoading, setBeneficiaryLoading] = useState(false);

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setPayments([]);
    setSelected(null);
    if (!cpf.trim()) return;
    setLoading(true);
    try {
      const res = await fetch(`/api/v1/payments?cpf=${cpf.replace(/\D/g, "")}`);
      if (!res.ok) {
        setError("Erro ao consultar pagamentos.");
        return;
      }
      setPayments(await res.json());
    } catch {
      setError("Falha de conexão.");
    } finally {
      setLoading(false);
    }
  }

  async function openDetails(payment: Payment) {
    setSelected(payment);
    setBeneficiary(null);
    setBeneficiaryLoading(true);
    try {
      const res = await fetch(`/api/v1/beneficiaries/${payment.cpf.replace(/\D/g, "")}`);
      if (res.ok) {
        setBeneficiary(await res.json());
      }
    } catch {
      // detalhe do beneficiário é complementar; ignora falha
    } finally {
      setBeneficiaryLoading(false);
    }
  }

  function closeDetails() {
    setSelected(null);
    setBeneficiary(null);
  }

  return (
    <div>
      <form onSubmit={handleSearch} className="flex gap-2 mb-6 max-w-md">
        <input
          type="text"
          value={cpf}
          onChange={(e) => setCpf(e.target.value)}
          placeholder="CPF do beneficiário"
          aria-label="CPF"
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

      {payments.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full text-sm border-collapse">
            <thead>
              <tr className="border-b border-slate-700 text-slate-400 text-left">
                <th className="py-2 pr-4">Competência</th>
                <th className="py-2 pr-4">Tipo</th>
                <th className="py-2 pr-4">Bruto</th>
                <th className="py-2 pr-4">Desconto</th>
                <th className="py-2 pr-4">Líquido</th>
                <th className="py-2">Situação</th>
              </tr>
            </thead>
            <tbody>
              {payments.map((p) => (
                <tr
                  key={p.id}
                  onClick={() => openDetails(p)}
                  className="border-b border-slate-800 hover:bg-slate-800/50 cursor-pointer"
                  tabIndex={0}
                  role="button"
                  aria-label={`Detalhar pagamento da competência ${formatCompetence(p.competence)}`}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" || e.key === " ") {
                      e.preventDefault();
                      openDetails(p);
                    }
                  }}
                >
                  <td className="py-2 pr-4 font-mono">{formatCompetence(p.competence)}</td>
                  <td className="py-2 pr-4">{TYPE_LABELS[p.paymentType] ?? p.paymentType}</td>
                  <td className="py-2 pr-4">{formatBRL(p.grossAmount)}</td>
                  <td className="py-2 pr-4 text-red-400">{formatBRL(p.totalDiscount)}</td>
                  <td className="py-2 pr-4 text-green-400">{formatBRL(p.netAmount)}</td>
                  <td className="py-2">{STATUS_LABELS[p.status] ?? p.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <p className="text-xs text-slate-500 mt-2">
            Clique em uma linha para ver os detalhes do pagamento.
          </p>
        </div>
      )}

      {!loading && payments.length === 0 && cpf && !error && (
        <p className="text-slate-400">Nenhum pagamento encontrado.</p>
      )}

      {selected && (
        <PaymentDetail
          payment={selected}
          beneficiary={beneficiary}
          beneficiaryLoading={beneficiaryLoading}
          onClose={closeDetails}
        />
      )}
    </div>
  );
}

function Field({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div>
      <span className="text-slate-400 text-xs">{label}</span>
      <p className="text-sm">{value ?? "—"}</p>
    </div>
  );
}

function PaymentDetail({
  payment,
  beneficiary,
  beneficiaryLoading,
  onClose,
}: {
  payment: Payment;
  beneficiary: Beneficiary | null;
  beneficiaryLoading: boolean;
  onClose: () => void;
}) {
  const discounts = payment.discounts ?? [];

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
      role="dialog"
      aria-modal="true"
      aria-label="Detalhes do pagamento"
      onClick={onClose}
    >
      <div
        className="bg-slate-900 border border-slate-700 rounded-xl w-full max-w-3xl
                   max-h-[90vh] overflow-y-auto p-6 space-y-6"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between">
          <div>
            <h3 className="text-xl font-semibold">Detalhes do Pagamento</h3>
            <p className="text-slate-400 text-sm">
              Competência {formatCompetence(payment.competence)} ·{" "}
              {STATUS_LABELS[payment.status] ?? payment.status}
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Fechar"
            className="text-slate-400 hover:text-slate-100 text-xl leading-none"
          >
            ✕
          </button>
        </div>

        <section className="space-y-3">
          <h4 className="text-sm font-medium text-blue-400 uppercase tracking-wide">
            Beneficiário
          </h4>
          {beneficiaryLoading ? (
            <p className="text-slate-400 text-sm">Carregando dados do beneficiário…</p>
          ) : beneficiary ? (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 bg-slate-800/50
                            border border-slate-700 rounded-lg p-4">
              <Field label="Nome" value={beneficiary.name} />
              <Field label="CPF" value={<span className="font-mono">{beneficiary.maskedCpf}</span>} />
              <Field
                label="Situação"
                value={BENEFICIARY_STATUS_LABELS[beneficiary.status] ?? beneficiary.status}
              />
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-slate-800/50
                            border border-slate-700 rounded-lg p-4">
              <Field label="CPF" value={<span className="font-mono">{payment.cpf}</span>} />
              <Field label="Nº Inscrição (NIS)" value={payment.registrationNumber} />
            </div>
          )}
        </section>

        <section className="space-y-3">
          <h4 className="text-sm font-medium text-blue-400 uppercase tracking-wide">
            Pagamento
          </h4>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 bg-slate-800/50
                          border border-slate-700 rounded-lg p-4">
            <Field label="Nº Pagamento" value={payment.paymentNumber} />
            <Field label="Programa" value={payment.programCode} />
            <Field label="Ciclo" value={payment.cycleNumber} />
            <Field label="Tipo" value={TYPE_LABELS[payment.paymentType] ?? payment.paymentType} />
            <Field label="Valor Bruto" value={formatBRL(payment.grossAmount)} />
            <Field
              label="Desconto Total"
              value={<span className="text-red-400">{formatBRL(payment.totalDiscount)}</span>}
            />
            <Field
              label="Valor Líquido"
              value={<span className="text-green-400">{formatBRL(payment.netAmount)}</span>}
            />
            <Field label="Geração" value={formatDate(payment.generationDate)} />
            <Field label="Emissão" value={formatDate(payment.emissionDate)} />
            <Field label="Confirmação" value={formatDate(payment.confirmationDate)} />
            <Field label="Cancelamento" value={formatDate(payment.cancellationDate)} />
            <Field label="Motivo Cancel." value={payment.cancellationReason} />
          </div>
        </section>

        {(payment.bankCode || payment.agencyCode || payment.accountNumber) && (
          <section className="space-y-3">
            <h4 className="text-sm font-medium text-blue-400 uppercase tracking-wide">
              Dados Bancários
            </h4>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 bg-slate-800/50
                            border border-slate-700 rounded-lg p-4">
              <Field label="Banco" value={payment.bankCode} />
              <Field label="Agência" value={payment.agencyCode} />
              <Field label="Conta" value={payment.accountNumber} />
              <Field label="Tipo de Conta" value={payment.accountType} />
              <Field label="Operação" value={payment.operationCode} />
            </div>
          </section>
        )}

        {(payment.siafiOrderNumber || payment.siafiIntegrationStatus) && (
          <section className="space-y-3">
            <h4 className="text-sm font-medium text-blue-400 uppercase tracking-wide">
              Integração SIAFI
            </h4>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 bg-slate-800/50
                            border border-slate-700 rounded-lg p-4">
              <Field label="Ordem Bancária" value={payment.siafiOrderNumber} />
              <Field label="Nota de Empenho" value={payment.siafiCommitmentNote} />
              <Field label="Unidade Gestora" value={payment.siafiManagementUnit} />
              <Field label="Gestão" value={payment.siafiManagementCode} />
              <Field
                label="Situação"
                value={
                  payment.siafiIntegrationStatus
                    ? SIAFI_STATUS_LABELS[payment.siafiIntegrationStatus] ??
                      payment.siafiIntegrationStatus
                    : "—"
                }
              />
            </div>
          </section>
        )}

        {(payment.reconciliationStatus || payment.reconciliationDate) && (
          <section className="space-y-3">
            <h4 className="text-sm font-medium text-blue-400 uppercase tracking-wide">
              Conciliação Bancária
            </h4>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 bg-slate-800/50
                            border border-slate-700 rounded-lg p-4">
              <Field
                label="Situação"
                value={
                  payment.reconciliationStatus
                    ? RECONCILIATION_STATUS_LABELS[payment.reconciliationStatus] ??
                      payment.reconciliationStatus
                    : "—"
                }
              />
              <Field label="Data" value={formatDate(payment.reconciliationDate)} />
              <Field label="Valor Conciliado" value={formatBRL(payment.reconciledAmount)} />
              <Field label="Cód. Retorno" value={payment.bankReturnCode} />
              <Field label="Descrição Retorno" value={payment.bankReturnDescription} />
            </div>
          </section>
        )}

        {discounts.length > 0 && (
          <section className="space-y-3">
            <h4 className="text-sm font-medium text-blue-400 uppercase tracking-wide">
              Descontos
            </h4>
            <div className="overflow-x-auto">
              <table className="w-full text-sm border border-slate-700 rounded-lg">
                <thead className="bg-slate-800 text-slate-400">
                  <tr>
                    <th className="px-3 py-2 text-left">Tipo</th>
                    <th className="px-3 py-2 text-left">Descrição</th>
                    <th className="px-3 py-2 text-left">Valor</th>
                  </tr>
                </thead>
                <tbody>
                  {discounts.map((d, index) => (
                    <tr key={index} className="border-t border-slate-700">
                      <td className="px-3 py-2">{d.discountType ?? "—"}</td>
                      <td className="px-3 py-2">{d.description ?? "—"}</td>
                      <td className="px-3 py-2 text-red-400">{formatBRL(d.amount)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        <div className="flex justify-end">
          <button
            type="button"
            onClick={onClose}
            className="px-6 py-2 bg-slate-700 rounded-lg hover:bg-slate-600 transition-colors"
          >
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
}
