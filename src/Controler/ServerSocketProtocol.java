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
 * Klasa odbiera wys�any przez klienta pakiet i w zale�no�ci czy jest to zapytanie o logowanie czy o rejestracje
 * to jest to odpowiednio obs�ugiwane.
 * Klasa mo�e rzuca� wyj�tek niezgodno�ci z protoko�em lub zamykania aplikacji.
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
	 * Dodatkowa zmienna przechowuj�ca obecnie zalogowane konto. Wykorzystywana jest w przypadku terminate u Clienta
	 * i braku wylogowania. Wtedy wylogowanie nast�puje bezwarunkowo w bloku finally na koniec protoko�u.
	 */
	private Integer currentConnected = null;
	/**
	 * Klasa obs�uguje tylko jednego klienta! 
	 * Konstruktor tworzy Strumienie i przypisuje Socket. 
	 * Wywo�uje funkcje getFirstPackage, kt�ra rozga��zia dalej ca�y protoku� obs�ugi pakiet�w. Gdy si� sko�czy, 
	 * strumienie i socket zostaj� zamkni�te i obiekt oraz po��czenie przestaj� istnie�.
	 * W konstruktorze zamkni�cie strumieni i socketa nast�puje tylko w momencie wyst�pienia b��du.
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
	 * Dla danego socketa, powi�zuje Out i In Streamy. 
	 * Wywo�ywana jest w niesko�czonej p�tli getPackage, kt�ra obs�uguje pakiety. 
	 * P�tla zostaje zamkni�ta w wyniku rzucenia wyj�tku CloseException, kt�ra powoduje zamkni�cie Stream�w, 
	 * Socketa oraz tego w�tku.
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
	 * W momencie wys�ania przez klienta RegisterPackage, server przyjmuje ten pakiet i na jego podstawie tworzy nowe konto, 
	 * i na podstawie utworzonego konta tworzy pakiet zwrotny.
	 * W momencie otrzymania pakietu Loging, server loguje konto lub wylogowywuje.
	 * -Je�li jakie� konto jest ju� zalogowane to nie mo�na si� ponownie wylogowa�
	 * -Jesli konto jest wylogowane to nie mo�na ponownie kogo� wylogowa� 
	 * -Jesli has�o si� nie zgadza to nie otrzymuje si� dost�pu do konta. 
	 * W momencie otrzymania pakietu closing, po��czenie musi zosta� zakonczone.
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
			// tu mozna zrobic osobny w�tek i wtedy sprawdzalby to isLogged
			/**
			 * Je�li wyst�pi b��d wysy�ania wiadomo�ci to nie mo�e przecie� zosta� utracona.
			 * W przypadku b��du zostaje z powrotem zapisana do buforu.
			 */
			while( isLogged(null) && ((message = messagesQueue.poll()) != null)) {
				try {
					output.writeObject(new MessagePackage(message.getSenderProfile(), message.getReceiverProfile(), 
							message, null));
					output.flush();
				} catch (IOException e3) {
					// nie uda�o mi sie przetestowac czy to dziala
					DebugPrint.print("Przerwano wysylanie wiadomo�ci. Cofam j� do bufora");
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
			System.out.println("Bl�d w getFirstPackage");
			// przygotuj wyj�tek do rzucenia - wyj�tek kt�ry og�osi niezgodno�� z protoko�em
		}
	}
	
	/**
	 * Funkcja synchroniczna, kt�ra powinna si� wykona� w ca�o�ci bez przerw.
	 * Gdy podamy jej parametr Boolean to taki zostaje ustawiony na zmiennej.
	 * Gdy argumentem jest null to poprostu zwraca obecn� warto��.
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
