import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { PaymentList } from "../PaymentList";

/**
 * Testes de componente para PaymentList.
 * REQ-011 (desconto), REQ-022 (status G/P/D/E).
 */
describe("PaymentList", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("should render search form", () => {
    render(<PaymentList />);
    expect(screen.getByLabelText(/CPF/i)).toBeTruthy();
  });

  it("should display payments in table with correct status labels", async () => {
    const mockPayments = [
      {
        id: 1,
        competence: "202506",
        grossAmount: 1100.0,
        netAmount: 1067.0,
        totalDiscount: 33.0,
        status: "G",
        paymentType: "N",
      },
      {
        id: 2,
        competence: "202412",
        grossAmount: 2365.0,
        netAmount: 2294.05,
        totalDiscount: 70.95,
        status: "P",
        paymentType: "D",
      },
    ];
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: true,
      json: async () => mockPayments,
    }));

    render(<PaymentList />);
    fireEvent.change(screen.getByLabelText(/CPF/i), {
      target: { value: "11144477735" },
    });
    fireEvent.click(screen.getByRole("button", { name: /buscar/i }));

    await waitFor(() => {
      expect(screen.getByText("Gerado")).toBeTruthy();      // status G
      expect(screen.getByText("Pago")).toBeTruthy();        // status P
      expect(screen.getByText("Dezembro/13º")).toBeTruthy(); // type D
    });
  });

  it("should show empty state when no payments found", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [],
    }));

    render(<PaymentList />);
    fireEvent.change(screen.getByLabelText(/CPF/i), {
      target: { value: "11144477735" },
    });
    fireEvent.click(screen.getByRole("button", { name: /buscar/i }));

    await waitFor(() => {
      expect(screen.getByText(/Nenhum pagamento encontrado/i)).toBeTruthy();
    });
  });
});
