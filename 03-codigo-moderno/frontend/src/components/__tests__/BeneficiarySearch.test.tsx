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

  it("should display masked CPF when beneficiary is found", async () => {
    const mockData = {
      maskedCpf: "***.***.777-35",
      name: "JOSE DA SILVA",
      status: "A",
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
      expect(screen.getByText("Ativo")).toBeTruthy();
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
