
public class Quit {
	private boolean quit = false;
	
	public synchronized void quit(){
		quit = true;
	}
	
	public synchronized boolean userHasQuit(){
		return this.quit;
	}
}
