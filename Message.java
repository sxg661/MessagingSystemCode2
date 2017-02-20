public class Message {

  private final String sender;
  private final String text;
  private final boolean quit;

  Message(String sender, String text, boolean quit) {
    this.sender = sender;
    this.text = text;
    this.quit = quit;
  }

  public String getSender() {
    return sender;
  }

  public String getText() {
    return text;
  }
  
  public boolean isQuitMessage() {
	  return quit;
  }

  public String toString() {
    return "From " + sender + ": " + text;
  }
}
