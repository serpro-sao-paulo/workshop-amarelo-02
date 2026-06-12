/**
 * Home / dashboard do SIFAP 2.0.
 */

const CARDS = [
  {
    href: "/beneficiarios",
    title: "Beneficiários",
    desc: "Cadastrar, consultar e validar beneficiários de programas sociais.",
    icon: "👥",
  },
  {
    href: "/programas",
    title: "Programas Sociais",
    desc: "Cadastrar programas sociais, faixas de cálculo e parâmetros regionais.",
    icon: "📋",
  },
  {
    href: "/pagamentos",
    title: "Pagamentos",
    desc: "Gerar ciclo mensal e consultar o histórico de pagamentos.",
    icon: "💰",
  },
];

export default function Home() {
  return (
    <div className="max-w-4xl mx-auto">
      <header className="text-center mb-12 mt-8">
        <h1 className="text-4xl font-bold mb-3">SIFAP 2.0</h1>
        <p className="text-slate-400">
          Sistema de Fiscalização e Administração de Pagamentos
        </p>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {CARDS.map((card) => (
          <a
            key={card.href}
            href={card.href}
            className="bg-slate-800 border border-slate-700 rounded-xl p-6
                       hover:border-blue-500 hover:bg-slate-800/70 transition-all"
          >
            <div className="text-3xl mb-3">{card.icon}</div>
            <h2 className="text-xl font-semibold mb-2">{card.title}</h2>
            <p className="text-slate-400 text-sm">{card.desc}</p>
          </a>
        ))}
      </div>

      <footer className="text-center text-xs text-slate-600 mt-12">
        Modernização do legado SIFAP (Natural/Adabas) — Java 21 + Next.js 15
      </footer>
    </div>
  );
}
