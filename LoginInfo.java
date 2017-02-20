
public class LoginInfo {
	private String nickname;
	private boolean valid;
	
	public LoginInfo(){
		this.nickname = "";
		this.valid = true;
	}
	
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean isValid() {
		return valid;
	}

	public void makeInvalid() {
		this.valid = false;
	}

}
