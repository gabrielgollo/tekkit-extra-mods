final class AssistantGui extends vp {
    private boolean controllerPage;
    private String t(String en,String pt){return mod_GenericAssistant.text(p,en,pt);}
    public void a(int mx,int my,float tick){
        super.a();int left=(q-280)/2,top=(r-230)/2;
        a(left,top,left+280,top+230,0xEE202530);
        b(p.q,"Generic Assistant",left+10,top+10,0xFFFFFF);
        if(controllerPage){drawController(mx,my,left,top);return;}
        String[] rows={t("Auto tool: ","Ferramenta automatica: ")+(mod_GenericAssistant.autoTool?t("ON","SIM"):t("OFF","NAO")),t("Auto jump: ","Pulo automatico: ")+(mod_GenericAssistant.autoJump?t("ON","SIM"):t("OFF","NAO")),t("Auto treetap: ","Treetap automatico: ")+(mod_GenericAssistant.autoTreetap?t("ON","SIM"):t("OFF","NAO")),t("Refill hotbar: ","Repor hotbar: ")+(mod_GenericAssistant.refillHotbar?t("ON","SIM"):t("OFF","NAO")),t("Replace broken tool: ","Trocar ferramenta quebrada: ")+(mod_GenericAssistant.replaceBrokenTool?t("ON","SIM"):t("OFF","NAO"))};
        for(int i=0;i<5;i++){int y=top+30+i*24;a(left+10,y,left+270,y+20,0xFF34485D);b(p.q,rows[i],left+17,y+8,0xFFFFFF);}
        String device=mod_GenericAssistant.instance==null ? "" : mod_GenericAssistant.instance.pad.status();
        if(device.equals("No controller detected"))device=t("No controller detected","Nenhum controle detectado");
        b(p.q,t("Controller: ","Controle: ")+device,left+10,top+155,0xFFD36A);
        String axes=mod_GenericAssistant.instance==null ? "" : mod_GenericAssistant.instance.pad.axes();
        b(p.q,t("Axes: ","Eixos: ")+axes,left+10,top+168,0xBCC7D4);
        a(left+10,top+181,left+270,top+201,0xFF34485D);
        b(p.q,t("Controller settings","Configurar controle"),left+17,top+187,0xFFFFFF);
        b(p.q,t("F8 / R-stick: settings | Back: X-Ray","F8 / analogico direito: opcoes | Back: X-Ray"),left+10,top+205,0xBCC7D4);
        b(p.q,t("ESC / B: close. Changes saved automatically.","ESC / B: fechar. Alteracoes salvas automaticamente."),left+10,top+218,0xBCC7D4);
    }
    protected void a(int x,int y,int button){
        if(button!=0)return;int left=(q-280)/2,top=(r-230)/2;
        if(controllerPage){clickController(x,y,left,top);return;}
        if(x<left+10 || x>left+270)return;
        if(y>=top+181 && y<top+201 && mod_GenericAssistant.instance!=null){controllerPage=true;return;}
        for(int i=0;i<5;i++)if(y>=top+30+i*24 && y<top+50+i*24){
            if(i==0)mod_GenericAssistant.autoTool=!mod_GenericAssistant.autoTool;
            if(i==1)mod_GenericAssistant.autoJump=!mod_GenericAssistant.autoJump;
            if(i==2)mod_GenericAssistant.autoTreetap=!mod_GenericAssistant.autoTreetap;
            if(i==3)mod_GenericAssistant.refillHotbar=!mod_GenericAssistant.refillHotbar;
            if(i==4)mod_GenericAssistant.replaceBrokenTool=!mod_GenericAssistant.replaceBrokenTool;
            if(mod_GenericAssistant.instance!=null)mod_GenericAssistant.instance.saveSettings();return;
        }
    }
    private void drawController(int mx,int my,int left,int top){
        mod_GenericAssistant mod=mod_GenericAssistant.instance;
        if(mod==null)return;
        AssistantPad pad=mod.pad;
        String[] names={t("Camera sensitivity","Sensibilidade da camera"),t("Stick deadzone","Zona morta"),t("Menu cursor speed","Velocidade do cursor")};
        float[] values={pad.lookSpeed,pad.deadzone,pad.cursorSpeed};
        for(int i=0;i<3;i++){
            int y=top+35+i*30;
            new abp(0,left+10,y,30,20,"-").a(p,mx,my);
            new abp(0,left+240,y,30,20,"+").a(p,mx,my);
            String value=i==1?Math.round(values[i]*100)+"%":String.format(java.util.Locale.ROOT,"%.1f",values[i]);
            b(p.q,names[i]+": "+value,left+47,y+6,0xFFFFFF);
        }
        new abp(0,left+10,top+128,260,20,t("Invert vertical camera: ","Inverter camera vertical: ")+(pad.invertLookY?t("ON","SIM"):t("OFF","NAO"))).a(p,mx,my);
        new abp(0,left+10,top+158,260,20,t("Scan controllers again","Buscar controles novamente")).a(p,mx,my);
        new abp(0,left+10,top+188,260,20,t("Assistance settings","Assistencias")).a(p,mx,my);
    }
    private void clickController(int x,int y,int left,int top){
        mod_GenericAssistant mod=mod_GenericAssistant.instance;
        if(mod==null || x<left+10 || x>left+270)return;
        AssistantPad pad=mod.pad;
        for(int i=0;i<3;i++)if(y>=top+35+i*30 && y<top+55+i*30){
            int direction=x<left+40?-1:x>=left+240?1:0;
            if(direction==0)return;
            if(i==0)pad.lookSpeed=Math.max(.1f,Math.min(30f,pad.lookSpeed+direction*.5f));
            if(i==1)pad.deadzone=Math.max(.05f,Math.min(.8f,pad.deadzone+direction*.05f));
            if(i==2)pad.cursorSpeed=Math.max(1f,Math.min(40f,pad.cursorSpeed+direction));
            mod.saveSettings();return;
        }
        if(y>=top+128 && y<top+148){pad.invertLookY=!pad.invertLookY;mod.saveSettings();}
        else if(y>=top+158 && y<top+178)pad.reconnect();
        else if(y>=top+188 && y<top+208)controllerPage=false;
    }
}
