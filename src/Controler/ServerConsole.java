package Controler;
import java.io.IOException;
import java.net.ServerSocket;

import Model.DebugPrint;
import Model.Model;
import View.ServerView;

/** 
 * G³owna klasa programu, oczekuje na po³¹czenia klientów i dla odebranmych po³¹czeñ tworzy dla 
 * ka¿dego klienta w¹tek potomny, który go obs³u¿y
 * */
public class ServerConsole extends Thread {

	final Integer port;
	final Model model;
	public ServerConsole(Integer port, Model model) {
		this.port = port;
		this.model = model;
	}
	/**
	 * Czeka na ServerSockecie i oczekuje na zg³aszaj¹cych siê klientów.
	 * Gdy nast¹pi po³¹czenie to tworzony jest nowy socket i sterowanie/obs³uga tego nowego klienta przekazywana jest do nowego
	 * w¹tku ServerSocketProtocol.
	 */
	public void run() {
		DebugPrint.print("ServerConsole started!");
        boolean listening = true;
        /** 
         * Tworzy ServerSocket, który oczekuje na po³¹czenia z innymi klientami. 
         * Gdy jakiœ klient siê po³¹czy, to przekazuje to po³¹czenie w postaci socketu do ServerSocketProtocol, 
         * który decyduje jak go obs³u¿yæ
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
