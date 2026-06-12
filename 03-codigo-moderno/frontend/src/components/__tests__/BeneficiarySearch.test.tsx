import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { BeneficiarySearch } from "../BeneficiarySearch";

/**
 * Testes de componente para BeneficiarySearch.
 * REQ-006 (CPF mascarado na exibição).
 */
describe("BeneficiarySearch", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("should render search form", () => {
    render(<BeneficiarySearch />);
    expect(screen.getByLabelText(/CPF do beneficiário/i)).toBeTruthy();
    expect(screen.getByRole("button", { name: /buscar/i })).toBeTruthy();
  });

  it("should display masked CPF and full data when beneficiary is found", async () => {
    const mockData = {
      maskedCpf: "***.***.777-35",
      registrationNumber: "12345678901",
      fullName: "JOSE DA SILVA",
      motherName: "MARIA DA SILVA",
      fatherName: null,
      birthDate: "1980-05-10",
      gender: "M",
      maritalStatus: "C",
      rgNumber: "1234567",
      rgIssuer: "SSP",
      rgState: "SP",
      rgIssueDate: "2000-01-01",
      street: "RUA A",
      streetNumber: "100",
      complement: null,
      neighborhood: "CENTRO",
      city: "SAO PAULO",
      uf: "SP",
      zipCode: 1310100,
      ibgeCode: 3550308,
      regionCode: "03",
      programCode: "0001",
      registrationDate: "2010-03-01",
      benefitStartDate: "2010-04-01",
      benefitEndDate: null,
      status: "A",
      statusReason: null,
      statusDate: "2010-04-01",
      familyIncome: 1500.5,
      familyMembers: 4,
      perCapitaIncome: 375.13,
      phoneLandline: null,
      phoneMobile: "11999990000",
      email: "jose@example.com",
      biometricStatus: "S",
      biometricCollectionDate: "2015-06-01",
      biometricPostCode: "001234",
      dependents: [
        {
          maskedCpf: "***.***.111-22",
          name: "ANA DA SILVA",
          birthDate: "2010-08-15",
          kinship: "FI",
          status: "A",
          disabilityFlag: "N",
        },
      ],
      createdAt: "2010-03-01T10:00:00",
      updatedAt: null,
    };
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => mockData,
    }));

    render(<BeneficiarySearch />);
    fireEvent.change(screen.getByLabelText(/CPF/i), {
      target: { value: "11144477735" },
    });
    fireEvent.click(screen.getByRole("button", { name: /buscar/i }));

    await waitFor(() => {
      expect(screen.getByText("***.***.777-35")).toBeTruthy();
      expect(screen.getByText("JOSE DA SILVA")).toBeTruthy();
      expect(screen.getAllByText("Ativo").length).toBeGreaterThan(0);
      expect(screen.getByText("MARIA DA SILVA")).toBeTruthy();
      expect(screen.getByText("jose@example.com")).toBeTruthy();
      expect(screen.getByText("ANA DA SILVA")).toBeTruthy();
      expect(screen.getByText("***.***.111-22")).toBeTruthy();
    });
  });

  it("should display error when beneficiary is not found", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: false,
      status: 404,
    }));

    render(<BeneficiarySearch />);
    fireEvent.change(screen.getByLabelText(/CPF/i), {
      target: { value: "00000000000" },
    });
    fireEvent.click(screen.getByRole("button", { name: /buscar/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toBeTruthy();
    });
  });
});
