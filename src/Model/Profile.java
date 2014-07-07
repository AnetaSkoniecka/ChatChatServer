package Model;

import java.io.Serializable;

public class Profile implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3508028930177977977L;
	private final Integer id;
	private final String userName;
	public Profile(Integer id, String userName) {
		this.id = id;
		this.userName = userName;
	}
	public Profile(Profile profile, Integer id) {
		this.id = id;
		this.userName = profile.getUserName();
	}
	public String getUserName() {
		return userName;
	}
	public Integer getId() {
		return id;
	}
	public Object[] getProfile() {
		Object[] ret = {id,userName};
		return ret;
	}
}
