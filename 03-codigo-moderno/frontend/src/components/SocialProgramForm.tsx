"use client";
/**
 * Formulário de cadastro de programa social.
 * Consome POST /api/v1/social-programs (REQ-007).
 * source_legacy: 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN
 * Client Component (interação de formulário com listas dinâmicas).
 */

import { useState } from "react";

interface TierState {
  occurrenceIndex: string;
  incomeFrom: string;
  incomeTo: string;
  multiplierFactor: string;
  additionalValue: string;
  cumulativeFlag: string;
}

interface RegionalState {
  occurrenceIndex: string;
  regionCode: string;
  regionalFactor: string;
  regionalComplement: string;
  activeFlag: string;
}

interface FormState {
  programCode: string;
  name: string;
  acronym: string;
  programType: string;
  responsibleAgency: string;
  creationLaw: string;
  creationDate: string;
  closureDate: string;
  status: string;
  baseValueIndividual: string;
  baseValueFamily: string;
  benefitCap: string;
  benefitFloor: string;
  annualAdjustmentPct: string;
  lastAdjustmentDate: string;
  factorK: string;
  maxPerCapitaIncome: string;
  minAge: string;
  maxAge: string;
  requiresChildren: string;
  minChildren: string;
  requiresSchool: string;
  requiresVaccination: string;
  requiresPrenatal: string;
  requiresBiometrics: string;
  discountTypes: string;
}

const EMPTY: FormState = {
  programCode: "", name: "", acronym: "", programType: "A",
  responsibleAgency: "", creationLaw: "", creationDate: "", closureDate: "",
  status: "A", baseValueIndividual: "", baseValueFamily: "", benefitCap: "",
  benefitFloor: "", annualAdjustmentPct: "", lastAdjustmentDate: "", factorK: "",
  maxPerCapitaIncome: "", minAge: "", maxAge: "", requiresChildren: "N",
  minChildren: "", requiresSchool: "N", requiresVaccination: "N",
  requiresPrenatal: "N", requiresBiometrics: "N", discountTypes: "",
};

const DISCOUNT_OPTIONS = ["IR", "JD", "CS", "PA", "EM", "TX", "OU", "EX"];

function num(value: string): number | null {
  if (value.trim() === "") return null;
  const n = Number(value);
  return Number.isNaN(n) ? null : n;
}

function intNum(value: string): number | null {
  if (value.trim() === "") return null;
  const n = Number.parseInt(value, 10);
  return Number.isNaN(n) ? null : n;
}

export function SocialProgramForm() {
  const [form, setForm] = useState<FormState>(EMPTY);
  const [tiers, setTiers] = useState<TierState[]>([]);
  const [regionals, setRegionals] = useState<RegionalState[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  function update(field: keyof FormState, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  function addTier() {
    if (tiers.length >= 5) return;
    setTiers((prev) => [
      ...prev,
      {
        occurrenceIndex: String(prev.length + 1),
        incomeFrom: "", incomeTo: "", multiplierFactor: "",
        additionalValue: "", cumulativeFlag: "N",
      },
    ]);
  }

  function updateTier(index: number, field: keyof TierState, value: string) {
    setTiers((prev) =>
      prev.map((t, i) => (i === index ? { ...t, [field]: value } : t)),
    );
  }

  function removeTier(index: number) {
    setTiers((prev) =>
      prev
        .filter((_, i) => i !== index)
        .map((t, i) => ({ ...t, occurrenceIndex: String(i + 1) })),
    );
  }

  function addRegional() {
    if (regionals.length >= 6) return;
    setRegionals((prev) => [
      ...prev,
      {
        occurrenceIndex: String(prev.length + 1),
        regionCode: "", regionalFactor: "", regionalComplement: "",
        activeFlag: "S",
      },
    ]);
  }

  function updateRegional(index: number, field: keyof RegionalState, value: string) {
    setRegionals((prev) =>
      prev.map((r, i) => (i === index ? { ...r, [field]: value } : r)),
    );
  }

  function removeRegional(index: number) {
    setRegionals((prev) =>
      prev
        .filter((_, i) => i !== index)
        .map((r, i) => ({ ...r, occurrenceIndex: String(i + 1) })),
    );
  }

  function reset() {
    setForm(EMPTY);
    setTiers([]);
    setRegionals([]);
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setLoading(true);
    try {
      const payload = {
        programCode: form.programCode,
        name: form.name,
        acronym: form.acronym || null,
        programType: form.programType,
        responsibleAgency: form.responsibleAgency || null,
        creationLaw: form.creationLaw || null,
        creationDate: form.creationDate || null,
        closureDate: form.closureDate || null,
        status: form.status,
        baseValueIndividual: num(form.baseValueIndividual),
        baseValueFamily: num(form.baseValueFamily),
        benefitCap: num(form.benefitCap),
        benefitFloor: num(form.benefitFloor),
        annualAdjustmentPct: num(form.annualAdjustmentPct),
        lastAdjustmentDate: form.lastAdjustmentDate || null,
        factorK: num(form.factorK),
        maxPerCapitaIncome: num(form.maxPerCapitaIncome),
        minAge: intNum(form.minAge),
        maxAge: intNum(form.maxAge),
        requiresChildren: form.requiresChildren,
        minChildren: intNum(form.minChildren),
        requiresSchool: form.requiresSchool,
        requiresVaccination: form.requiresVaccination,
        requiresPrenatal: form.requiresPrenatal,
        requiresBiometrics: form.requiresBiometrics,
        applicableDiscountTypes: form.discountTypes
          ? form.discountTypes.split(",").map((d) => d.trim()).filter(Boolean)
          : [],
        calculationTiers: tiers.map((t) => ({
          occurrenceIndex: intNum(t.occurrenceIndex),
          incomeFrom: num(t.incomeFrom),
          incomeTo: num(t.incomeTo),
          multiplierFactor: num(t.multiplierFactor),
          additionalValue: num(t.additionalValue),
          cumulativeFlag: t.cumulativeFlag,
        })),
        regionalParams: regionals.map((r) => ({
          occurrenceIndex: intNum(r.occurrenceIndex),
          regionCode: r.regionCode || null,
          regionalFactor: num(r.regionalFactor),
          regionalComplement: num(r.regionalComplement),
          activeFlag: r.activeFlag,
        })),
      };
      const res = await fetch("/api/v1/social-programs", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (res.status === 201) {
        const data = await res.json();
        setSuccess(`Programa cadastrado: ${data.name} (${data.programCode})`);
        reset();
        return;
      }
      if (res.status === 409) {
        setError("Código de programa já cadastrado.");
        return;
      }
      const body = await res.text();
      setError(body || "Falha na validação dos dados.");
    } catch {
      setError("Falha de conexão com o servidor.");
    } finally {
      setLoading(false);
    }
  }

  const inputClass =
    "w-full px-3 py-2 rounded-lg bg-slate-800 border border-slate-600 " +
    "focus:outline-none focus:border-blue-500 text-sm";
  const labelClass = "block text-slate-400 text-xs mb-1";

  return (
    <form onSubmit={handleSubmit} className="max-w-3xl space-y-6">
      <fieldset className="space-y-4">
        <legend className="text-lg font-medium text-blue-400 mb-2">Identificação</legend>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className={labelClass} htmlFor="programCode">Código do Programa *</label>
            <input id="programCode" required maxLength={4} value={form.programCode}
              onChange={(e) => update("programCode", e.target.value)}
              placeholder="Até 4 caracteres (ex: 0001)" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="acronym">Sigla</label>
            <input id="acronym" maxLength={10} value={form.acronym}
              onChange={(e) => update("acronym", e.target.value)}
              placeholder="Ex: PBF, BPC, PETI" className={inputClass} />
          </div>
          <div className="md:col-span-2">
            <label className={labelClass} htmlFor="name">Nome do Programa *</label>
            <input id="name" required maxLength={60} value={form.name}
              onChange={(e) => update("name", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="programType">Tipo *</label>
            <select id="programType" value={form.programType}
              onChange={(e) => update("programType", e.target.value)} className={inputClass}>
              <option value="A">Assistencial</option>
              <option value="T">Trabalho</option>
              <option value="P">Previdenciário</option>
            </select>
          </div>
          <div>
            <label className={labelClass} htmlFor="status">Situação *</label>
            <select id="status" value={form.status}
              onChange={(e) => update("status", e.target.value)} className={inputClass}>
              <option value="A">Ativo</option>
              <option value="I">Inativo</option>
              <option value="E">Encerrado</option>
            </select>
          </div>
          <div>
            <label className={labelClass} htmlFor="responsibleAgency">Órgão Responsável</label>
            <input id="responsibleAgency" maxLength={10} value={form.responsibleAgency}
              onChange={(e) => update("responsibleAgency", e.target.value)}
              placeholder="Ex: MDS" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="creationLaw">Lei/Decreto de Criação</label>
            <input id="creationLaw" maxLength={20} value={form.creationLaw}
              onChange={(e) => update("creationLaw", e.target.value)}
              placeholder="Ex: LEI 10.836/2004" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="creationDate">Data de Criação</label>
            <input id="creationDate" type="date" value={form.creationDate}
              onChange={(e) => update("creationDate", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="closureDate">Data de Encerramento</label>
            <input id="closureDate" type="date" value={form.closureDate}
              onChange={(e) => update("closureDate", e.target.value)} className={inputClass} />
          </div>
        </div>
      </fieldset>

      <fieldset className="space-y-4">
        <legend className="text-lg font-medium text-blue-400 mb-2">Valores Base</legend>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className={labelClass} htmlFor="baseValueIndividual">Valor Base Individual (R$)</label>
            <input id="baseValueIndividual" type="number" step="0.01" value={form.baseValueIndividual}
              onChange={(e) => update("baseValueIndividual", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="baseValueFamily">Valor Base Familiar (R$)</label>
            <input id="baseValueFamily" type="number" step="0.01" value={form.baseValueFamily}
              onChange={(e) => update("baseValueFamily", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="benefitCap">Teto do Benefício (R$)</label>
            <input id="benefitCap" type="number" step="0.01" value={form.benefitCap}
              onChange={(e) => update("benefitCap", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="benefitFloor">Piso do Benefício (R$)</label>
            <input id="benefitFloor" type="number" step="0.01" value={form.benefitFloor}
              onChange={(e) => update("benefitFloor", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="annualAdjustmentPct">Reajuste Anual (%)</label>
            <input id="annualAdjustmentPct" type="number" step="0.01" max="9.99" value={form.annualAdjustmentPct}
              onChange={(e) => update("annualAdjustmentPct", e.target.value)}
              placeholder="máx 9.99" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="lastAdjustmentDate">Último Reajuste</label>
            <input id="lastAdjustmentDate" type="date" value={form.lastAdjustmentDate}
              onChange={(e) => update("lastAdjustmentDate", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="factorK">Fator-K</label>
            <input id="factorK" type="number" step="0.0001" max="9.9999" value={form.factorK}
              onChange={(e) => update("factorK", e.target.value)}
              placeholder="máx 9.9999" className={inputClass} />
          </div>
        </div>
      </fieldset>

      <fieldset className="space-y-4">
        <legend className="text-lg font-medium text-blue-400 mb-2">Elegibilidade</legend>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className={labelClass} htmlFor="maxPerCapitaIncome">Renda Máx. per capita (R$)</label>
            <input id="maxPerCapitaIncome" type="number" step="0.01" value={form.maxPerCapitaIncome}
              onChange={(e) => update("maxPerCapitaIncome", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="minAge">Idade Mínima</label>
            <input id="minAge" type="number" value={form.minAge}
              onChange={(e) => update("minAge", e.target.value)}
              placeholder="0 = sem limite" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="maxAge">Idade Máxima</label>
            <input id="maxAge" type="number" value={form.maxAge}
              onChange={(e) => update("maxAge", e.target.value)}
              placeholder="0 = sem limite" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="requiresChildren">Exige Filhos?</label>
            <select id="requiresChildren" value={form.requiresChildren}
              onChange={(e) => update("requiresChildren", e.target.value)} className={inputClass}>
              <option value="N">Não</option>
              <option value="S">Sim</option>
            </select>
          </div>
          <div>
            <label className={labelClass} htmlFor="minChildren">Mínimo de Filhos</label>
            <input id="minChildren" type="number" value={form.minChildren}
              onChange={(e) => update("minChildren", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="requiresSchool">Exige Frequência Escolar?</label>
            <select id="requiresSchool" value={form.requiresSchool}
              onChange={(e) => update("requiresSchool", e.target.value)} className={inputClass}>
              <option value="N">Não</option>
              <option value="S">Sim</option>
            </select>
          </div>
          <div>
            <label className={labelClass} htmlFor="requiresVaccination">Exige Cartão de Vacina?</label>
            <select id="requiresVaccination" value={form.requiresVaccination}
              onChange={(e) => update("requiresVaccination", e.target.value)} className={inputClass}>
              <option value="N">Não</option>
              <option value="S">Sim</option>
            </select>
          </div>
          <div>
            <label className={labelClass} htmlFor="requiresPrenatal">Exige Pré-natal?</label>
            <select id="requiresPrenatal" value={form.requiresPrenatal}
              onChange={(e) => update("requiresPrenatal", e.target.value)} className={inputClass}>
              <option value="N">Não</option>
              <option value="S">Sim</option>
            </select>
          </div>
          <div>
            <label className={labelClass} htmlFor="requiresBiometrics">Exige Biometria?</label>
            <select id="requiresBiometrics" value={form.requiresBiometrics}
              onChange={(e) => update("requiresBiometrics", e.target.value)} className={inputClass}>
              <option value="N">Não</option>
              <option value="S">Sim</option>
            </select>
          </div>
        </div>
        <div>
          <label className={labelClass} htmlFor="discountTypes">Tipos de Desconto Aplicáveis</label>
          <input id="discountTypes" value={form.discountTypes}
            onChange={(e) => update("discountTypes", e.target.value)}
            placeholder={`Separados por vírgula. Ex: ${DISCOUNT_OPTIONS.join(", ")}`}
            className={inputClass} />
        </div>
      </fieldset>

      <fieldset className="space-y-4">
        <div className="flex items-center justify-between">
          <legend className="text-lg font-medium text-blue-400">Faixas de Cálculo</legend>
          <button type="button" onClick={addTier} disabled={tiers.length >= 5}
            className="px-3 py-1 text-sm bg-slate-700 rounded-lg hover:bg-slate-600
                       disabled:opacity-50 transition-colors">
            + Faixa
          </button>
        </div>
        {tiers.length === 0 && (
          <p className="text-xs text-slate-500">Nenhuma faixa (máx. 5).</p>
        )}
        {tiers.map((tier, index) => (
          <div key={index} className="grid grid-cols-2 md:grid-cols-6 gap-2 items-end
                                      border border-slate-700 rounded-lg p-3">
            <div>
              <label className={labelClass}>Ordem</label>
              <input type="number" value={tier.occurrenceIndex} readOnly
                className={`${inputClass} opacity-60`} />
            </div>
            <div>
              <label className={labelClass}>Renda Início</label>
              <input type="number" step="0.01" value={tier.incomeFrom}
                onChange={(e) => updateTier(index, "incomeFrom", e.target.value)} className={inputClass} />
            </div>
            <div>
              <label className={labelClass}>Renda Fim</label>
              <input type="number" step="0.01" value={tier.incomeTo}
                onChange={(e) => updateTier(index, "incomeTo", e.target.value)} className={inputClass} />
            </div>
            <div>
              <label className={labelClass}>Fator Mult.</label>
              <input type="number" step="0.0001" max="0.0999" value={tier.multiplierFactor}
                onChange={(e) => updateTier(index, "multiplierFactor", e.target.value)}
                placeholder="máx 0.0999" className={inputClass} />
            </div>
            <div>
              <label className={labelClass}>Vlr Adicional</label>
              <input type="number" step="0.01" value={tier.additionalValue}
                onChange={(e) => updateTier(index, "additionalValue", e.target.value)} className={inputClass} />
            </div>
            <div className="flex gap-2">
              <div className="flex-1">
                <label className={labelClass}>Acumula</label>
                <select value={tier.cumulativeFlag}
                  onChange={(e) => updateTier(index, "cumulativeFlag", e.target.value)} className={inputClass}>
                  <option value="N">Não</option>
                  <option value="S">Sim</option>
                </select>
              </div>
              <button type="button" onClick={() => removeTier(index)}
                className="px-2 py-2 text-red-400 hover:text-red-300" aria-label="Remover faixa">
                ✕
              </button>
            </div>
          </div>
        ))}
      </fieldset>

      <fieldset className="space-y-4">
        <div className="flex items-center justify-between">
          <legend className="text-lg font-medium text-blue-400">Parâmetros Regionais</legend>
          <button type="button" onClick={addRegional} disabled={regionals.length >= 6}
            className="px-3 py-1 text-sm bg-slate-700 rounded-lg hover:bg-slate-600
                       disabled:opacity-50 transition-colors">
            + Região
          </button>
        </div>
        {regionals.length === 0 && (
          <p className="text-xs text-slate-500">Nenhum parâmetro regional (máx. 6).</p>
        )}
        {regionals.map((reg, index) => (
          <div key={index} className="grid grid-cols-2 md:grid-cols-5 gap-2 items-end
                                      border border-slate-700 rounded-lg p-3">
            <div>
              <label className={labelClass}>Ordem</label>
              <input type="number" value={reg.occurrenceIndex} readOnly
                className={`${inputClass} opacity-60`} />
            </div>
            <div>
              <label className={labelClass}>Cód. Região</label>
              <input maxLength={2} value={reg.regionCode}
                onChange={(e) => updateRegional(index, "regionCode", e.target.value)}
                placeholder="01-05 ou 99" className={inputClass} />
            </div>
            <div>
              <label className={labelClass}>Fator Regional</label>
              <input type="number" step="0.0001" max="0.0999" value={reg.regionalFactor}
                onChange={(e) => updateRegional(index, "regionalFactor", e.target.value)}
                placeholder="máx 0.0999" className={inputClass} />
            </div>
            <div>
              <label className={labelClass}>Complemento</label>
              <input type="number" step="0.01" value={reg.regionalComplement}
                onChange={(e) => updateRegional(index, "regionalComplement", e.target.value)} className={inputClass} />
            </div>
            <div className="flex gap-2">
              <div className="flex-1">
                <label className={labelClass}>Ativo</label>
                <select value={reg.activeFlag}
                  onChange={(e) => updateRegional(index, "activeFlag", e.target.value)} className={inputClass}>
                  <option value="S">Sim</option>
                  <option value="N">Não</option>
                </select>
              </div>
              <button type="button" onClick={() => removeRegional(index)}
                className="px-2 py-2 text-red-400 hover:text-red-300" aria-label="Remover região">
                ✕
              </button>
            </div>
          </div>
        ))}
      </fieldset>

      {error && <div role="alert" className="text-red-400">{error}</div>}
      {success && <div role="status" className="text-green-400">{success}</div>}

      <div className="flex gap-3">
        <button type="submit" disabled={loading}
          className="px-6 py-2 bg-blue-600 rounded-lg hover:bg-blue-500
                     disabled:opacity-50 transition-colors">
          {loading ? "Salvando..." : "Cadastrar"}
        </button>
        <button type="button" onClick={reset}
          className="px-6 py-2 bg-slate-700 rounded-lg hover:bg-slate-600 transition-colors">
          Limpar
        </button>
      </div>
      <p className="text-xs text-slate-500">
        * campos obrigatórios (REQ-007). Fatores em NUMERIC(3,4) aceitam no máximo 0.0999.
      </p>
    </form>
  );
}
