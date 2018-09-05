package show.we.socket.domain;

public class ClientInfo {
	
	private String token;
	
	private Long userId;
	
	private Long roomId;
	
	private String clientId;
	//设备标示
	private String s;
	
	public ClientInfo(String clientId,String token, Long roomId, UserInfo userInfo) {
		super();
		this.clientId = clientId;
		this.token = token;
		this.roomId = roomId;
		this.s = userInfo.getSign().get("s").toString();
		this.userId = userInfo.getUserId();
	}
	public ClientInfo(String clientId,String token, Long roomId) {
		super();
		this.clientId = clientId;
		this.token = token;
		this.roomId = roomId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	public boolean isStar(){
		if(userId != null && userId.equals(roomId))
			return true;
		else
			return false;
	}
	
}
