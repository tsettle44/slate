import Setup from "./pages/Setup";
import "./App.css";

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <div className="logo">
          <span className="logo-mark">S</span>
          <div>
            <strong>Slate</strong>
            <span className="logo-sub">Companion</span>
          </div>
        </div>
      </header>
      <main className="app-main">
        <Setup />
      </main>
    </div>
  );
}
