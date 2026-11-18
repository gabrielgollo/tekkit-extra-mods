public final class FrameMotionCheck {
    static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        for(int fps:new int[]{20,30,60,120,144,240}){
            FrameMotion motion=new FrameMotion();require(motion.step(0)==0f,"First frame must not jump");
            float yaw=0f;int cursor=0,frames=0;
            for(int frame=1;frame<=fps;frame++){
                float scale=motion.step(Math.round(frame*1000000000d/fps));
                float delta=5f*scale;yaw+=delta;if(delta>0)frames++;
                cursor+=motion.pixelsX(14f*scale);
            }
            require(Math.abs(yaw-100f)<.001f,"Frame-rate dependent turn speed at "+fps);
            require(Math.abs(cursor-280)<=1,"Frame-rate dependent cursor speed at "+fps);
            require(frames==fps,"Camera skipped rendering frames at "+fps);
        }
        FrameMotion motion=new FrameMotion();motion.step(0);
        require(motion.step(5000000000L)==1f,"Stall causes jump");
        motion.reset();require(motion.step(9000000000L)==0f,"Resume causes jump");
        require(motion.pixelsX(.2f)==0,"Subpixel rounding");
        require(motion.pixelsX(.8f)==1,"Subpixel movement lost");
        motion.pixelsY(.8f);require(motion.pixelsY(0f)==0&&motion.pixelsY(.2f)==0,"Neutral leaves residual drift");
        motion.reset();int total=0;for(int i=0;i<10;i++)total+=motion.pixelsX(-.25f);
        require(total==-2,"Negative subpixel movement");
        System.out.println("PASS: per-frame motion at 20/30/60/120/144/240 FPS, equal sensitivity, cursor fractions, neutral/reset and stall guards");
    }
}
