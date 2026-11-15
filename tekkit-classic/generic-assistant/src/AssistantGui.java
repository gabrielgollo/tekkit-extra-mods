final class AssistantGui extends vp {
    private String t(String en,String pt){return mod_GenericAssistant.text(p,en,pt);}
    public void a(int mx,int my,float tick){
        super.a();int left=(q-280)/2,top=(r-230)/2;
        a(left,top,left+280,top+230,0xEE202530);
        b(p.q,"Generic Assistant",left+10,top+10,0xFFFFFF);
        String[] rows={t("Auto tool: ","Ferramenta automatica: ")+(mod_GenericAssistant.autoTool?t("ON","SIM"):t("OFF","NAO")),t("Auto jump: ","Pulo automatico: ")+(mod_GenericAssistant.autoJump?t("ON","SIM"):t("OFF","NAO")),t("Auto treetap: ","Treetap automatico: ")+(mod_GenericAssistant.autoTreetap?t("ON","SIM"):t("OFF","NAO")),t("Refill hotbar: ","Repor hotbar: ")+(mod_GenericAssistant.refillHotbar?t("ON","SIM"):t("OFF","NAO")),t("Replace broken tool: ","Trocar ferramenta quebrada: ")+(mod_GenericAssistant.replaceBrokenTool?t("ON","SIM"):t("OFF","NAO"))};
        for(int i=0;i<5;i++){int y=top+30+i*24;a(left+10,y,left+270,y+20,0xFF34485D);b(p.q,rows[i],left+17,y+8,0xFFFFFF);}
        String device=mod_GenericAssistant.instance==null ? "" : mod_GenericAssistant.instance.pad.status();
        if(device.equals("No controller detected"))device=t("No controller detected","Nenhum controle detectado");
        b(p.q,t("Controller: ","Controle: ")+device,left+10,top+155,0xFFD36A);
        String axes=mod_GenericAssistant.instance==null ? "" : mod_GenericAssistant.instance.pad.axes();
        b(p.q,t("Axes: ","Eixos: ")+axes,left+10,top+168,0xBCC7D4);
        a(left+10,top+181,left+270,top+201,0xFF34485D);
        b(p.q,t("Scan controllers again","Buscar controles novamente"),left+17,top+187,0xFFFFFF);
        b(p.q,t("F8 / R-stick: settings | Back: X-Ray","F8 / analogico direito: opcoes | Back: X-Ray"),left+10,top+205,0xBCC7D4);
        b(p.q,t("ESC / B: close. Changes saved automatically.","ESC / B: fechar. Alteracoes salvas automaticamente."),left+10,top+218,0xBCC7D4);
    }
    protected void a(int x,int y,int button){
        if(button!=0)return;int left=(q-280)/2,top=(r-230)/2;
        if(x<left+10 || x>left+270)return;
        if(y>=top+181 && y<top+201 && mod_GenericAssistant.instance!=null){mod_GenericAssistant.instance.pad.reconnect();return;}
        for(int i=0;i<5;i++)if(y>=top+30+i*24 && y<top+50+i*24){
            if(i==0)mod_GenericAssistant.autoTool=!mod_GenericAssistant.autoTool;
            if(i==1)mod_GenericAssistant.autoJump=!mod_GenericAssistant.autoJump;
            if(i==2)mod_GenericAssistant.autoTreetap=!mod_GenericAssistant.autoTreetap;
            if(i==3)mod_GenericAssistant.refillHotbar=!mod_GenericAssistant.refillHotbar;
            if(i==4)mod_GenericAssistant.replaceBrokenTool=!mod_GenericAssistant.replaceBrokenTool;
            if(mod_GenericAssistant.instance!=null)mod_GenericAssistant.instance.saveSettings();return;
        }
    }
}
