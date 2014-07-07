import Controler.ServerControler;
import Model.Model;
import View.ServerView;
public class Server  {
	/** 
	 * Utworzenie programu, ustawienie portu i nazwy hosta na kt�rm dzia�a aplikacja, uruchomienie konsoli 
	 * */
	public static void main(String[] args) {
		Model model = new Model();
		ServerView view = new ServerView();
		ServerControler controler = new ServerControler(view, model);
		controler.start();
		//ServerConsole console = new ServerConsole(view, model);
//		console.start();
	}

}
