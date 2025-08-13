// 얇고 흐린 흰색의 "풀 박스" 렌더러 (라벨 배경 제거: 흰색 텍스트만)
export function makeSoftBoxRenderer(userOpts = {}) {
  const opts = {
    color: 'rgba(255,255,255,0.55)', // 박스 선
    strokeWidth: 1.25,
    glow: 3,
    radius: 0,                       // 0 = 완전 네모
    minBoxPx: 10,
    showLabel: true,
    font: '12px Inter',
    labelYOffset: 6,                 // 박스 위로 띄우는 간격(px)
    bboxFormat: 'auto',              // 'tlwh' | 'xyxy' | 'cxcywh' | 'auto'
    labelMap: (cls) => cls || 'Object',
    ...userOpts,
  };

  // --- helpers ---
  function toTLWH(b, W, H) {
    if (!Array.isArray(b) || b.length < 4) return [0,0,0,0];
    const [a,b2,c,d] = b.map(Number);
    const isNorm = [a,b2,c,d].every(v => v >= 0 && v <= 1);
    const px = (v, axis) => isNorm ? v * (axis === 'x' ? W : H) : v;
    const fmt = opts.bboxFormat;

    if (fmt === 'tlwh') return [px(a,'x'), px(b2,'y'), px(c,'x'), px(d,'y')];
    if (fmt === 'xyxy') {
      const x1 = px(a,'x'), y1 = px(b2,'y'), x2 = px(c,'x'), y2 = px(d,'y');
      return [x1, y1, Math.max(0, x2-x1), Math.max(0, y2-y1)];
    }
    if (fmt === 'cxcywh') {
      const cx = px(a,'x'), cy = px(b2,'y'), w = px(c,'x'), h = px(d,'y');
      return [cx - w/2, cy - h/2, w, h];
    }

    if (isNorm) {
      if (c > a && d > b2 && (a + c > 1 || b2 + d > 1)) {
        const x1 = a*W, y1 = b2*H, x2 = c*W, y2 = d*H;
        return [x1, y1, Math.max(0, x2-x1), Math.max(0, y2-y1)];
      }
      if ((a - c/2) >= 0 && (b2 - d/2) >= 0 && (a + c/2) <= 1 && (b2 + d/2) <= 1) {
        const cx = a*W, cy = b2*H, w = c*W, h = d*H;
        return [cx - w/2, cy - h/2, w, h];
      }
      return [a*W, b2*H, c*W, d*H];
    } else {
      if (c > a && d > b2) return [a, b2, c-a, d-b2];
      return [a, b2, c, d];
    }
  }

  function roundRect(ctx, x, y, w, h, r) {
    if (r <= 0) { ctx.strokeRect(x, y, w, h); return; }
    const rr = Math.min(r, w/2, h/2);
    ctx.beginPath();
    ctx.moveTo(x+rr, y);
    ctx.lineTo(x+w-rr, y);
    ctx.arcTo(x+w, y, x+w, y+rr, rr);
    ctx.lineTo(x+w, y+h-rr);
    ctx.arcTo(x+w, y+h, x+w-rr, y+h, rr);
    ctx.lineTo(x+rr, y+h);
    ctx.arcTo(x, y+h, x, y+h-rr, rr);
    ctx.lineTo(x, y+rr);
    ctx.arcTo(x, y, x+rr, y, rr);
    ctx.closePath();
    ctx.stroke();
  }

  // 배경 없이 흰색 텍스트만
    function drawLabel(ctx, W, text, x, y, w) {
    const gapY = 6;          // 박스 위로 띄우는 간격
    const padX = 4;          // 왼쪽 선에서 살짝 안쪽으로
    const tw = ctx.measureText(text).width;
    const lh = Math.ceil(parseInt(ctx.font, 10) * 1.1) || 16;

    ctx.save();
    ctx.fillStyle = '#fff';
    ctx.textAlign = 'left';
    ctx.textBaseline = 'top';

    // x: 왼쪽 선에서 약간 오른쪽 (필요시 화면 밖으로 안 나가게 클램프)
    let tx = x + padX;
    if (tx + tw > W) tx = Math.max(0, W - tw);

    // y: 박스 상단 바깥쪽. 화면 위로 넘치면 상단 안쪽으로 내린다.
    let ty = y - lh - gapY;
    if (ty < 0) ty = y + gapY;

    ctx.fillText(text, tx, ty);
    ctx.restore();
    }
    
  // --- main renderer ---
  return function render(preds, ctx) {
    const canvas = ctx.canvas;
    const W = canvas.width || canvas.clientWidth || 1;
    const H = canvas.height || canvas.clientHeight || 1;

    ctx.save();
    ctx.clearRect(0, 0, W, H);
    ctx.font = opts.font;
    ctx.textBaseline = 'top';
    ctx.lineWidth = opts.strokeWidth;
    ctx.strokeStyle = opts.color;
    ctx.shadowBlur = opts.glow;
    ctx.shadowColor = opts.color;

    const items = Array.isArray(preds) ? preds : [];
    for (const p of items) {
      const conf = Number(p?.confidence ?? 0);
      if (!p?.bbox) continue;

      let [x, y, w, h] = toTLWH(p.bbox, W, H);
      if (w < opts.minBoxPx || h < opts.minBoxPx) continue;

      roundRect(ctx, x, y, w, h, opts.radius);

      if (opts.showLabel) {
        const text = `${opts.labelMap(p.class)}${Number.isFinite(conf) ? `  ${(conf*100|0)}%` : ''}`;
        drawLabel(ctx, W, text, x, y, w);
      }
    }
    ctx.restore();
  };
}
