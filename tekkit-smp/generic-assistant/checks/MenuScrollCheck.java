package com.gabrielgollo.genericassistant.client;

public final class MenuScrollCheck {
    static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        MenuScroll scroll=new MenuScroll();
        require(scroll.poll(.1f,1000)==0,"Neutral drift");
        require(scroll.poll(.5f,1000)==1,"Down immediately");
        require(scroll.poll(.5f,1189)==0,"Throttle repeated frames");
        require(scroll.poll(.5f,1190)==1,"Held-stick repetition");
        require(scroll.poll(-.5f,1191)==-1,"Reverse immediately");
        require(scroll.poll(0f,1192)==0,"Neutral stops");
        require(scroll.poll(.5f,1193)==1,"Re-engage immediately");
        scroll.reset();require(scroll.poll(1f,1194)==1,"New menu starts fresh");
        require(scroll.poll(1f,1273)==0&&scroll.poll(1f,1274)==1,"Full tilt faster repeat");
        require(scroll.poll(Float.NaN,1275)==0,"Invalid input");
        System.out.println("PASS: right-stick menu scroll direction, drift, repeat timing, reversal, reset and tilt speed");
    }
}
