import net.minecraft.client.Minecraft;

final class XRayDeluxeGui extends vp {
    public void a(int mouseX, int mouseY, float tick) {
        super.a();
        int left=(q-280)/2, top=(r-240)/2;
        a(left,top,left+280,top+240,0xEE202530);
        b(p.q,mod_XRayDeluxe.tr(p,"X-Ray Deluxe | X: toggle | ESC: close","X-Ray Deluxe | X: ligar | ESC: fechar"),left+8,top+8,0xFFFFFF);
        b(p.q,mod_XRayDeluxe.tr(p,"Status: ","Estado: ") + (mod_XRayDeluxe.enabled ? mod_XRayDeluxe.tr(p,"ON","ATIVO") : mod_XRayDeluxe.tr(p,"OFF","DESLIGADO")),left+8,top+25,0xFFD36A);
        a(left+171,top+22,left+273,top+38,mod_XRayDeluxe.fullbright ? 0xFF365E48 : 0xFF34485D);
        b(p.q,mod_XRayDeluxe.tr(p,"Fullbright: ","Clarear: ")+(mod_XRayDeluxe.fullbright ? mod_XRayDeluxe.tr(p,"ON","SIM") : mod_XRayDeluxe.tr(p,"OFF","NAO")),left+176,top+26,0xFFFFFF);
        int hovered=-1;
        for(int i=0;i<mod_XRayDeluxe.choices.length;i++) {
            int x=left+12+(i%4)*66, y=top+45+(i/4)*48;
            boolean checked=mod_XRayDeluxe.isOre(mod_XRayDeluxe.choices[i]);
            boolean hover=mouseX>=x && mouseX<x+60 && mouseY>=y && mouseY<y+42;
            a(x,y,x+60,y+42,checked ? 0xFF365E48 : 0xFF303A48);
            a(x+1,y+1,x+59,y+2,hover ? 0xFFFFFFFF : (checked ? 0xFF8DFF9D : 0xFF687687));
            mod_XRayDeluxe.drawOreIcon(p,mod_XRayDeluxe.choices[i],x+22,y+6);
            b(p.q,checked ? "[x]" : "[ ]",x+22,y+28,checked ? 0x8DFF9D : 0xCCCCCC);
            if(hover) hovered=i;
        }
        if(hovered>=0) b(p.q,mod_XRayDeluxe.tr(p,mod_XRayDeluxe.names[hovered],mod_XRayDeluxe.portugueseNames[hovered]),left+12,top+191,0xFFFFFF);
        a(left+7,top+201,left+58,top+217,0xFF34485D);
        a(left+63,top+201,left+114,top+217,0xFF34485D);
        a(left+174,top+201,left+273,top+217,0xFF34485D);
        b(p.q,mod_XRayDeluxe.tr(p,"Range -","Raio -"),left+12,top+205,0xFFFFFF);
        b(p.q,mod_XRayDeluxe.tr(p,"Range +","Raio +"),left+68,top+205,0xFFFFFF);
        b(p.q,String.valueOf(mod_XRayDeluxe.radius),left+135,top+205,0xFFD36A);
        b(p.q,mod_XRayDeluxe.tr(p,"Reset defaults","Restaurar padrao"),left+179,top+205,0xFFFFFF);
        b(p.q,mod_XRayDeluxe.tr(p,"Hidden opacity: ","Opacidade oculta: ") + mod_XRayDeluxe.hiddenOpacity + "%  [- / +]",left+8,top+226,0xBCC7D4);
    }
    protected void a(int x,int y,int button) {
        if(button!=0) return;
        int left=(q-280)/2, top=(r-240)/2;
        if(x<left+7 || x>left+273) return;
        if(y>=top+221 && y<=top+239) {
            mod_XRayDeluxe.hiddenOpacity=Math.max(0,Math.min(100,mod_XRayDeluxe.hiddenOpacity+(x<left+140 ? -10 : 10)));
            mod_XRayDeluxe.refreshSelection(p); return;
        }
        if(y>=top+201 && y<=top+217) {
            if(x<=left+58) mod_XRayDeluxe.radius=Math.max(4,mod_XRayDeluxe.radius-4);
            else if(x<=left+114) mod_XRayDeluxe.radius=Math.min(32,mod_XRayDeluxe.radius+4);
            else if(x>=left+174) mod_XRayDeluxe.oreIds=mod_XRayDeluxe.defaultOreIds();
            mod_XRayDeluxe.refreshSelection(p);
            return;
        }
        if(y>=top+22 && y<top+38 && x>=left+171) { mod_XRayDeluxe.toggleFullbright(p); return; }
        if(y>=top+23 && y<top+38) { mod_XRayDeluxe.setEnabled(p,!mod_XRayDeluxe.enabled); return; }
        for(int i=0;i<mod_XRayDeluxe.choices.length;i++) {
            int cellX=left+12+(i%4)*66, cellY=top+45+(i/4)*48;
            if(x>=cellX && x<cellX+60 && y>=cellY && y<cellY+42) {
                mod_XRayDeluxe.toggleOre(p,mod_XRayDeluxe.choices[i]); return;
            }
        }
    }
    protected void a(char character,int code) {
        if(code==45) mod_XRayDeluxe.setEnabled(p,!mod_XRayDeluxe.enabled);
        else super.a(character,code);
    }
}
