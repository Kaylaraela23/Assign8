package packy;

import java.io.*;
import java.util.HashMap;
import java.util.Collection;

public class Inventory {
	private HashMap<Integer, Textbook> textbooks = new HashMap<>();

	public boolean addTextbook(Textbook textbook) {
		if (textbooks.containsKey(textbook.getSku()))
			return false;
		textbooks.put(textbook.getSku(), textbook);
		return true;
	}

	public boolean removeTextbook(int sku) {
		return textbooks.remove(sku) != null;
	}

	public Textbook findTextbook(int sku) {
		return textbooks.get(sku);
	}

	public Collection<Textbook> getAllTextbooks() {
		return textbooks.values();
	}

	public void saveToFile() throws IOException {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("inventory.ser"))) {
			out.writeObject(textbooks);
		}
	}

	@SuppressWarnings("unchecked")
	public void loadFromFile() throws IOException, ClassNotFoundException {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("inventory.ser"))) {
			textbooks = (HashMap<Integer, Textbook>) in.readObject();
		} catch (FileNotFoundException e) {
			// Start with an empty inventory if file does not exist
		}
	}
}
