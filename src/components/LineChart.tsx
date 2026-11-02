import React, { useRef, useEffect } from 'react';

interface LineChartProps {
  data: number[];
  minVal: number;
  maxVal: number;
  color: string;
  unit: string;
  height?: number;
  targetLineVal?: number;
}

export const LineChart: React.FC<LineChartProps> = ({
  data,
  minVal,
  maxVal,
  color,
  unit,
  height = 130,
  targetLineVal,
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const dpr = window.devicePixelRatio || 1;
    const parent = canvas.parentElement;
    const w = parent ? parent.clientWidth : 300;
    const h = height;

    canvas.width = w * dpr;
    canvas.height = h * dpr;
    ctx.scale(dpr, dpr);

    ctx.clearRect(0, 0, w, h);

    // Subtle horizontal gridlines
    ctx.strokeStyle = 'rgba(51, 71, 102, 0.3)';
    ctx.lineWidth = 1;
    const gridLines = 4;
    for (let i = 1; i <= gridLines; i++) {
      const y = (h / (gridLines + 1)) * i;
      ctx.beginPath();
      ctx.moveTo(8, y);
      ctx.lineTo(w - 8, y);
      ctx.stroke();
    }

    // Target threshold line (e.g. 500m apogee)
    if (targetLineVal !== undefined) {
      const normTarget = Math.max(0, Math.min(1, (targetLineVal - minVal) / (maxVal - minVal)));
      const targetY = h - (normTarget * (h - 28)) - 14;
      ctx.strokeStyle = 'rgba(0, 209, 255, 0.45)';
      ctx.lineWidth = 1;
      ctx.setLineDash([4, 4]);
      ctx.beginPath();
      ctx.moveTo(8, targetY);
      ctx.lineTo(w - 8, targetY);
      ctx.stroke();
      ctx.setLineDash([]);

      // Subtle target label
      ctx.fillStyle = 'rgba(0, 209, 255, 0.6)';
      ctx.font = '9px ui-monospace, SFMono-Regular, monospace';
      ctx.fillText(`TARGET ${targetLineVal}${unit}`, w - 75, targetY - 4);
    }

    if (data.length < 2) {
      ctx.fillStyle = 'rgba(148, 163, 184, 0.4)';
      ctx.font = '10px ui-monospace, monospace';
      ctx.fillText('AWAITING TELEMETRY...', w / 2 - 60, h / 2);
      return;
    }

    const step = (w - 16) / Math.max(20, data.length - 1);
    const getCoords = (idx: number, val: number) => {
      const norm = Math.max(0, Math.min(1, (val - minVal) / (maxVal - minVal)));
      const x = 8 + idx * step;
      const y = h - (norm * (h - 28)) - 14;
      return { x, y };
    };

    // Calculate points
    const points = data.map((val, i) => getCoords(i, val));

    // Area Fill with smooth bezier curve
    ctx.beginPath();
    ctx.moveTo(points[0].x, points[0].y);

    for (let i = 0; i < points.length - 1; i++) {
      const p0 = points[i];
      const p1 = points[i + 1];
      const midX = (p0.x + p1.x) / 2;
      ctx.bezierCurveTo(midX, p0.y, midX, p1.y, p1.x, p1.y);
    }

    const lastPt = points[points.length - 1];
    ctx.lineTo(lastPt.x, h - 6);
    ctx.lineTo(points[0].x, h - 6);
    ctx.closePath();

    const fillGrad = ctx.createLinearGradient(0, 0, 0, h);
    fillGrad.addColorStop(0, `${color}30`);
    fillGrad.addColorStop(0.7, `${color}0A`);
    fillGrad.addColorStop(1, `${color}00`);
    ctx.fillStyle = fillGrad;
    ctx.fill();

    // Line Stroke with smooth curve
    ctx.beginPath();
    ctx.moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length - 1; i++) {
      const p0 = points[i];
      const p1 = points[i + 1];
      const midX = (p0.x + p1.x) / 2;
      ctx.bezierCurveTo(midX, p0.y, midX, p1.y, p1.x, p1.y);
    }
    ctx.strokeStyle = color;
    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.stroke();

    // Glowing tip at the most recent reading
    ctx.beginPath();
    ctx.arc(lastPt.x, lastPt.y, 7, 0, Math.PI * 2);
    ctx.fillStyle = `${color}25`;
    ctx.fill();

    ctx.beginPath();
    ctx.arc(lastPt.x, lastPt.y, 3.5, 0, Math.PI * 2);
    ctx.fillStyle = color;
    ctx.fill();
    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = 1.5;
    ctx.stroke();

    // Current value top-right readout
    const latestVal = data[data.length - 1];
    ctx.fillStyle = color;
    ctx.font = '600 11px ui-monospace, SFMono-Regular, monospace';
    const text = `${latestVal.toFixed(1)} ${unit}`;
    const textWidth = ctx.measureText(text).width;
    ctx.fillText(text, w - textWidth - 12, 14);
  }, [data, minVal, maxVal, color, unit, height, targetLineVal]);

  return (
    <div className="w-full relative overflow-hidden rounded-lg">
      <canvas
        ref={canvasRef}
        className="w-full bg-[#070c17]/60 block transition-all"
        style={{ height: `${height}px` }}
      />
    </div>
  );
};
