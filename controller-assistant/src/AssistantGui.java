final class AssistantGui extends vp {
    private String t(String en,String pt){return mod_ControllerAssistant.text(p,en,pt);}
    public void a(int mx,int my,float tick){
        super.a();int left=(q-280)/2,top=(r-230)/2;
        a(left,top,left+280,top+230,0xEE202530);
        b(p.q,"Controller Assistant",left+10,top+10,0xFFFFFF);
        String[] rows={t("Auto tool: ","Ferramenta automatica: ")+(mod_ControllerAssistant.autoTool?t("ON","SIM"):t("OFF","NAO")),t("Auto jump: ","Pulo automatico: ")+(mod_ControllerAssistant.autoJump?t("ON","SIM"):t("OFF","NAO")),t("Auto treetap: ","Treetap automatico: ")+(mod_ControllerAssistant.autoTreetap?t("ON","SIM"):t("OFF","NAO"))};
        for(int i=0;i<3;i++){int y=top+35+i*30;a(left+10,y,left+270,y+24,0xFF34485D);b(p.q,rows[i],left+17,y+8,0xFFFFFF);}
        String device=mod_ControllerAssistant.instance==null ? "" : mod_ControllerAssistant.instance.pad.status();
        if(device.equals("No controller detected"))device=t("No controller detected","Nenhum controle detectado");
        b(p.q,t("Controller: ","Controle: ")+device,left+10,top+131,0xFFD36A);
        String axes=mod_ControllerAssistant.instance==null ? "" : mod_ControllerAssistant.instance.pad.axes();
        b(p.q,t("Axes: ","Eixos: ")+axes,left+10,top+146,0xBCC7D4);
        a(left+10,top+163,left+270,top+186,0xFF34485D);
        b(p.q,t("Scan controllers again","Buscar controles novamente"),left+17,top+171,0xFFFFFF);
        b(p.q,t("F8 / R-stick: settings | Back: X-Ray","F8 / analogico direito: opcoes | Back: X-Ray"),left+10,top+197,0xBCC7D4);
        b(p.q,t("ESC / B: close. Changes saved automatically.","ESC / B: fechar. Alteracoes salvas automaticamente."),left+10,top+213,0xBCC7D4);
    }
    protected void a(int x,int y,int button){
        if(button!=0)return;int left=(q-280)/2,top=(r-230)/2;
        if(x<left+10 || x>left+270)return;
        if(y>=top+163 && y<top+186 && mod_ControllerAssistant.instance!=null){mod_ControllerAssistant.instance.pad.reconnect();return;}
        for(int i=0;i<3;i++)if(y>=top+35+i*30 && y<top+59+i*30){
            if(i==0)mod_ControllerAssistant.autoTool=!mod_ControllerAssistant.autoTool;
            if(i==1)mod_ControllerAssistant.autoJump=!mod_ControllerAssistant.autoJump;
            if(i==2)mod_ControllerAssistant.autoTreetap=!mod_ControllerAssistant.autoTreetap;
            if(mod_ControllerAssistant.instance!=null)mod_ControllerAssistant.instance.saveSettings();return;
        }
    }
}
