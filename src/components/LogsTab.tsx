import React, { useState } from 'react';
import { Download, Search, FileText, Filter, AlertCircle, CheckCircle2, AlertTriangle } from 'lucide-react';
import { MissionEvent, EventSeverity, MissionPhase } from '../types/telemetry';

interface LogsTabProps {
  events: MissionEvent[];
}

export const LogsTab: React.FC<LogsTabProps> = ({ events }) => {
  const [search, setSearch] = useState('');
  const [phaseFilter, setPhaseFilter] = useState<string>('ALL');
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');

  // Export full mission event logbook as CSV
  const exportCSV = () => {
    let csv = 'id,timestamp_met,timestamp_utc,phase,severity,event_type,altitude_m,coordinates,packet_id,message\n';
    events.forEach((e) => {
      csv += `"${e.id}","${e.time}","${e.utcTime}","${e.phase}","${e.severity}","${e.type}",${e.alt ?? ''},"${e.coords ?? ''}",${e.packetId ?? ''},"${e.msg.replace(/"/g, '""')}"\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `AetherView_Advaita_Mission_Logbook_${Date.now()}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const filteredEvents = events.filter((e) => {
    const matchesPhase = phaseFilter === 'ALL' || e.phase === phaseFilter;
    const matchesSeverity = severityFilter === 'ALL' || e.severity === severityFilter;
    const matchesSearch =
      search === '' ||
      e.msg.toLowerCase().includes(search.toLowerCase()) ||
      e.type.toLowerCase().includes(search.toLowerCase()) ||
      e.time.includes(search) ||
      (e.packetId && e.packetId.toString().includes(search));
    return matchesPhase && matchesSeverity && matchesSearch;
  });

  const severityBadges: Record<EventSeverity, { bg: string; text: string; border: string }> = {
    INFO: { bg: 'bg-cyan-500/10', text: 'text-cyan-300', border: 'border-cyan-500/30' },
    SUCCESS: { bg: 'bg-emerald-500/10', text: 'text-emerald-400', border: 'border-emerald-500/30' },
    WARNING: { bg: 'bg-amber-500/10', text: 'text-amber-400', border: 'border-amber-500/30' },
    CRITICAL: { bg: 'bg-red-500/10', text: 'text-red-400', border: 'border-red-500/30' },
  };

  return (
    <div className="space-y-4">
      {/* Top Banner & Multi-level Filter Bar */}
      <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-3 pb-2.5 border-b border-slate-800/60">
        <div className="flex items-center space-x-3">
          <FileText className="w-5 h-5 text-cyan-400" />
          <div>
            <h2 className="text-sm font-bold text-white uppercase tracking-wider">
              DTU RYIC 2026 Mission Event Logbook
            </h2>
            <div className="text-[10px] text-slate-400 mono">
              Chronological audit trail • Total events logged: {events.length}
            </div>
          </div>
        </div>

        {/* Filter Controls */}
        <div className="flex items-center space-x-2.5 flex-wrap gap-y-2">
          {/* Search box */}
          <div className="relative">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search event messages..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="bg-slate-900/60 pl-8 pr-3 py-1.5 rounded-lg border border-slate-800 text-xs mono text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-cyan-400 w-52 transition"
            />
          </div>

          {/* Phase Filter */}
          <select
            value={phaseFilter}
            onChange={(e) => setPhaseFilter(e.target.value)}
            className="bg-slate-900/60 px-2.5 py-1.5 rounded-lg border border-slate-800 text-xs mono text-slate-300 focus:outline-none focus:border-cyan-400"
          >
            <option value="ALL">All Phases</option>
            <option value="STANDBY">Pre-launch</option>
            <option value="ARMED">Armed</option>
            <option value="DEPLOYED">Deployment</option>
            <option value="DESCENT">Descent</option>
            <option value="LANDING">Landing</option>
            <option value="RECOVERY">Recovery</option>
          </select>

          {/* Severity Filter */}
          <select
            value={severityFilter}
            onChange={(e) => setSeverityFilter(e.target.value)}
            className="bg-slate-900/60 px-2.5 py-1.5 rounded-lg border border-slate-800 text-xs mono text-slate-300 focus:outline-none focus:border-cyan-400"
          >
            <option value="ALL">All Severities</option>
            <option value="INFO">INFO</option>
            <option value="SUCCESS">SUCCESS</option>
            <option value="WARNING">WARNING</option>
            <option value="CRITICAL">CRITICAL</option>
          </select>

          {/* Export CSV button */}
          <button
            onClick={exportCSV}
            className="px-3.5 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold transition-all flex items-center space-x-1.5 shadow-[0_0_12px_rgba(16,185,129,0.3)] active:scale-95"
          >
            <Download className="w-3.5 h-3.5" />
            <span>EXPORT LOGS</span>
          </button>
        </div>
      </div>

      {/* Logbook Table */}
      <div className="glass-panel rounded-xl overflow-hidden">
        <div className="overflow-x-auto max-h-[540px] custom-scroll">
          <table className="w-full text-left text-xs mono">
            <thead className="bg-slate-950/80 text-slate-400 text-[10px] tracking-wider uppercase sticky top-0 border-b border-slate-800 z-10">
              <tr>
                <th className="p-3">MET</th>
                <th className="p-3">UTC TIME</th>
                <th className="p-3">PHASE</th>
                <th className="p-3">SEVERITY</th>
                <th className="p-3">CATEGORY</th>
                <th className="p-3">ALT (m)</th>
                <th className="p-3">EVENT AUDIT LOG MESSAGE</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {filteredEvents.length === 0 ? (
                <tr>
                  <td colSpan={7} className="p-8 text-center text-slate-500 text-xs">
                    No matching log events recorded for current filter criteria.
                  </td>
                </tr>
              ) : (
                filteredEvents.map((e) => {
                  const badge = severityBadges[e.severity] || severityBadges.INFO;
                  return (
                    <tr key={e.id} className="hover:bg-slate-800/40 transition">
                      <td className="p-2.5 text-cyan-400 font-bold">{e.time}</td>
                      <td className="p-2.5 text-slate-400">{e.utcTime}</td>
                      <td className="p-2.5">
                        <span className="px-2 py-0.5 rounded text-[9px] font-bold bg-slate-800 text-slate-200">
                          {e.phase}
                        </span>
                      </td>
                      <td className="p-2.5">
                        <span
                          className={`px-2 py-0.5 rounded text-[9px] font-bold border ${badge.bg} ${badge.text} ${badge.border}`}
                        >
                          {e.severity}
                        </span>
                      </td>
                      <td className="p-2.5 text-slate-400 font-medium">{e.type}</td>
                      <td className="p-2.5 text-white font-bold">
                        {e.alt !== undefined ? `${e.alt.toFixed(1)}m` : '—'}
                      </td>
                      <td className="p-2.5 text-slate-200 max-w-xl">{e.msg}</td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
