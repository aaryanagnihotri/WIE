import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import useDashboard from '../components/useDashboard'; import { Spinner, Notice } from '../components/States'; import StatCard from '../components/StatCard'; import InterventionCards from '../components/InterventionCards';
const risk = { HIGH: 'bg-red-500/15 text-red-300', MEDIUM: 'bg-amber-500/15 text-amber-300', LOW: 'bg-emerald-500/15 text-emerald-300' };
export default function RecruiterDashboard() {
  const { data: d, error } = useDashboard('recruiter');
  if (error) return <Notice title="Couldn't load dashboard" text={error}/>; if (!d) return <Spinner/>;
  const top = [...d.riskRadar].sort((a, b) => b.gap - a.gap);
  return (<div className="space-y-8"><h1 className="text-2xl font-semibold">{d.company} · Workforce Risk</h1>
    <div className="grid grid-cols-2 gap-4"><StatCard label="Open Positions" value={d.openPositions}/><StatCard i={1} label="Talent Pipeline" value={d.talentPipeline}/></div>
    <section className="rounded-2xl bg-card p-5"><h2 className="mb-3 font-semibold">Supply vs projected demand</h2>
      <ResponsiveContainer width="100%" height={280}><BarChart data={d.riskRadar}><CartesianGrid stroke="rgba(255,255,255,.06)"/><XAxis dataKey="skill" stroke="#94a3b8" fontSize={12}/><YAxis stroke="#94a3b8" fontSize={12}/>
        <Tooltip contentStyle={{ background: '#0b1226', border: 0 }}/><Legend/><Bar dataKey="currentSupply" name="Supply" fill="#3b82f6" animationDuration={1000}/><Bar dataKey="projectedDemand" name="Demand" fill="#8b5cf6" animationDuration={1000}/></BarChart></ResponsiveContainer></section>
    <section className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">{top.map(s => (<div key={s.skill} className="rounded-2xl bg-card p-4">
      <div className="flex items-center justify-between"><b>{s.skill}</b><span className={`rounded-full px-2 py-0.5 text-xs ${risk[s.risk]}`}>{s.risk}</span></div>
      <p className="mt-2 text-sm text-slate-400">Talent gap <span className="text-white">{s.gap}</span> · momentum {s.momentumPct > 0 ? '↑' : '↓'} {Math.abs(s.momentumPct)}%</p></div>))}</section>
    <section><h2 className="mb-3 font-semibold">Intervention: Edge AI</h2><InterventionCards items={d.interventions}/></section></div>);
}
