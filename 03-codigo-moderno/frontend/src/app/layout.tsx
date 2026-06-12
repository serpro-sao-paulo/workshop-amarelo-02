import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "SIFAP 2.0",
  description: "Sistema de Fiscalização e Administração de Pagamentos",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="pt-BR">
      <body className="min-h-screen bg-slate-900 text-slate-100 font-sans">
        <nav className="bg-slate-800 border-b border-slate-700 px-6 py-3 flex gap-6">
          <a href="/" className="hover:text-blue-400 transition-colors">
            Home
          </a>
          <a href="/beneficiarios" className="hover:text-blue-400 transition-colors">
            Beneficiários
          </a>
          <a href="/programas" className="hover:text-blue-400 transition-colors">
            Programas
          </a>
          <a href="/pagamentos" className="hover:text-blue-400 transition-colors">
            Pagamentos
          </a>
        </nav>
        <main className="px-6 py-8">{children}</main>
      </body>
    </html>
  );
}
