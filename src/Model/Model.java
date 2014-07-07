package Model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import CommunicatePackages.LogingPackage;
import CommunicatePackages.RegisterPackage;




/**
 * Model zarz�dza wszystkimi danymi przechowywanymi na serwerze
 * @author necia
 *
 */
public class Model {
	private final ServerBuffer serverBuffer;
	/** 
	 * Tworzony jest bufor servera
	 */
	public Model() {
		serverBuffer = new ServerBuffer();
	}
	/**
	 * Funkcja wywo�ywana przez protoku� serwera.
	 * Otrzymuj�� pakiet od klienta typu RegisterPackage, nale�y z niego wyci�gn�� dane, 
	 * stworzy� konto, doda� do bufora i zwr�ci� Pakiet zwrotny do klienta.
	 * Zarejestrowanym kontom ustawia si� status wylogowany.
	 * 
	 * @param regPackage
	 * @return
	 */
	public RegisterPackage registerNewAccount(RegisterPackage regPackage) {
		Account account = new Account( regPackage.getSenderProfile().getUserName(), regPackage.getPassword() );
		serverBuffer.addAccount(account);
		account.setStatus(0);
		return new RegisterPackage( account.getProfile(), account.getPassword() );
	}
	/**
	 * Zalogowanie nast�puje kiedy konto o podanym id istnieje, has�o do konta si� zgadza. 
	 * Nie mo�na si� zalogowa�, gdy kto� ju� jest zalogowany na konto. 
	 * Nie mo�na si� wylogowa�, gdy konto jest wylogowane.
	 * Przy poprawnym wylogowaniu ustawiany jest status dla tego konta na wylogowany.
	 * @param logPackage
	 * @return
	 */
	public LogingPackage loginAccount(LogingPackage logPackage) {
		Account acc = serverBuffer.checkIfAccountExist(logPackage.getSenderId());
		if(acc != null && acc.accessAccount(logPackage.getSenderId(), logPackage.getPassword()) 
				&& logPackage.getStatement() != serverBuffer.checkAccountStatement(logPackage.getSenderId())
				) {
			DebugPrint.print("Znaleziono konto i haslo sie zgadza");
			serverBuffer.setAccountStatement(logPackage.getSenderId(), logPackage.getStatement());
			if(logPackage.getStatement() == Statement.UNLOGGED)
				serverBuffer.setStatus(logPackage.getSenderId(),logPackage.getPassword(), new Status(0));
			return new LogingPackage(acc.getProfile(), logPackage.getStatement(), acc.getPassword());
		}
		else {
			return new LogingPackage(logPackage.getSenderProfile(), Statement.UNLOGGED, logPackage.getPassword());
		}
	}
	/**
	 * W przypadku usuni�cia po��czenia z b��dem i zalogowanego konta, nale�y na koniec protoko�u wylogowa� konto.
	 * Istniej�cemu kontu ustawia sie status wylogowany.
	 * @param id
	 */
	public void terminateUnLoging(Integer id){
		Account acc = serverBuffer.checkIfAccountExist(id);
		if(acc != null){
			serverBuffer.setAccountStatement(id, Statement.UNLOGGED);
			acc.setStatus(0);
			DebugPrint.print("wyj�tkowe wylogowanie");
		}		
	}
	/**
	 * Na podstawie danych loguj�cych �ci�ga liste wiadomo�ci jakie czekaj� na danym koncie.
	 * Nie mo�na �ciaga� wiadomo�ci je�li, konto nie istnieje, jest niezalogowane, b��dne has�o, wiadomo�ci nie ma.
	 * @param profile
	 * @param password
	 * @return
	 */
	public Queue<Message> getMessages(Profile profile, String password) {
		Account acc = serverBuffer.checkIfAccountExist(profile.getId());
		if( acc != null && acc.accessAccount(profile.getId(), password) && acc.getStatement() == Statement.LOGGED ) {
			return serverBuffer.getMessages(acc);
		}
		return new LinkedList<>();
	}
	/**
	 * Dodanie wiadomo�ci do bufera.
	 * @param id
	 * @param message
	 */
	public void putMessage(Integer id, Message message) {
		Account acc = serverBuffer.checkIfAccountExist(id);
		if(acc != null) 
			serverBuffer.putMessage(acc, message);
	}
	/**
	 * Funkcja na podstawie listy numer�w identyfikacyjnych zwraca mape id powi�zanych z odpowiadaj�cymi im statusami.
	 * @param list
	 * @return
	 */
	public Map<Integer, Status> getAllStatus(Map<Integer, Status> map) {
		Status status;
		for (Integer id : map.keySet()) {
			map.put(id, serverBuffer.getStatus(id));
		}
		return map;
	}
	/**
	 * Sprawdza czy dane konto istnieje. Je�li tak to zwraca ustawiony status.
	 * Je�li konto nie istnieje to zwraca status �e konto jest niezarejestrowane/nie istenieje
	 * @param id
	 * @param status
	 */
	public synchronized Status setStatus(Integer id, String password, Status status) {
		return serverBuffer.setStatus(id, password, status);
	}
	/**
	 * Iteruj�c po elementach mapy pobierany jest z bufera Status dla ka�dego konta, uaktualniana jest
	 * warto�� dla kazdego klucza i mapa jest zwracana.
	 * Dla id kt�re s� w mapie ale s� niezarejestrowane, przypisywana jest wartosc niezarejestrowania.
	 * @param mapStatus
	 * @return
	 */
	public Map<Integer, Status> getMapStatus(Map<Integer, Status> mapStatus) {
		for (Map.Entry<Integer, Status> entry : mapStatus.entrySet()) {
		    System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
		    mapStatus.put( entry.getKey(), serverBuffer.getStatus(entry.getKey()) );
		}
		return mapStatus;
	}
}
