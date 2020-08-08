
public class Thread2 extends Interface implements Runnable{
    Thread thread;
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    Thread2(){
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        Scrabble.onEnter();
    }
    
}
