package Controler;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Queue;

import CommunicatePackages.LogingPackage;
import CommunicatePackages.MessagePackage;
import CommunicatePackages.MyPackage;
import CommunicatePackages.StatusListPackage;
import CommunicatePackages.MyPackage.TypePackage;
import CommunicatePackages.RegisterPackage;
import CommunicatePackages.StatusPackage;
import Exceptions.CloseException;
import Model.Account;
import Model.DebugPrint;
import Model.Message;
import Model.Model;
import Model.Profile;
import Model.Statement;

/**
 * Klasa odbiera wys³any przez klienta pakiet i w zale¿noœci czy jest to zapytanie o logowanie czy o rejestracje
 * to jest to odpowiednio obs³ugiwane.
 * Klasa mo¿e rzucaæ wyj¹tek niezgodnoœci z protoko³em lub zamykania aplikacji.
 * @author necia
 *
 */
public class ServerSocketProtocol extends Thread {
	private OutputStream out;
	private InputStream in;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private final Socket socket;
	private final Model model;
	private Boolean isLogged;
	/**
	 * Dodatkowa zmienna przechowuj¹ca obecnie zalogowane konto. Wykorzystywana jest w przypadku terminate u Clienta
	 * i braku wylogowania. Wtedy wylogowanie nastêpuje bezwarunkowo w bloku finally na koniec protoko³u.
	 */
	private Integer currentConnected = null;
	/**
	 * Klasa obs³uguje tylko jednego klienta! 
	 * Konstruktor tworzy Strumienie i przypisuje Socket. 
	 * Wywo³uje funkcje getFirstPackage, która rozga³êzia dalej ca³y protoku³ obs³ugi pakietów. Gdy siê skoñczy, 
	 * strumienie i socket zostaj¹ zamkniête i obiekt oraz po³¹czenie przestaj¹ istnieæ.
	 * W konstruktorze zamkniêcie strumieni i socketa nastêpuje tylko w momencie wyst¹pienia b³êdu.
	 * @param socket
	 * @param model
	 */
	public ServerSocketProtocol(Socket socket, Model model) {
		super();
		DebugPrint.print("nowy protocol");
		this.socket = socket;
		this.model = model; 
		isLogged = false;
	}
	/**
	 * Dla danego socketa, powi¹zuje Out i In Streamy. 
	 * Wywo³ywana jest w nieskoñczonej pêtli getPackage, która obs³uguje pakiety. 
	 * Pêtla zostaje zamkniêta w wyniku rzucenia wyj¹tku CloseException, która powoduje zamkniêcie Streamów, 
	 * Socketa oraz tego w¹tku.
	 */
	public void run() {
		try {
        	out = socket.getOutputStream();
			in = socket.getInputStream();
			input = new ObjectInputStream(in);
			output = new ObjectOutputStream(out);
        	 
        } catch (IOException e) {
           // e.printStackTrace();
            try {
            	DebugPrint.print("ups serversocketprotocol");
            	input.close();
            	output.close();
				socket.close();
			} catch (IOException e1) {
				DebugPrint.print("io exception");
			} catch (Exception e2) {
				DebugPrint.print("zamknieto socketa, awaryjnie ale przewidzianie");
			}
        } catch (Exception e1) {
        	DebugPrint.print("lapie to co sie da");
        	return;
        }
		try {
			while(true) {
				getPackage();
			}
		} catch (CloseException e) {
        	try {
        		DebugPrint.print("zamykanie polaczenia");
				input.close();
	        	output.close();
				socket.close();
				DebugPrint.print("Zamaknieto socketa");
				this.interrupt();
			} catch (IOException e1) {}
		} catch (ClassNotFoundException e) {
			// do nothing
		} catch (IOException e) {
			// do nothing
		} finally {
			if(currentConnected != null)
				model.terminateUnLoging(currentConnected);
			DebugPrint.print("koniec polaczenia klienta z serwerem");
		}
	}
	/**
	 * W momencie wys³ania przez klienta RegisterPackage, server przyjmuje ten pakiet i na jego podstawie tworzy nowe konto, 
	 * i na podstawie utworzonego konta tworzy pakiet zwrotny.
	 * W momencie otrzymania pakietu Loging, server loguje konto lub wylogowywuje.
	 * -Jeœli jakieœ konto jest ju¿ zalogowane to nie mo¿na siê ponownie wylogowaæ
	 * -Jesli konto jest wylogowane to nie mo¿na ponownie kogoœ wylogowaæ 
	 * -Jesli has³o siê nie zgadza to nie otrzymuje siê dostêpu do konta. 
	 * W momencie otrzymania pakietu closing, po³¹czenie musi zostaæ zakonczone.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void getPackage() throws CloseException, ClassNotFoundException, IOException {
		DebugPrint.print("czekam na pierwszy pakiet");
		MyPackage pack = (MyPackage) input.readObject();
		DebugPrint.print("odebrano");
		if (pack.getTypePackage() == TypePackage.REGISTER) {
			DebugPrint.print("odebrano pakiet register");
			RegisterPackage returnPackage = model.registerNewAccount((RegisterPackage) pack);
			
			System.out.println("Utworzono pakiet rejestracji: "+returnPackage.getSenderProfile().getId().toString()
					+" " + returnPackage.getSenderProfile().getUserName() + " " + returnPackage.getPassword().toString());
			output.writeObject(returnPackage);
			output.flush();
		}
		else if (pack.getTypePackage() == TypePackage.LOGING) {
			DebugPrint.print("odebrano pakiet loging");
			LogingPackage returnPackage = model.loginAccount((LogingPackage) pack);
			isLogged(returnPackage.getStatement() == Statement.LOGGED);
			if(returnPackage.getStatement() == Statement.LOGGED)
				currentConnected = returnPackage.getSenderId();
			else
				currentConnected = null;
			output.writeObject(returnPackage);
			output.flush();
			DebugPrint.print("tu1");
			if(returnPackage.getStatement() == Statement.UNLOGGED)
				DebugPrint.print("wylogowano lub zhe haslo");
		}
		else if ( pack.getTypePackage() == TypePackage.CLOSING ) {
			DebugPrint.print("odebrano pakiet closing");
			throw new CloseException();	
		}
		else if (pack.getTypePackage() == TypePackage.TAKEMESSAGES ) {
			LogingPackage logPack = (LogingPackage) pack;
			Queue<Message> messagesQueue = model.getMessages(logPack.getSenderProfile(), logPack.getPassword());
			Message message = null;
			// tu mozna zrobic osobny w¹tek i wtedy sprawdzalby to isLogged
			/**
			 * Jeœli wyst¹pi b³¹d wysy³ania wiadomoœci to nie mo¿e przecie¿ zostaæ utracona.
			 * W przypadku b³êdu zostaje z powrotem zapisana do buforu.
			 */
			while( isLogged(null) && ((message = messagesQueue.poll()) != null)) {
				try {
					output.writeObject(new MessagePackage(message.getSenderProfile(), message.getReceiverProfile(), 
							message, null));
					output.flush();
				} catch (IOException e3) {
					// nie uda³o mi sie przetestowac czy to dziala
					DebugPrint.print("Przerwano wysylanie wiadomoœci. Cofam j¹ do bufora");
					model.putMessage(message.getReceiverProfile().getId(), message);
				}

			}
		}
		else if( pack.getTypePackage() == TypePackage.STATUS) {
			DebugPrint.print("odebrano pakiet status");
			StatusPackage statPack = (StatusPackage) pack;
			model.setStatus(statPack.getSenderId(), statPack.getPassword(), statPack.getStatus());
		}
		else if( pack.getTypePackage() == TypePackage.STATUSLIST ) {
			DebugPrint.print("odebrano pakiet liststaus");
			StatusListPackage statListPack = (StatusListPackage) pack;
			StatusListPackage retpack = new StatusListPackage
							(statListPack.getSenderProfile(), model.getAllStatus(statListPack.getMap()));
			output.writeObject(retpack);
			output.flush();
		}
		else if (pack.getTypePackage() == TypePackage.MESSAGE ) {
			MessagePackage messPack = (MessagePackage) pack;
			model.putMessage(messPack.getReceiverId(), messPack.getMessage());
		}
		else {
			System.out.println("Bl¹d w getFirstPackage");
			// przygotuj wyj¹tek do rzucenia - wyj¹tek który og³osi niezgodnoœæ z protoko³em
		}
	}
	
	/**
	 * Funkcja synchroniczna, która powinna siê wykonaæ w ca³oœci bez przerw.
	 * Gdy podamy jej parametr Boolean to taki zostaje ustawiony na zmiennej.
	 * Gdy argumentem jest null to poprostu zwraca obecn¹ wartoœæ.
	 * @param bool
	 * @return
	 */
	private synchronized Boolean isLogged(Boolean bool) {
		if(bool == null)
			return isLogged;
		else {
			isLogged = bool;
			return isLogged;
		}		
	}
}
