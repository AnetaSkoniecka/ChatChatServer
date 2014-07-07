package Controler;
import java.io.IOException;
import java.net.ServerSocket;

import Model.DebugPrint;
import Model.Model;
import View.ServerView;

/** 
 * G�owna klasa programu, oczekuje na po��czenia klient�w i dla odebranmych po��cze� tworzy dla 
 * ka�dego klienta w�tek potomny, kt�ry go obs�u�y
 * */
public class ServerConsole extends Thread {

	final Integer port;
	final Model model;
	public ServerConsole(Integer port, Model model) {
		this.port = port;
		this.model = model;
	}
	/**
	 * Czeka na ServerSockecie i oczekuje na zg�aszaj�cych si� klient�w.
	 * Gdy nast�pi po��czenie to tworzony jest nowy socket i sterowanie/obs�uga tego nowego klienta przekazywana jest do nowego
	 * w�tku ServerSocketProtocol.
	 */
	public void run() {
		DebugPrint.print("ServerConsole started!");
        boolean listening = true;
        /** 
         * Tworzy ServerSocket, kt�ry oczekuje na po��czenia z innymi klientami. 
         * Gdy jaki� klient si� po��czy, to przekazuje to po��czenie w postaci socketu do ServerSocketProtocol, 
         * kt�ry decyduje jak go obs�u�y�
         * */
        try (ServerSocket serverSocket = new ServerSocket(port)) {
        	DebugPrint.print("serverSocket dziala");
            while (listening) {
            	DebugPrint.print("listening");
                new ServerSocketProtocol(serverSocket.accept(), model).start();DebugPrint.print("socket dziala");
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port);
            System.exit(-1);
        }
    }
}
