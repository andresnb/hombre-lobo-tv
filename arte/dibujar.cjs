// Genera los PNG del ícono y el banner a partir de banner.svg. Uso único: node dibujar.cjs <ruta a sharp>
const fs = require('fs');
const sharp = require(process.argv[2]);
const lobo = '<path fill="#06080f" d="M-14 0 L-16 -28 Q-16 -46 -20 -58 L-31 -77 L-28 -80 L-16 -71 L-12 -81 L-8 -88 L-5 -73 Q4 -60 10 -42 Q24 -30 24 -12 Q34 -10 44 -3 L40 0 Z"/>';
let b = fs.readFileSync('banner.svg', 'utf8');
if (b.includes('translate(250 128)')) {
  b = b.replace(/<g transform="translate\(250 128\)[\s\S]*?<\/g>/, `<g transform="translate(246 152) scale(1.05)">${lobo}</g>`)
    .replace('cx="258" cy="52" r="58"', 'cx="250" cy="70" r="70"').replace('cx="258" cy="52" r="34"', 'cx="250" cy="70" r="44"')
    .replace('M0 150 Q60 128 120 146 T240 140 T320 136 V180 H0 Z', 'M0 156 Q70 140 140 154 Q200 146 230 152 Q280 140 320 150 V180 H0 Z').replace('fill="#14231a"', 'fill="#06080f"')
    .replace('y="104"', 'y="86"').replace('y="140"', 'y="124"');
  fs.writeFileSync('banner.svg', b);
}
const defs = b.match(/<defs>[\s\S]*?<\/defs>/)[0];
const icono = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 192 192" width="192" height="192">${defs}<rect width="192" height="192" fill="url(#cielo)"/><g fill="#fff" opacity="0.7"><circle cx="24" cy="30" r="1.4"/><circle cx="40" cy="96" r="1.1"/><circle cx="170" cy="150" r="1.2"/></g><circle cx="108" cy="78" r="80" fill="url(#halo)"/><circle cx="108" cy="78" r="54" fill="url(#luna)"/><path d="M0 172 Q60 156 110 168 Q150 160 192 166 V192 H0 Z" fill="#06080f"/><g transform="translate(92 182) scale(1.45)">${lobo}</g></svg>\n`;
fs.writeFileSync('icono.svg', icono);
const res = '../app/src/main/res/';
for (const d of ['drawable-xxxhdpi', 'mipmap-xxxhdpi']) fs.mkdirSync(res + d, { recursive: true });
(async () => {
  await sharp(Buffer.from(b), { density: 288 }).resize(640, 360).png().toFile(res + 'drawable-xxxhdpi/banner.png');
  await sharp(Buffer.from(icono), { density: 288 }).resize(192, 192).png().toFile(res + 'mipmap-xxxhdpi/ic_launcher.png');
  console.log('listo');
})();
