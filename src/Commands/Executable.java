package Commands;

/**
 * Interfejs do wykonywania komend.
 * Jest przekazywany do kontrolera jako komenda i taki interfejs jest wykonywany za pomoc� funkcji execute.
 * @author necia
 *
 */
public interface Executable {
	public void execute();
}
