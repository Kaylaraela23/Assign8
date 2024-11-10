package packy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class InventoryApp extends JFrame {
	private Inventory inventory = new Inventory();
	private JTextField skuField, titleField, priceField, quantityField;
	private JTable inventoryTable;
	private DefaultTableModel tableModel;

	public InventoryApp() {
		setTitle("Campus Textbook Store Inventory");
		setSize(600, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		try {
			inventory.loadFromFile();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Starting with an empty inventory.");
		}

		JPanel inputPanel = new JPanel(new GridLayout(2, 4));
		inputPanel.add(new JLabel("SKU:"));
		skuField = new JTextField();
		inputPanel.add(skuField);
		inputPanel.add(new JLabel("Title:"));
		titleField = new JTextField();
		inputPanel.add(titleField);
		inputPanel.add(new JLabel("Price:"));
		priceField = new JTextField();
		inputPanel.add(priceField);
		inputPanel.add(new JLabel("Quantity:"));
		quantityField = new JTextField();
		inputPanel.add(quantityField);

		String[] columnNames = { "SKU", "Title", "Price", "Quantity" };
		tableModel = new DefaultTableModel(columnNames, 0);
		inventoryTable = new JTable(tableModel);
		loadTableData();
		JScrollPane scrollPane = new JScrollPane(inventoryTable);

		JPanel buttonPanel = new JPanel();
		JButton addButton = new JButton("Add Textbook");
		JButton removeButton = new JButton("Remove Textbook");
		JButton displayButton = new JButton("Display Textbook");
		JButton displayAllButton = new JButton("Display All Inventory");
		JButton saveExitButton = new JButton("Save & Exit");

		addButton.addActionListener(new AddButtonListener());
		removeButton.addActionListener(new RemoveButtonListener());
		displayButton.addActionListener(new DisplayButtonListener());
		displayAllButton.addActionListener(e -> loadTableData());
		saveExitButton.addActionListener(e -> saveAndExit());

		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		buttonPanel.add(displayButton);
		buttonPanel.add(displayAllButton);
		buttonPanel.add(saveExitButton);

		add(inputPanel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void loadTableData() {
		tableModel.setRowCount(0);
		for (Textbook textbook : inventory.getAllTextbooks()) {
			tableModel.addRow(new Object[] { textbook.getSku(), textbook.getTitle(), textbook.getPrice(),
					textbook.getQuantity() });
		}
	}

	private void saveAndExit() {
		try {
			inventory.saveToFile();
			JOptionPane.showMessageDialog(this, "Inventory saved. Exiting.");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error saving inventory.");
		}
		System.exit(0);
	}

	private class AddButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int sku = Integer.parseInt(skuField.getText());
				String title = titleField.getText();
				double price = Double.parseDouble(priceField.getText());
				int quantity = Integer.parseInt(quantityField.getText());

				if (sku <= 0)
					throw new NumberFormatException("SKU must be a positive integer.");
				if (title.isEmpty())
					throw new IllegalArgumentException("Title cannot be empty.");
				if (price <= 0)
					throw new IllegalArgumentException("Price must be greater than zero.");
				if (quantity < 0)
					throw new IllegalArgumentException("Quantity cannot be negative.");

				Textbook textbook = new Textbook(sku, title, price, quantity);
				if (inventory.addTextbook(textbook)) {
					loadTableData();
					JOptionPane.showMessageDialog(InventoryApp.this, "Textbook added.");
				} else {
					JOptionPane.showMessageDialog(InventoryApp.this, "SKU already exists.");
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(InventoryApp.this,
						"Invalid input: SKU, price, and quantity must be numbers.");
			} catch (IllegalArgumentException ex) {
				JOptionPane.showMessageDialog(InventoryApp.this, ex.getMessage());
			}
		}
	}

	private class RemoveButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int sku = Integer.parseInt(skuField.getText());
				if (inventory.removeTextbook(sku)) {
					loadTableData();
					JOptionPane.showMessageDialog(InventoryApp.this, "Textbook removed.");
				} else {
					JOptionPane.showMessageDialog(InventoryApp.this, "Textbook not found.");
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(InventoryApp.this, "Invalid SKU: must be a number.");
			}
		}
	}

	private class DisplayButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				int sku = Integer.parseInt(skuField.getText());
				Textbook textbook = inventory.findTextbook(sku);
				if (textbook != null) {
					JOptionPane.showMessageDialog(InventoryApp.this, textbook.toString());
				} else {
					JOptionPane.showMessageDialog(InventoryApp.this, "Textbook not found.");
				}
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(InventoryApp.this, "Invalid SKU: must be a number.");
			}
		}
	}

	private class Inventory {
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

	private class Textbook implements Serializable {
		private int sku;
		private String title;
		private double price;
		private int quantity;

		public Textbook(int sku, String title, double price, int quantity) {
			this.sku = sku;
			this.title = title;
			this.price = price;
			this.quantity = quantity;
		}

		public int getSku() {
			return sku;
		}

		public String getTitle() {
			return title;
		}

		public double getPrice() {
			return price;
		}

		public int getQuantity() {
			return quantity;
		}

		@Override
		public String toString() {
			return String.format("SKU: %d, Title: %s, Price: %.2f, Quantity: %d", sku, title, price, quantity);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new InventoryApp().setVisible(true));
	}
}