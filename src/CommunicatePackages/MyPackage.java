package CommunicatePackages;

import java.io.Serializable;

import Model.Profile;

/**
 * Klasa bazowa MyPackage, po kt�rej b�d� dziedziczy� kolejne paczki
 * @author necia
 */
public abstract class MyPackage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2351544176123021500L;
	
	/**
	 * Pozwala, okre�la� typ paczek do rzutowania oraz do obierania strategi obslugi paczki
	 */
	public enum TypePackage {
		MESSAGE, ACCESSING, REGISTER, STATUSLIST, LOGING, CLOSING, TAKEMESSAGES, STATUS
	}
	
	protected final TypePackage typePackage;
	protected final Profile sender;

	public MyPackage(MyPackage.TypePackage typePackage, Profile sender) {
		this.typePackage = typePackage;
		this.sender = sender;
	}
	public Profile getSenderProfile() {
		return sender;
	}
	public Integer getSenderId() {
		return sender.getId();
	}
	public TypePackage getTypePackage() {
		return typePackage;
	}
	
}