package CommunicatePackages;

import Model.Profile;
/**
 * Paczka register jest wysy�ana w przypadku rejestracji z profilem bez ustalonego id, i has�em kt�re chcemy 
 * by by�o przypisane do nowoutworzonego konta
 * @author necia
 *
 */
public class RegisterPackage extends MyPackage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7816710563603167295L;
	private final String password;
	public RegisterPackage(Profile sender, String password) {
		super(MyPackage.TypePackage.REGISTER, sender);
		this.password = password;
	}
	public String getPassword() {
		return password;
	}

}
