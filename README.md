# Mods do Gabriel Gollo

Monorepo dos mods cliente para Minecraft. Cada mod mantém seu próprio código, documentação e script de build.

## Projetos

- [`controller-assistant/`](controller-assistant/README.md): navegação com controle e assistências para Tekkit Classic.
- [`x-ray-deluxe/`](x-ray-deluxe/README.md): X-Ray configurável para Tekkit Classic.

As versões atuais foram feitas especificamente para Minecraft 1.2.5 e Forge Mod Loader do Tekkit Classic; não são jars universais para outras versões.

## Build

Este workspace espera encontrar o pack em `../technic/modpacks/tekkit`.

```sh
./build.sh                 # compila os dois jars em suas pastas dist/
./build.sh --install       # compila e instala os dois jars na pasta mods do pack
```

Também é possível construir um projeto isoladamente com `./controller-assistant/build.sh` ou `./x-ray-deluxe/build.sh`. Requer JDK 8 ou superior.
