package Model;

public class Account {
	private final Profile profile;
	private Status status;
	private final String password;
	private Statement statement;
	public Account(Profile profile, Status status, String password) {
		this.profile = profile;
		this.status = status;
		this.password = password;
		statement = Statement.UNLOGGED;
	}
	/**
	 * Funkcja u¿ywana przy rejestrowaniu nowego u¿ytkownika, 
	 * @param userName
	 * @param password
	 */
	public Account(String userName,  String password) {
		this.profile = new Profile(IdCreator.getIdentity(), userName);
		this.status = new Status(0);
		this.password = password;
		statement = Statement.UNLOGGED;
	}
	public boolean accessAccount(Integer id, String password) {
		boolean idBol = id.equals(this.profile.getId());
		boolean passBol = password.equals(this.password);
		return idBol & passBol;
	}
	public Status setStatus(Integer id, String password, Status status) {
		if(accessAccount(id, password)) {
			this.status = status;
			return this.status;
		}
		else
			return this.status;
	}
	public Status setStatus(Status status) {
		this.status = status;
		return this.status;
	}
	public Status setStatus(int i) {
		this.status = new Status(i);
		return this.status;
	}
	public Profile getProfile() {
		return this.profile;
	}
	public Status getStatus() {
		return this.status;
	}
	public int getStatus(int i) {
		return this.status.getStatus(i);
	}
	public final String getPassword() {
		return this.password;
	}
	public final Statement getStatement() {
		return this.statement;
	}
	public final Statement setStatement(Statement statement) {
		this.statement = statement;
		return this.statement;
	}
}
