public class paintLoop extends Interface implements Runnable{
    Thread thread;
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    paintLoop(){
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run() {
        while(true){
            repaint();
            System.out.println("Paint loop on");
            try {
                Thread.sleep(30);
            } catch (InterruptedException ex) {}
        }
    }
    
}
