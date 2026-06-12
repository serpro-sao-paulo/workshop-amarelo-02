"use client";
/**
 * Formulário de cadastro de beneficiário.
 * Consome POST /api/v1/beneficiaries (REQ-001..REQ-004).
 * Client Component (interação de formulário).
 */

import { useState } from "react";

interface FormState {
  cpf: string;
  fullName: string;
  motherName: string;
  fatherName: string;
  birthDate: string;
  gender: string;
  maritalStatus: string;
  rgNumber: string;
  street: string;
  streetNumber: string;
  neighborhood: string;
  city: string;
  uf: string;
  zipCode: string;
  regionCode: string;
  programCode: string;
  familyIncome: string;
  familyMembers: string;
  phoneMobile: string;
  email: string;
}

const EMPTY: FormState = {
  cpf: "", fullName: "", motherName: "", fatherName: "", birthDate: "",
  gender: "M", maritalStatus: "S", rgNumber: "", street: "", streetNumber: "",
  neighborhood: "", city: "", uf: "", zipCode: "", regionCode: "",
  programCode: "", familyIncome: "", familyMembers: "", phoneMobile: "", email: "",
};

const UFS = [
  "AC", "AL", "AM", "AP", "BA", "CE", "DF", "ES", "GO", "MA", "MG", "MS", "MT",
  "PA", "PB", "PE", "PI", "PR", "RJ", "RN", "RO", "RR", "RS", "SC", "SE", "SP", "TO",
];

export function BeneficiaryForm() {
  const [form, setForm] = useState<FormState>(EMPTY);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  function update(field: keyof FormState, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    setLoading(true);
    try {
      const payload = {
        cpf: form.cpf.replace(/\D/g, ""),
        fullName: form.fullName,
        motherName: form.motherName,
        fatherName: form.fatherName || null,
        birthDate: form.birthDate || null,
        gender: form.gender,
        maritalStatus: form.maritalStatus,
        rgNumber: form.rgNumber || null,
        street: form.street || null,
        streetNumber: form.streetNumber || null,
        neighborhood: form.neighborhood || null,
        city: form.city || null,
        uf: form.uf || null,
        zipCode: form.zipCode ? Number(form.zipCode.replace(/\D/g, "")) : null,
        regionCode: form.regionCode || null,
        programCode: form.programCode || null,
        familyIncome: form.familyIncome ? Number(form.familyIncome) : null,
        familyMembers: form.familyMembers ? Number(form.familyMembers) : null,
        phoneMobile: form.phoneMobile || null,
        email: form.email || null,
      };
      const res = await fetch("/api/v1/beneficiaries", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (res.status === 201) {
        const data = await res.json();
        setSuccess(`Beneficiário cadastrado: ${data.name} (${data.maskedCpf})`);
        setForm(EMPTY);
        return;
      }
      if (res.status === 409) {
        setError("CPF já cadastrado.");
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
        <legend className="text-lg font-medium text-blue-400 mb-2">Dados Pessoais</legend>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className={labelClass} htmlFor="cpf">CPF *</label>
            <input id="cpf" required value={form.cpf}
              onChange={(e) => update("cpf", e.target.value)}
              placeholder="Somente números" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="birthDate">Data de Nascimento *</label>
            <input id="birthDate" type="date" required value={form.birthDate}
              onChange={(e) => update("birthDate", e.target.value)} className={inputClass} />
          </div>
          <div className="md:col-span-2">
            <label className={labelClass} htmlFor="fullName">Nome Completo *</label>
            <input id="fullName" required value={form.fullName}
              onChange={(e) => update("fullName", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="motherName">Nome da Mãe *</label>
            <input id="motherName" required value={form.motherName}
              onChange={(e) => update("motherName", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="fatherName">Nome do Pai</label>
            <input id="fatherName" value={form.fatherName}
              onChange={(e) => update("fatherName", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="gender">Sexo</label>
            <select id="gender" value={form.gender}
              onChange={(e) => update("gender", e.target.value)} className={inputClass}>
              <option value="M">Masculino</option>
              <option value="F">Feminino</option>
              <option value="I">Indefinido</option>
            </select>
          </div>
          <div>
            <label className={labelClass} htmlFor="maritalStatus">Estado Civil</label>
            <select id="maritalStatus" value={form.maritalStatus}
              onChange={(e) => update("maritalStatus", e.target.value)} className={inputClass}>
              <option value="S">Solteiro(a)</option>
              <option value="C">Casado(a)</option>
              <option value="D">Divorciado(a)</option>
              <option value="V">Viúvo(a)</option>
              <option value="U">União Estável</option>
            </select>
          </div>
          <div>
            <label className={labelClass} htmlFor="rgNumber">RG</label>
            <input id="rgNumber" value={form.rgNumber}
              onChange={(e) => update("rgNumber", e.target.value)} className={inputClass} />
          </div>
        </div>
      </fieldset>

      <fieldset className="space-y-4">
        <legend className="text-lg font-medium text-blue-400 mb-2">Endereço</legend>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="md:col-span-2">
            <label className={labelClass} htmlFor="street">Logradouro</label>
            <input id="street" value={form.street}
              onChange={(e) => update("street", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="streetNumber">Número</label>
            <input id="streetNumber" value={form.streetNumber}
              onChange={(e) => update("streetNumber", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="neighborhood">Bairro</label>
            <input id="neighborhood" value={form.neighborhood}
              onChange={(e) => update("neighborhood", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="city">Município</label>
            <input id="city" value={form.city}
              onChange={(e) => update("city", e.target.value)} className={inputClass} />
          </div>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className={labelClass} htmlFor="uf">UF</label>
              <select id="uf" value={form.uf}
                onChange={(e) => update("uf", e.target.value)} className={inputClass}>
                <option value="">--</option>
                {UFS.map((uf) => <option key={uf} value={uf}>{uf}</option>)}
              </select>
            </div>
            <div>
              <label className={labelClass} htmlFor="zipCode">CEP</label>
              <input id="zipCode" value={form.zipCode}
                onChange={(e) => update("zipCode", e.target.value)} className={inputClass} />
            </div>
          </div>
        </div>
      </fieldset>

      <fieldset className="space-y-4">
        <legend className="text-lg font-medium text-blue-400 mb-2">Benefício e Contato</legend>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className={labelClass} htmlFor="programCode">Código do Programa</label>
            <input id="programCode" value={form.programCode}
              onChange={(e) => update("programCode", e.target.value)}
              placeholder="Ex: PBF1" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="regionCode">Código da Região</label>
            <input id="regionCode" value={form.regionCode}
              onChange={(e) => update("regionCode", e.target.value)}
              placeholder="01-25 ou 99" className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="familyIncome">Renda Familiar (R$)</label>
            <input id="familyIncome" type="number" step="0.01" value={form.familyIncome}
              onChange={(e) => update("familyIncome", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="familyMembers">Membros da Família</label>
            <input id="familyMembers" type="number" value={form.familyMembers}
              onChange={(e) => update("familyMembers", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="phoneMobile">Celular</label>
            <input id="phoneMobile" value={form.phoneMobile}
              onChange={(e) => update("phoneMobile", e.target.value)} className={inputClass} />
          </div>
          <div>
            <label className={labelClass} htmlFor="email">E-mail</label>
            <input id="email" type="email" value={form.email}
              onChange={(e) => update("email", e.target.value)} className={inputClass} />
          </div>
        </div>
      </fieldset>

      {error && <div role="alert" className="text-red-400">{error}</div>}
      {success && <div role="status" className="text-green-400">{success}</div>}

      <div className="flex gap-3">
        <button type="submit" disabled={loading}
          className="px-6 py-2 bg-blue-600 rounded-lg hover:bg-blue-500
                     disabled:opacity-50 transition-colors">
          {loading ? "Salvando..." : "Cadastrar"}
        </button>
        <button type="button" onClick={() => setForm(EMPTY)}
          className="px-6 py-2 bg-slate-700 rounded-lg hover:bg-slate-600 transition-colors">
          Limpar
        </button>
      </div>
      <p className="text-xs text-slate-500">* campos obrigatórios (REQ-001..REQ-004)</p>
    </form>
  );
}
