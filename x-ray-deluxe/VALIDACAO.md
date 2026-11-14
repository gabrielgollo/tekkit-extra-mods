# Evidências de validação

- Build: `./build.sh` compila contra os jars locais do Tekkit e instala o jar em `mods/`.
- `checks/run.sh`: testes de cliques do painel, limites de opacidade (0–100%), alcance e restauração dos padrões passaram no Java 8 do launcher.
- Jar conferido: inclui a atlas auxiliar `xray-hidden.png` e o handler de renderização.
- Teste isolado `SelectionCheck` executado fora do modpack: passou ao desmarcar e remarcar redstone (IDs 73 e 74 juntos), manter outro minério selecionado e salvar/recarregar uma seleção vazia.
- Assinatura `forge.IRenderWorldLastHandler.onRenderWorldLast(l, float)` conferida no Forge instalado.
- Altura válida 0–255 conferida na implementação 1.2.5 de World.

Captura do framebuffer de um mundo separado executado com o pack completo: `evidence/minerio-texturizado.png` mostra diamante com textura real e terreno transparente. O diagnóstico temporário foi removido após a captura. Testes OpenGL isolados verificaram mistura em 0/10/50/100%. Testes com as classes reais de petróleo parado/corrente verificaram preservação de tile e atlas em todos esses níveis.

Limitação atual: blocos com atlas própria e líquidos mantêm texturas originais; minérios enterrados são desenhados com suas texturas reais. RedPower e Nether Ores ainda são selecionados por grupo de ID, não por variante de metadados.

Captura integrada adicional: `evidence/opacidade-30-petroleo.png` mostra pedra translúcida a 30%, diamante com textura real e petróleo preto. Executada com o pack completo em mundo de diagnóstico separado. O jar temporário de diagnóstico foi removido.
