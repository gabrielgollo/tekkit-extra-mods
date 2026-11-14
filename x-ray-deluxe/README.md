# X-Ray Deluxe para Tekkit Classic 1.2.5

Mod cliente em formato ModLoader/BaseMod, compilado contra o Forge ModLoader 2.2 presente neste modpack.

## Controles

- **X**: liga/desliga o X-Ray. Os blocos que não estão na lista de minérios usam o tile transparente da atlas do Minecraft; os minérios selecionados continuam visíveis.
- **C**: abre o painel.
- Lava parada e corrente sempre aparecem com o X-Ray ligado, dentro do alcance configurado, mesmo sem nenhum minério selecionado.
- Na linha **Opacidade hidden**, clique à esquerda para diminuir e à direita para aumentar em passos de 10%, entre 0% e 100%. O valor é salvo em `hiddenOpacity` na configuração.
- No painel, clique nos nomes para marcar ou desmarcar minérios. A seleção é salva automaticamente. Clique no estado para ligar/desligar; **ESC** fecha.

A seleção inicial inclui ouro, ferro, carvão, lápis-lazúli, diamante, redstone, Nether Ores, RedPower e cobre, estanho e urânio do IC2. RedPower e Nether Ores são selecionados por grupo de bloco nesta versão.

As faces com as texturas reais mostram os blocos selecionados através das paredes no raio configurado em `config/x-ray-deluxe.cfg`. O raio padrão é 12 blocos. A busca cobre esse alcance nos três eixos, respeitando a altura de 0 a 255 do mundo. As texturas de blocos da atlas vanilla ficam transparentes; líquidos e atlas próprias dos mods conservam suas texturas para evitar o petróleo roxo.

## Build e instalação

Execute `./build.sh` para criar o jar em `dist/`; use `./build.sh --install` para copiá-lo para `technic/modpacks/tekkit/mods/`. Na raiz do monorepo, `./build.sh --install` compila e instala os dois mods.

Requisitos de compilação: JDK 8 ou superior. O bytecode produzido é Java 8.

## Default selection JSON

Edit `config/x-ray-deluxe-defaults.json` in the modpack. The `oreIds` array controls the initial selection and the **Reset defaults** button:

```json
{ "oreIds": [14, 15, 16, 21, 56, 73, 74, 135, 140, 247, 248, 249] }
```

The existing personal selection in `x-ray-deluxe.cfg` is preserved. After editing JSON, click **Reset defaults** to apply it. An empty array selects no ores. Lava remains visible. Invalid JSON falls back to the built-in selection and logs the error. IDs 135 and 140 select all metadata variants of their ore groups. The optional `groups` and `description` fields are documentation only.

Coverage checked against the Tekkit Classic Wiki and local pack configuration: vanilla coal, iron, gold, diamond, lapis and redstone; IC2 copper/tin/uranium; all eight RedPower variants (ruby, emerald, sapphire, silver, tin, copper, tungsten and nikolite); all eight installed Nether Ores (coal, diamond, gold, iron, lapis, redstone, copper and tin). RedPower emerald is a separate pre-1.3 gem, not vanilla emerald ore.

Sources: [Nether Ores](https://tekkitclassic.fandom.com/wiki/Nether_Ores), [Tunnel Bore ore IDs](https://tekkitclassic.fandom.com/wiki/Tunnel_Bore), [Gems](https://tekkitclassic.fandom.com/wiki/Gems).
