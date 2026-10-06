import { NavLink } from 'react-router-dom'; import { LogOut, Network } from 'lucide-react'; import { useAuth, HOME } from '../context/AuthContext';
export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const links = [['Dashboard', HOME[user.role]], ['Simulator', '/simulator'], ...(user.role === 'STUDENT' ? [['Assessment', '/assessment'], ['What-If', '/whatif']] : []), ['Alerts', '/alerts'], ['Outcomes', '/outcomes']];
  return (<div className="min-h-screen">
    <header className="flex flex-wrap items-center justify-between gap-2 border-b border-white/10 px-4 py-3 md:px-8">
      <div className="flex items-center gap-2 font-semibold"><Network className="text-electric" size={20}/> Workforce Intelligence</div>
      <nav className="order-3 flex w-full gap-4 overflow-x-auto text-sm md:order-none md:w-auto">{links.map(([t, to]) => (
        <NavLink key={to} to={to} end className={({ isActive }) => isActive ? 'text-white' : 'text-slate-400 hover:text-white'}>{t}</NavLink>))}</nav>
      <button onClick={logout} className="flex items-center gap-1 text-sm text-slate-400 hover:text-white"><LogOut size={16}/> Logout</button>
    </header><main className="mx-auto max-w-6xl p-4 md:p-8">{children}</main></div>);
}
