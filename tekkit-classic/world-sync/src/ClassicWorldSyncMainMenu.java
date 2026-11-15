final class ClassicWorldSyncMainMenu extends xt {
    private static final int BUTTON_ID = 18342;
    private final mod_WorldSync mod;

    ClassicWorldSyncMainMenu(mod_WorldSync mod) {
        this.mod = mod;
    }

    @SuppressWarnings("unchecked")
    public void c() {
        super.c();
        String label = p != null && p.A != null && "pt_BR".equalsIgnoreCase(p.A.R)
                ? "Sincronização de mundos" : "World Sync";
        s.add(new abp(BUTTON_ID, 164, r - 31, 150, 20, label));
    }

    protected void a(abp button) {
        if (button.f == BUTTON_ID) {
            p.a(new ClassicWorldSyncGui(mod, false));
            return;
        }
        super.a(button);
    }
}
