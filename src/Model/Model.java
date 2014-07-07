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
 * Model zarz¹dza wszystkimi danymi przechowywanymi na serwerze
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
	 * Funkcja wywo³ywana przez protoku³ serwera.
	 * Otrzymuj¹æ pakiet od klienta typu RegisterPackage, nale¿y z niego wyci¹gn¹æ dane, 
	 * stworzyæ konto, dodaæ do bufora i zwróciæ Pakiet zwrotny do klienta.
	 * Zarejestrowanym kontom ustawia siê status wylogowany.
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
	 * Zalogowanie nastêpuje kiedy konto o podanym id istnieje, has³o do konta siê zgadza. 
	 * Nie mo¿na siê zalogowaæ, gdy ktoœ ju¿ jest zalogowany na konto. 
	 * Nie mo¿na siê wylogowaæ, gdy konto jest wylogowane.
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
	 * W przypadku usuniêcia po³¹czenia z b³êdem i zalogowanego konta, nale¿y na koniec protoko³u wylogowaæ konto.
	 * Istniej¹cemu kontu ustawia sie status wylogowany.
	 * @param id
	 */
	public void terminateUnLoging(Integer id){
		Account acc = serverBuffer.checkIfAccountExist(id);
		if(acc != null){
			serverBuffer.setAccountStatement(id, Statement.UNLOGGED);
			acc.setStatus(0);
			DebugPrint.print("wyj¹tkowe wylogowanie");
		}		
	}
	/**
	 * Na podstawie danych loguj¹cych œci¹ga liste wiadomoœci jakie czekaj¹ na danym koncie.
	 * Nie mo¿na œciagaæ wiadomoœci jeœli, konto nie istnieje, jest niezalogowane, b³êdne has³o, wiadomoœci nie ma.
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
	 * Dodanie wiadomoœci do bufera.
	 * @param id
	 * @param message
	 */
	public void putMessage(Integer id, Message message) {
		Account acc = serverBuffer.checkIfAccountExist(id);
		if(acc != null) 
			serverBuffer.putMessage(acc, message);
	}
	/**
	 * Funkcja na podstawie listy numerów identyfikacyjnych zwraca mape id powi¹zanych z odpowiadaj¹cymi im statusami.
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
	 * Sprawdza czy dane konto istnieje. Jeœli tak to zwraca ustawiony status.
	 * Jeœli konto nie istnieje to zwraca status ¿e konto jest niezarejestrowane/nie istenieje
	 * @param id
	 * @param status
	 */
	public synchronized Status setStatus(Integer id, String password, Status status) {
		return serverBuffer.setStatus(id, password, status);
	}
	/**
	 * Iteruj¹c po elementach mapy pobierany jest z bufera Status dla ka¿dego konta, uaktualniana jest
	 * wartoœæ dla kazdego klucza i mapa jest zwracana.
	 * Dla id które s¹ w mapie ale s¹ niezarejestrowane, przypisywana jest wartosc niezarejestrowania.
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
